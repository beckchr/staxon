/*
 * Copyright 2011 Odysseus Software GmbH
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
package de.odysseus.staxon.json.jaxrs.jaxb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Assert;

import org.junit.Test;

import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.json.jaxrs.JsonXML;
import de.odysseus.staxon.json.jaxrs.jaxb.model.ObjectFactory;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleRootElement;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleType;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleTypeWithNamespace;

public class AbstractJsonXMLProviderTest {
	static class DummyProvider extends AbstractJsonXMLProvider {
		@Override
		public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
			return false;
		}
		@Override
		public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
			return false;
		}
		@Override
		public Object readFrom(Class<Object> arg0, Type arg1, Annotation[] arg2, MediaType arg3,
				MultivaluedMap<String, String> arg4, InputStream arg5) throws IOException, WebApplicationException {
			return null;
		}
		@Override
		public void writeTo(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4,
				MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
		}
	}
	
	@JsonXML
	static class JsonXMLDefault {}

	@JsonXML(autoArray = true, namespaceDeclarations = false, namespaceSeparator = '_', prettyPrint = true, virtualRoot = "root")
	static class JsonXMLCustom {}

	@XmlType
	static class EmptyType {}

	@Test
	public void testGetAnnotation() {
		Annotation[] annotations = new Annotation[2];
		annotations[0] = SampleType.class.getAnnotation(XmlType.class);
		annotations[1] = JsonXMLDefault.class.getAnnotation(JsonXML.class);
		Assert.assertEquals(XmlType.class, AbstractJsonXMLProvider.getAnnotation(annotations, XmlType.class).annotationType());
		Assert.assertEquals(JsonXML.class, AbstractJsonXMLProvider.getAnnotation(annotations, JsonXML.class).annotationType());
		Assert.assertNull(AbstractJsonXMLProvider.getAnnotation(annotations, XmlRootElement.class));
	}

	@Test
	public void testCreateInputFactory() {
		JsonXMLInputFactory factory = new DummyProvider().createInputFactory(JsonXMLDefault.class.getAnnotation(JsonXML.class));
		Assert.assertEquals(Boolean.TRUE, factory.getProperty(JsonXMLInputFactory.PROP_MULTIPLE_PI));
		Assert.assertEquals(Character.valueOf(':'), factory.getProperty(JsonXMLInputFactory.PROP_NAMESPACE_SEPARATOR));
		Assert.assertNull(factory.getProperty(JsonXMLInputFactory.PROP_VIRTUAL_ROOT));

		factory = new DummyProvider().createInputFactory(JsonXMLCustom.class.getAnnotation(JsonXML.class));
		Assert.assertEquals(Boolean.TRUE, factory.getProperty(JsonXMLInputFactory.PROP_MULTIPLE_PI));
		Assert.assertEquals(Character.valueOf('_'), factory.getProperty(JsonXMLInputFactory.PROP_NAMESPACE_SEPARATOR));
		Assert.assertEquals("root", factory.getProperty(JsonXMLInputFactory.PROP_VIRTUAL_ROOT));
	}

	@Test
	public void testCreateOutputFactory() {
		JsonXMLOutputFactory factory = new DummyProvider().createOutputFactory(JsonXMLDefault.class.getAnnotation(JsonXML.class));
		Assert.assertEquals(Boolean.TRUE, factory.getProperty(JsonXMLOutputFactory.PROP_MULTIPLE_PI));
		Assert.assertEquals(Character.valueOf(':'), factory.getProperty(JsonXMLOutputFactory.PROP_NAMESPACE_SEPARATOR));
		Assert.assertNull(factory.getProperty(JsonXMLOutputFactory.PROP_VIRTUAL_ROOT));
		Assert.assertEquals(Boolean.TRUE, factory.getProperty(JsonXMLOutputFactory.PROP_NAMESPACE_DECLARATIONS));
		Assert.assertEquals(Boolean.FALSE, factory.getProperty(JsonXMLOutputFactory.PROP_PRETTY_PRINT));
		Assert.assertEquals(Boolean.FALSE, factory.getProperty(JsonXMLOutputFactory.PROP_AUTO_ARRAY));

		factory = new DummyProvider().createOutputFactory(JsonXMLCustom.class.getAnnotation(JsonXML.class));
		Assert.assertEquals(Boolean.TRUE, factory.getProperty(JsonXMLOutputFactory.PROP_MULTIPLE_PI));
		Assert.assertEquals(Character.valueOf('_'), factory.getProperty(JsonXMLOutputFactory.PROP_NAMESPACE_SEPARATOR));
		Assert.assertEquals("root", factory.getProperty(JsonXMLOutputFactory.PROP_VIRTUAL_ROOT));
		Assert.assertEquals(Boolean.FALSE, factory.getProperty(JsonXMLOutputFactory.PROP_NAMESPACE_DECLARATIONS));
		Assert.assertEquals(Boolean.TRUE, factory.getProperty(JsonXMLOutputFactory.PROP_PRETTY_PRINT));
		Assert.assertEquals(Boolean.TRUE, factory.getProperty(JsonXMLOutputFactory.PROP_AUTO_ARRAY));
	}
	
	@Test
	public void testCreateJAXBElement() throws JAXBException {
		AbstractJsonXMLProvider provider = new DummyProvider();
		Assert.assertNull(provider.createJAXBElement(SampleRootElement.class, null, new SampleRootElement()));
		Assert.assertNull(provider.createJAXBElement(EmptyType.class, null, new EmptyType()));
		Assert.assertNotNull(provider.createJAXBElement(SampleType.class, null, new SampleType()));
		Assert.assertNotNull(provider.createJAXBElement(SampleType.class, "sampleType", new SampleType()));
		Assert.assertNull(provider.createJAXBElement(SampleType.class, "badName", new SampleType()));
	}
	
	@Test
	public void testGetEncoding() {
		Assert.assertEquals("UTF-8", new DummyProvider().getEncoding(MediaType.APPLICATION_JSON_TYPE));
		Map<String, String> parameters = new HashMap<String, String>();		
		parameters.put("charset", "ASCII");
		MediaType customMediaType = new MediaType("application", "json", parameters);
		Assert.assertEquals("ASCII", new DummyProvider().getEncoding(customMediaType));
	}
	
	@Test
	public void testGetJsonXML() {
		JsonXML typeAnnotation = SampleType.class.getAnnotation(JsonXML.class);
		Assert.assertEquals(typeAnnotation, new DummyProvider().getJsonXML(SampleType.class, new Annotation[0]));

		Annotation[] resourceAnnotations = new Annotation[]{JsonXMLCustom.class.getAnnotation(JsonXML.class)};
		Assert.assertEquals(resourceAnnotations[0], new DummyProvider().getJsonXML(SampleType.class, resourceAnnotations));
	}
	
	@Test
	public void testGetSize() {
		Assert.assertEquals(-1, new DummyProvider().getSize(null, null, null, null, null));
	}
	
	@Test
	public void testIsMappable() {
		Assert.assertTrue(new DummyProvider().isMappable(SampleRootElement.class));
		Assert.assertTrue(new DummyProvider().isMappable(SampleType.class));
		Assert.assertFalse(new DummyProvider().isMappable(getClass()));
	}
	
	@Test
	public void testIsSupported() {
		Assert.assertTrue(new DummyProvider().isSupported(MediaType.APPLICATION_JSON_TYPE));
		Assert.assertTrue(new DummyProvider().isSupported(new MediaType("text", "json")));
		Assert.assertTrue(new DummyProvider().isSupported(new MediaType("text", "JSON")));
		Assert.assertTrue(new DummyProvider().isSupported(new MediaType("text", "special+json")));
		Assert.assertFalse(new DummyProvider().isSupported(MediaType.APPLICATION_XML_TYPE));
	}
	
	@Test
	public void testGetNamespaceURI_XmlType() {
		Assert.assertEquals(XMLConstants.NULL_NS_URI,
				new DummyProvider().getNamespaceURI(SampleType.class.getAnnotation(XmlType.class), null));
		Assert.assertEquals("urn:staxon-jaxrs:test",
				new DummyProvider().getNamespaceURI(SampleTypeWithNamespace.class.getAnnotation(XmlType.class), null));
	}

	@Test
	public void testGetNamespaceURI_XmlElementDecl() throws Exception {
			Method createSampleType =
					ObjectFactory.class.getMethod("createSampleType", SampleType.class);
		Assert.assertEquals(XMLConstants.NULL_NS_URI,
				new DummyProvider().getNamespaceURI(createSampleType.getAnnotation(XmlElementDecl.class), null));
		Method createSampleTypeWithNamespace =
				ObjectFactory.class.getMethod("createSampleTypeWithNamespace", SampleTypeWithNamespace.class);
		Assert.assertEquals("urn:staxon-jaxrs:test",
				new DummyProvider().getNamespaceURI(createSampleTypeWithNamespace.getAnnotation(XmlElementDecl.class), null));
	}

	@Test
	public void testMarshallXmlRootElement() throws Exception {
		JsonXML config = JsonXMLDefault.class.getAnnotation(JsonXML.class);
		MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
		ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
		Object entry = new SampleRootElement();
		Class<?> type = SampleRootElement.class;
		AbstractJsonXMLProvider provider = new DummyProvider();
		String encoding = provider.getEncoding(mediaType);

		XMLStreamWriter writer = provider.createOutputFactory(config).createXMLStreamWriter(entityStream);
		writer.setPrefix("test", "urn:staxon-jaxrs:test");
		Marshaller marshaller = JAXBContext.newInstance(type).createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
		provider.marshal(type, config, marshaller, writer, entry);
		writer.close();
		Assert.assertEquals("{\"sampleRootElement\":null}", new String(entityStream.toByteArray(), encoding));
	}
	
	@Test
	public void testMarshallXmlType() throws Exception {
		JsonXML config = JsonXMLDefault.class.getAnnotation(JsonXML.class);
		MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
		ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
		Object entry = new SampleType();
		Class<?> type = SampleType.class;
		AbstractJsonXMLProvider provider = new DummyProvider();
		String encoding = provider.getEncoding(mediaType);

		XMLStreamWriter writer = provider.createOutputFactory(config).createXMLStreamWriter(entityStream);
		Marshaller marshaller = JAXBContext.newInstance(type).createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
		provider.marshal(type, config, marshaller, writer, entry);
		writer.close();
		Assert.assertEquals("{\"sampleType\":null}", new String(entityStream.toByteArray(), encoding));
	}

	@Test
	public void testMarshallXmlTypeWithNamespace() throws Exception {
		JsonXML config = JsonXMLDefault.class.getAnnotation(JsonXML.class);
		MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
		ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
		Object entry = new SampleTypeWithNamespace();
		Class<?> type = SampleTypeWithNamespace.class;
		AbstractJsonXMLProvider provider = new DummyProvider();
		String encoding = provider.getEncoding(mediaType);

		XMLStreamWriter writer = provider.createOutputFactory(config).createXMLStreamWriter(entityStream);
		Marshaller marshaller = JAXBContext.newInstance(type).createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
		provider.marshal(type, config, marshaller, writer, entry);
		writer.close();
		Assert.assertEquals("{\"ns2:sampleTypeWithNamespace\":{\"@xmlns:ns2\":\"urn:staxon-jaxrs:test\"}}",
				new String(entityStream.toByteArray(), encoding)); // TODO don't rely on prefix "ns2"
	}
}
