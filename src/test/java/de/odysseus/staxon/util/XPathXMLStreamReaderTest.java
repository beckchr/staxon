package de.odysseus.staxon.util;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.junit.Test;

public class XPathXMLStreamReaderTest {
	@Test
	public void testNext() throws XMLStreamException {
		String xml = "<alice><bob>charlie</bob><edgar/><bob>david</bob></alice>";
		XPathXMLStreamReader reader = new XPathXMLStreamReader(XMLInputFactory.newFactory().createXMLStreamReader(new StringReader(xml)));
		Assert.assertNull(reader.getXPath());
		reader.next();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "alice");
		Assert.assertEquals("/alice", reader.getXPath());
		reader.next();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "bob");
		Assert.assertEquals("/alice/bob[1]", reader.getXPath());
		reader.next();
		reader.require(XMLStreamConstants.CHARACTERS, null, null);
		Assert.assertEquals("/alice/bob[1]", reader.getXPath());
		reader.next();
		reader.require(XMLStreamConstants.END_ELEMENT, null, "bob");
		Assert.assertEquals("/alice/bob[1]", reader.getXPath());
		reader.next();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "edgar");
		Assert.assertEquals("/alice/edgar[1]", reader.getXPath());
		reader.next();
		reader.require(XMLStreamConstants.END_ELEMENT, null, "edgar");
		Assert.assertEquals("/alice/edgar[1]", reader.getXPath());
		reader.next();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "bob");
		Assert.assertEquals("/alice/bob[2]", reader.getXPath());
		reader.next();
		reader.require(XMLStreamConstants.CHARACTERS, null, null);
		Assert.assertEquals("/alice/bob[2]", reader.getXPath());
		reader.next();
		reader.require(XMLStreamConstants.END_ELEMENT, null, "bob");
		Assert.assertEquals("/alice/bob[2]", reader.getXPath());
		reader.next();
		reader.require(XMLStreamConstants.END_ELEMENT, null, "alice");
		Assert.assertEquals("/alice", reader.getXPath());
		reader.next();
		reader.require(XMLStreamConstants.END_DOCUMENT, null, null);
		Assert.assertNull(reader.getXPath());
	}

	@Test
	public void testNextTag() throws XMLStreamException {
		String xml = "<alice><bob>charlie</bob><edgar/><bob>david</bob></alice>";
		XPathXMLStreamReader reader = new XPathXMLStreamReader(XMLInputFactory.newFactory().createXMLStreamReader(new StringReader(xml)));
		Assert.assertNull(reader.getXPath());
		reader.nextTag();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "alice");
		Assert.assertEquals("/alice", reader.getXPath());
		reader.nextTag();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "bob");
		Assert.assertEquals("/alice/bob[1]", reader.getXPath());
		reader.getElementText();
		reader.require(XMLStreamConstants.END_ELEMENT, null, "bob");
		Assert.assertEquals("/alice/bob[1]", reader.getXPath());
		reader.nextTag();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "edgar");
		Assert.assertEquals("/alice/edgar[1]", reader.getXPath());
		reader.nextTag();
		reader.require(XMLStreamConstants.END_ELEMENT, null, "edgar");
		Assert.assertEquals("/alice/edgar[1]", reader.getXPath());
		reader.nextTag();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "bob");
		Assert.assertEquals("/alice/bob[2]", reader.getXPath());
		reader.getElementText();
		reader.require(XMLStreamConstants.END_ELEMENT, null, "bob");
		Assert.assertEquals("/alice/bob[2]", reader.getXPath());
		reader.nextTag();
		reader.require(XMLStreamConstants.END_ELEMENT, null, "alice");
		Assert.assertEquals("/alice", reader.getXPath());
	}

	@Test
	public void testSimplePathAndPosition() throws XMLStreamException {
		String xml = "<alice><bob>charlie</bob><edgar/><bob>david</bob></alice>";
		XPathXMLStreamReader reader = new XPathXMLStreamReader(XMLInputFactory.newFactory().createXMLStreamReader(new StringReader(xml)));
		Assert.assertNull(reader.getXPath(null, false));
		Assert.assertEquals(0, reader.getPosition());
		reader.nextTag();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "alice");
		Assert.assertEquals("/alice", reader.getXPath(null, false));
		Assert.assertEquals(1, reader.getPosition());
		reader.nextTag();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "bob");
		Assert.assertEquals("/alice/bob", reader.getXPath(null, false));
		Assert.assertEquals(1, reader.getPosition());
		reader.getElementText();
		reader.require(XMLStreamConstants.END_ELEMENT, null, "bob");
		Assert.assertEquals("/alice/bob", reader.getXPath(null, false));
		Assert.assertEquals(1, reader.getPosition());
		reader.nextTag();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "edgar");
		Assert.assertEquals("/alice/edgar", reader.getXPath(null, false));
		Assert.assertEquals(1, reader.getPosition());
		reader.nextTag();
		reader.require(XMLStreamConstants.END_ELEMENT, null, "edgar");
		Assert.assertEquals("/alice/edgar", reader.getXPath(null, false));
		Assert.assertEquals(1, reader.getPosition());
		reader.nextTag();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "bob");
		Assert.assertEquals("/alice/bob", reader.getXPath(null, false));
		Assert.assertEquals(2, reader.getPosition());
		reader.getElementText();
		reader.require(XMLStreamConstants.END_ELEMENT, null, "bob");
		Assert.assertEquals("/alice/bob", reader.getXPath(null, false));
		Assert.assertEquals(2, reader.getPosition());
		reader.nextTag();
		reader.require(XMLStreamConstants.END_ELEMENT, null, "alice");
		Assert.assertEquals("/alice", reader.getXPath(null, false));
		Assert.assertEquals(1, reader.getPosition());
	}
}
