/*
 * Copyright 2011, 2012 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.odysseus.staxon.json;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.*;

import de.odysseus.staxon.event.SimpleXMLEventWriter;
import de.odysseus.staxon.xml.SimpleXMLStreamWriter;
import org.junit.Assert;
import org.junit.Test;

public class JsonXMLStreamReaderTest {
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
		String input = "{\"alice\":\"bob\"}";
		XMLStreamReader reader = new JsonXMLInputFactory().createXMLStreamReader(new StringReader(input));
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
		String input = "{\"alice\":{\"bob\":\"charlie\",\"david\":\"edgar\"}}";
		XMLStreamReader reader = new JsonXMLInputFactory().createXMLStreamReader(new StringReader(input));
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
		String input = "{\"alice\":{\"bob\":[\"charlie\",\"david\"]}}";
		XMLStreamReader reader = new JsonXMLInputFactory().createXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.PROCESSING_INSTRUCTION, null, null);
		Assert.assertEquals(JsonXMLStreamConstants.MULTIPLE_PI_TARGET, reader.getPITarget());
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
		String input = "{\"alice\":{\"@charlie\":\"david\",\"$\":\"bob\"}}";
		XMLStreamReader reader = new JsonXMLInputFactory().createXMLStreamReader(new StringReader(input));
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
		String input = "{\"alice\":{\"@xmlns\":\"http://some-namespace\",\"$\":\"bob\"}}";
		XMLStreamReader reader = new JsonXMLInputFactory().createXMLStreamReader(new StringReader(input));
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
	
	/**
	 * Should use namespace mappings
	 * <code>&lt;foo:alice&gt;bob&lt;/alice&gt;</code>
	 */
	@Test
	public void testNamespaceMappings() throws Exception {
		String input = "{\"foo:alice\":\"bob\"}";
		JsonXMLConfig config = new JsonXMLConfigBuilder().namespaceMapping("foo", "http://some-namespace").build();
		XMLStreamReader reader = new JsonXMLInputFactory(config).createXMLStreamReader(new StringReader(input));
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

	/**
	 * {"test":null} must produce &lt;test xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" >&lt;/test>
	 * @throws Exception
	 */
	@Test
	public void testReadJsonNullToWriteXSINil() throws Exception {
		String input = "{\"test\":null}";
		JsonXMLConfig config = new JsonXMLConfigBuilder().readXmlNil(false).writeXmlNil(true).build();
		XMLStreamReader reader = new JsonXMLInputFactory(config).createXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "test", null);
		Assert.assertEquals("", reader.getNamespaceURI());
		Assert.assertEquals(1, reader.getAttributeCount());
		QName attributeName = reader.getAttributeName(0);
		Assert.assertEquals("nil", attributeName.getLocalPart());
		Assert.assertEquals("xsi", attributeName.getPrefix());
		Assert.assertEquals("http://www.w3.org/2001/XMLSchema-instance", attributeName.getNamespaceURI());
		reader.close();
	}

	@Test
	public void testReadXMLNilToWriteJsonNull() throws Exception {
		String input = "<test xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\" ></test>";
		JsonXMLConfig config = new JsonXMLConfigBuilder().readXmlNil(true).namespaceDeclarations(true).writeXmlNil(false).build();
		XMLInputFactory inputFactory = new JsonXMLInputFactory(config);
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(input));
		XMLEventReader xmlEventReader = inputFactory.createXMLEventReader(reader);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		XMLEventWriter writer = new JsonXMLOutputFactory(config).createXMLEventWriter(outputStream);
		writer.add(xmlEventReader);
		xmlEventReader.close();
		writer.close();
		outputStream.flush();
		Assert.assertArrayEquals(outputStream.toByteArray(), "{\"test\":null}".getBytes());
		outputStream.close();
	}

	@Test
	public void testReadXMLNilToWriteJsonEmpty() throws Exception {
		String input = "<test></test>";
		JsonXMLConfig config = new JsonXMLConfigBuilder().readXmlNil(true).writeXmlNil(false).build();
		XMLInputFactory inputFactory = new JsonXMLInputFactory(config);
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(input));
		XMLEventReader xmlEventReader = inputFactory.createXMLEventReader(reader);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		XMLEventWriter writer = new JsonXMLOutputFactory(config).createXMLEventWriter(outputStream);
		writer.add(xmlEventReader);
		xmlEventReader.close();
		writer.close();
		outputStream.flush();
		Assert.assertArrayEquals(outputStream.toByteArray(), "{\"test\":\"\"}".getBytes());
		outputStream.close();
	}

	/**
	 * {"test": ""} must produce &lt;test>&lt;/test>
	 * @throws Exception
	 */
	@Test
	public void testReadJsonEmptyToWriteXML() throws Exception {
		String input = "{\"test\":\"\"}";
		JsonXMLConfig config = new JsonXMLConfigBuilder().readXmlNil(false).writeXmlNil(true).build();
		XMLStreamReader reader = new JsonXMLInputFactory(config).createXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "test", null);
		Assert.assertEquals("", reader.getNamespaceURI());
		Assert.assertEquals(0, reader.getAttributeCount());
		reader.close();
	}

	/**
	 * <code>&lt;alice xmlns="http://foo" xmlns:bar="http://bar"&gt;bob&lt;/alice&gt;</code>
	 * with badgerfish notation
	 */
	@Test
	public void testNamespacesBadgerfish() throws Exception {
		String input = "{\"alice\":{\"@xmlns\":{\"$\":\"http://foo\",\"bar\":\"http://bar\"},\"$\":\"bob\"}}";
		XMLStreamReader reader = new JsonXMLInputFactory().createXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		Assert.assertEquals("http://foo", reader.getNamespaceURI());
		Assert.assertEquals("http://bar", reader.getNamespaceURI("bar"));
		Assert.assertEquals(0, reader.getAttributeCount());
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "bob");
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
		Assert.assertEquals("http://foo", reader.getNamespaceURI());
		Assert.assertEquals("http://bar", reader.getNamespaceURI("bar"));
		reader.next();
		verify(reader, XMLStreamConstants.END_DOCUMENT, null, null);
		reader.close();
	}

	/**
	 * <code>&lt;alice&gt;bob&lt;/alice&gt;&lt;alice&gt;bob&lt;/alice&gt;</code>
	 */
	@Test
	public void testRootArray() throws Exception {
		String input = "{\"alice\":[\"bob\",\"bob\"]}";
		XMLStreamReader reader = new JsonXMLInputFactory().createXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.PROCESSING_INSTRUCTION, null, null);
		Assert.assertEquals(JsonXMLStreamConstants.MULTIPLE_PI_TARGET, reader.getPITarget());
		Assert.assertEquals("alice", reader.getPIData());
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "bob");
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
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
	 * <code>&lt;alice&gt;bob&lt;/alice&gt;&lt;alice&gt;bob&lt;/alice&gt;</code>
	 */
	@Test
	public void testRootArrayWithVirtualRoot() throws Exception {
		String input = "[\"bob\",\"bob\"]";
		JsonXMLInputFactory factory = new JsonXMLInputFactory();
		factory.setProperty(JsonXMLInputFactory.PROP_VIRTUAL_ROOT, new QName("alice"));
		XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.PROCESSING_INSTRUCTION, null, null);
		Assert.assertEquals(JsonXMLStreamConstants.MULTIPLE_PI_TARGET, reader.getPITarget());
		Assert.assertEquals("alice", reader.getPIData());
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "bob");
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
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


	@Test
	public void testSimpleValueArray() throws Exception {
		String input = "[\"edgar\",\"david\"]";
		XMLStreamReader reader = new JsonXMLInputFactory().createXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.PROCESSING_INSTRUCTION, null, null);
		Assert.assertEquals(JsonXMLStreamConstants.MULTIPLE_PI_TARGET, reader.getPITarget());
		Assert.assertNull(reader.getPIData());
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "edgar");
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "david");
		Assert.assertFalse(reader.hasNext());
		reader.close();
	}

	@Test
	public void testDocumentArray() throws Exception {
		String input = "[{\"alice\":\"bob\"},{\"alice\":\"bob\"}]";
		XMLStreamReader reader = new JsonXMLInputFactory().createXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.PROCESSING_INSTRUCTION, null, null);
		Assert.assertEquals(JsonXMLStreamConstants.MULTIPLE_PI_TARGET, reader.getPITarget());
		Assert.assertNull(reader.getPIData());
		reader.next();
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "bob");
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.END_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "bob");
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.END_DOCUMENT, null, null);
		Assert.assertFalse(reader.hasNext());
		reader.close();
	}

	/**
	 * <code>&lt;alice&gt;123.40&lt;/alice&gt;</code>
	 */
	@Test
	public void testNumber() throws Exception {
		String input = "{\"alice\" : 123.40}";
		JsonXMLStreamReader reader = new JsonXMLInputFactory().createXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "123.40");
		Assert.assertTrue(reader.hasNumber());
		Assert.assertFalse(reader.hasBoolean());
		Assert.assertEquals(new BigDecimal("123.40"), reader.getNumber());
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.END_DOCUMENT, null, null);
		reader.close();
	}

	/**
	 * <code>&lt;alice&gt;false&lt;/alice&gt;</code>
	 */
	@Test
	public void testBoolean() throws Exception {
		String input = "{\"alice\" : false}";
		JsonXMLStreamReader reader = new JsonXMLInputFactory().createXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.CHARACTERS, null, "false");
		Assert.assertFalse(reader.hasNumber());
		Assert.assertTrue(reader.hasBoolean());
		Assert.assertFalse(reader.getBoolean());
		reader.next();
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.END_DOCUMENT, null, null);
		reader.close();
	}

	/**
	 * <code>&lt;alice/&gt;</code>
	 */
	@Test
	public void testNull() throws Exception {
		String input = "{\"alice\" : null}";
		JsonXMLStreamReader reader = new JsonXMLInputFactory().createXMLStreamReader(new StringReader(input));
		verify(reader, XMLStreamConstants.START_DOCUMENT, null, null);
		reader.next();
		verify(reader, XMLStreamConstants.START_ELEMENT, "alice", null);
		reader.next();
//		verify(reader, XMLStreamConstants.CHARACTERS, null, null); // null is not reported
		verify(reader, XMLStreamConstants.END_ELEMENT, "alice", null);
		reader.next();
		verify(reader, XMLStreamConstants.END_DOCUMENT, null, null);
		reader.close();
	}
}
