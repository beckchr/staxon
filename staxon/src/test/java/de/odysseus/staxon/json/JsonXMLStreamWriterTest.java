package de.odysseus.staxon.json;

import java.io.StringWriter;

import javax.xml.stream.XMLStreamWriter;

import org.junit.Assert;
import org.junit.Test;

import de.odysseus.staxon.json.JsonXMLStreamUtil;

public class JsonXMLStreamWriterTest {
	/**
	 * <code>&lt;alice&gt;bob&lt;/alice&gt;</code>
	 */
	@Test
	public void testTextContent() throws Exception {
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = JsonXMLStreamUtil.createJsonXMLStreamWriter(result, false);
		//<code>&lt;alice&gt;bob&lt;/alice&gt;</code>
		writer.writeStartDocument();
		writer.writeStartElement("alice");
		writer.writeCharacters("bob");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		Assert.assertEquals("{\"alice\":\"bob\"}", result.toString());
	}

	/**
	 * <code>&lt;alice&gt;&lt;bob&gt;charlie&lt;/bob&gt;&lt;david&gt;edgar&lt;/david&gt;&lt;/alice&gt;</code>
	 */
	@Test
	public void testNested() throws Exception {
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = JsonXMLStreamUtil.createJsonXMLStreamWriter(result, false);
		writer.writeStartDocument();
		writer.writeStartElement("alice");
		writer.writeStartElement("bob");
		writer.writeCharacters("charlie");
		writer.writeEndElement();
		writer.writeStartElement("david");
		writer.writeCharacters("edgar");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		Assert.assertEquals("{\"alice\":{\"bob\":\"charlie\",\"david\":\"edgar\"}}", result.toString());
	}
	
	/**
	 * <code>&lt;alice&gt;&lt;bob&gt;charlie&lt;/bob&gt;&lt;bob&gt;david&lt;/bob&gt;&lt;/alice&gt;</code>
	 */
	@Test
	public void testArray() throws Exception {
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = JsonXMLStreamUtil.createJsonXMLStreamWriter(result, false);
		writer.writeStartDocument();
		writer.writeStartElement("alice");
		writer.writeProcessingInstruction(JsonXMLStreamUtil.PI_MULTIPLE_TARGET, "bob");
		writer.writeStartElement("bob");
		writer.writeCharacters("charlie");
		writer.writeEndElement();
		writer.writeStartElement("bob");
		writer.writeCharacters("david");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		Assert.assertEquals("{\"alice\":{\"bob\":[\"charlie\",\"david\"]}}", result.toString());
	}

	/**
	 * <code>&lt;alice charlie="david"&gt;bob&lt;/alice&gt;</code>
	 */
	@Test
	public void testAttributes() throws Exception {
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = JsonXMLStreamUtil.createJsonXMLStreamWriter(result, false);
		writer.writeStartDocument();
		writer.writeStartElement("alice");
		writer.writeAttribute("charlie", "david");
		writer.writeCharacters("bob");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		Assert.assertEquals("{\"alice\":{\"@charlie\":\"david\",\"$\":\"bob\"}}", result.toString());
	}
	
	/**
	 * <code>&lt;alice xmlns="http://some-namespace"&gt;bob&lt;/alice&gt;</code>
	 */
	@Test
	public void testNamespaces() throws Exception {
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = JsonXMLStreamUtil.createJsonXMLStreamWriter(result, false);
		writer.setDefaultNamespace("http://some-namespace");
		writer.writeStartDocument();
		writer.writeStartElement("alice");
		writer.writeDefaultNamespace("http://some-namespace");
		writer.writeCharacters("bob");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		Assert.assertEquals("{\"alice\":{\"@xmlns\":\"http://some-namespace\",\"$\":\"bob\"}}", result.toString());
	}
}
