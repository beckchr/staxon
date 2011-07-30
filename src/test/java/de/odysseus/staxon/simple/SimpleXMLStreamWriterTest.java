package de.odysseus.staxon.simple;

import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Assert;

import org.junit.Test;

import de.odysseus.staxon.simple.SimpleXMLStreamWriter;

public class SimpleXMLStreamWriterTest {
	/**
	 * <code>&lt;alice&gt;bob&lt;/alice&gt;</code>
	 */
	@Test
	public void testTextContent() throws Exception {
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = new SimpleXMLStreamWriter(result);
		writer.writeStartDocument();
		writer.writeStartElement("alice");
		writer.writeCharacters("bob");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><alice>bob</alice>", result.toString());
	}

	/**
	 * <code>&lt;alice&gt;&lt;bob&gt;charlie&lt;/bob&gt;&lt;david&gt;edgar&lt;/david&gt;&lt;/alice&gt;</code>
	 */
	@Test
	public void testNested() throws Exception {
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = new SimpleXMLStreamWriter(result);
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
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><alice><bob>charlie</bob><david>edgar</david></alice>", result.toString());
	}
	
	/**
	 * <code>&lt;alice&gt;&lt;bob&gt;charlie&lt;/bob&gt;&lt;bob&gt;david&lt;/bob&gt;&lt;/alice&gt;</code>
	 */
	@Test
	public void testArray() throws Exception {
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = new SimpleXMLStreamWriter(result);
		writer.writeStartDocument();
		writer.writeStartElement("alice");
		writer.writeStartElement("bob");
		writer.writeCharacters("charlie");
		writer.writeEndElement();
		writer.writeStartElement("bob");
		writer.writeCharacters("david");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><alice><bob>charlie</bob><bob>david</bob></alice>", result.toString());
	}

	/**
	 * <code>&lt;alice charlie="david"&gt;bob&lt;/alice&gt;</code>
	 */
	@Test
	public void testAttributes() throws Exception {
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = new SimpleXMLStreamWriter(result);
		writer.writeStartDocument();
		writer.writeStartElement("alice");
		writer.writeAttribute("charlie", "david");
		writer.writeCharacters("bob");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><alice charlie=\"david\">bob</alice>", result.toString());
	}
	
	/**
	 * <code>&lt;alice xmlns="http://some-namespace"&gt;bob&lt;/alice&gt;</code>
	 */
	@Test
	public void testNamespaces() throws Exception {
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = new SimpleXMLStreamWriter(result);
		writer.setDefaultNamespace("http://some-namespace");
		writer.writeStartDocument();
		writer.writeStartElement("alice");
		writer.writeDefaultNamespace("http://some-namespace");
		writer.writeCharacters("bob");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><alice xmlns=\"http://some-namespace\">bob</alice>", result.toString());
	}

	@Test
	public void testOther() throws XMLStreamException {
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = new SimpleXMLStreamWriter(result);
		writer.setDefaultNamespace("http://foo");
		writer.writeStartDocument();
		writer.writeStartElement("alice");
		writer.writeEmptyElement("bar", "bob", "http://bar");
		writer.writeNamespace("bar", "http://bar");
		writer.writeAttribute("jane", "do\"'<>lly");
		writer.writeCharacters("hel\"'<>lo");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
//		System.out.println(result);
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><alice><bar:bob xmlns:bar=\"http://bar\" jane=\"do&quot;'&lt;&gt;lly\"/>hel\"'&lt;&gt;lo</alice>", result.toString());
	}
}
