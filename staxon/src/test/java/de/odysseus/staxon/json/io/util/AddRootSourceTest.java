package de.odysseus.staxon.json.io.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.junit.Assert;
import org.junit.Test;

import de.odysseus.staxon.json.JsonXMLStreamReader;
import de.odysseus.staxon.json.JsonXMLStreamUtil;
import de.odysseus.staxon.json.io.jackson.JacksonStreamFactory;
import de.odysseus.staxon.json.io.util.AddRootSource;

public class AddRootSourceTest {
	private AddRootSource createSource(StringReader reader, String root) throws IOException {
		return new AddRootSource(new JacksonStreamFactory().createJsonStreamSource(reader), root);
	}

	void verify(XMLStreamReader reader, int expectedEventType, String expectedLocalName, String expectedText) {
		Assert.assertEquals(expectedEventType, reader.getEventType());
		Assert.assertEquals(expectedLocalName, reader.getLocalName());
		Assert.assertEquals(expectedText, reader.getText());
	}

	/**
	 * <code>&lt;alice&gt;bob&lt;/alice&gt;</code>
	 */
	@Test
	public void testTextContent() throws Exception {
		String input = "\"bob\"";
		XMLStreamReader reader = new JsonXMLStreamReader(createSource(new StringReader(input), "alice"));
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
		String input = "{\"bob\":\"charlie\",\"david\":\"edgar\"}";
		XMLStreamReader reader = new JsonXMLStreamReader(createSource(new StringReader(input), "alice"));
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
		String input = "{\"bob\":[\"charlie\",\"david\"]}";
		XMLStreamReader reader = new JsonXMLStreamReader(createSource(new StringReader(input), "alice"));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.PROCESSING_INSTRUCTION, null, null);
		Assert.assertEquals(JsonXMLStreamUtil.PI_MULTIPLE_TARGET, reader.getPITarget());
		Assert.assertEquals("bob", reader.getPIData());
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
		String input = "{\"@charlie\":\"david\",\"$\":\"bob\"}";
		XMLStreamReader reader = new JsonXMLStreamReader(createSource(new StringReader(input), "alice"));
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
		String input = "{\"@xmlns\":\"http://some-namespace\",\"$\":\"bob\"}";
		XMLStreamReader reader = new JsonXMLStreamReader(createSource(new StringReader(input), "alice"));
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
}
