package de.odysseus.staxon.simple;

import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Assert;
import org.junit.Test;

import de.odysseus.staxon.simple.SimpleXMLStreamReader;

public class SimpleXMLStreamReaderTest {
	void verify(XMLStreamReader reader, int expectedEventType, String expectedLocalName, String expectedText) {
		Assert.assertEquals(expectedEventType, reader.getEventType());
		Assert.assertEquals(expectedLocalName, reader.getLocalName());
		Assert.assertEquals(expectedText, reader.getText());
	}

	/**
	 * <code>&lt;alice&gt;bob&lt;/alice&gt;</code>
	 */
	@Test
	public void test1() throws XMLStreamException {
		String input = "<?xml version=\"1.0\"?><alice>bob</alice>";
		XMLStreamReader reader = new SimpleXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "bob");
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.END_DOCUMENT, null, null);
		reader.close();
	}

	/**
	 * <code>&lt;alice&gt;&lt;bob&gt;charlie&lt;/bob&gt;&lt;david&gt;edgar&lt;/david&gt;&lt;/alice&gt;</code>
	 */
	@Test
	public void testNested() throws Exception {
		String input = "<?xml version=\"1.0\"?><alice><bob>charlie</bob><david>edgar</david></alice>";
		XMLStreamReader reader = new SimpleXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "bob", null);
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "charlie");
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "bob", null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "david", null);
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "edgar");
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "david", null);
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.END_DOCUMENT, null, null);
		reader.close();
	}
	
	/**
	 * <code>&lt;alice&gt;&lt;bob&gt;charlie&lt;/bob&gt;&lt;bob&gt;david&lt;/bob&gt;&lt;/alice&gt;</code>
	 */
	@Test
	public void testArray() throws Exception {
		String input = "<?xml version=\"1.0\"?><alice><bob>charlie</bob><bob>david</bob></alice>";
		XMLStreamReader reader = new SimpleXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "bob", null);
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "charlie");
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "bob", null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "bob", null);
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "david");
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "bob", null);
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.END_DOCUMENT, null, null);
		reader.close();
	}

	/**
	 * <code>&lt;alice charlie="david"&gt;bob&lt;/alice&gt;</code>
	 */
	@Test
	public void testAttributes() throws Exception {
		String input = "<?xml version=\"1.0\"?><alice charlie=\"david\">bob</alice>";
		XMLStreamReader reader = new SimpleXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		Assert.assertEquals(1, reader.getAttributeCount());
		Assert.assertEquals("david", reader.getAttributeValue(null, "charlie"));
		Assert.assertEquals("david", reader.getAttributeValue(XMLConstants.NULL_NS_URI, "charlie"));
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "bob");
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.END_DOCUMENT, null, null);
		reader.close();
	}
	
	/**
	 * <code>&lt;alice xmlns="http://some-namespace"&gt;bob&lt;/alice&gt;</code>
	 */
	@Test
	public void testNamespaces() throws Exception {
		String input = "<?xml version=\"1.0\"?><alice xmlns=\"http://some-namespace\">bob</alice>";
		XMLStreamReader reader = new SimpleXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		Assert.assertEquals("http://some-namespace", reader.getNamespaceURI());
		Assert.assertEquals(0, reader.getAttributeCount());
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "bob");
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
		Assert.assertEquals("http://some-namespace", reader.getNamespaceURI());
		reader.next();
		verify(reader, XMLStreamConstants.END_DOCUMENT, null, null);
		reader.close();
	}

	@Test
	public void testOther() throws Exception {
		String input = "<?xml version=\"1.0\"?><alice><bar:bob xmlns:bar=\"http://bar\" jane=\"do&quot;'&lt;&gt;lly\"/>hel\"'&lt;&gt;lo</alice>";
		XMLStreamReader reader = new SimpleXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "bob", null);
		Assert.assertEquals("http://bar", reader.getNamespaceURI());
		Assert.assertEquals(1, reader.getAttributeCount());
		Assert.assertEquals("do\"'<>lly", reader.getAttributeValue(null, "jane"));
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "bob", null);
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "hel\"'<>lo");
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.END_DOCUMENT, null, null);
		reader.close();
	}
}
