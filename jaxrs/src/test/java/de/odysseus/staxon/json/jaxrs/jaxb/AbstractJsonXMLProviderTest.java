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
package de.odysseus.staxon.json.jaxrs.jaxb;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import junit.framework.Assert;

import org.junit.Test;

import de.odysseus.staxon.json.jaxb.JsonXML;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleRootElement;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleType;

public class AbstractJsonXMLProviderTest {
	static class TestProvider extends AbstractJsonXMLProvider {
		public TestProvider() {
			super(null);
		}
		@Override
		protected boolean isReadWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
			return false;
		}
		@Override
		public Object read(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
				MultivaluedMap<String, String> httpHeaders, Reader entityStream) throws IOException,
				WebApplicationException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void write(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
				MultivaluedMap<String, Object> httpHeaders, Writer entityStream, Object entry)
				throws IOException, WebApplicationException {
			throw new UnsupportedOperationException();
		}
	}
	
	@JsonXML
	static class JsonXMLDefault {}

	@JsonXML(autoArray = true, namespaceDeclarations = false, namespaceSeparator = '_', prettyPrint = true, virtualRoot = true)
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
	public void testGetCharset() {
		Assert.assertEquals("UTF-8", new TestProvider().getCharset(MediaType.APPLICATION_JSON_TYPE));
		Map<String, String> parameters = new HashMap<String, String>();		
		parameters.put("charset", "ASCII");
		MediaType customMediaType = new MediaType("application", "json", parameters);
		Assert.assertEquals("ASCII", new TestProvider().getCharset(customMediaType));
	}
	
	@Test
	public void testGetJsonXML() {
		JsonXML typeAnnotation = SampleRootElement.class.getAnnotation(JsonXML.class);
		Assert.assertEquals(typeAnnotation, new TestProvider().getJsonXML(SampleRootElement.class, new Annotation[0]));

		Annotation[] resourceAnnotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};
		Assert.assertEquals(resourceAnnotations[0], new TestProvider().getJsonXML(SampleType.class, resourceAnnotations));

		Assert.assertNull(new TestProvider().getJsonXML(SampleType.class, new Annotation[0]));
	}

	@Test
	public void testGetSize() {
		Assert.assertEquals(-1, new TestProvider().getSize(null, null, null, null, null));
	}
	
	@Test
	public void testIsSupported() {
		Assert.assertTrue(new TestProvider().isSupported(MediaType.APPLICATION_JSON_TYPE));
		Assert.assertTrue(new TestProvider().isSupported(new MediaType("text", "json")));
		Assert.assertTrue(new TestProvider().isSupported(new MediaType("text", "JSON")));
		Assert.assertTrue(new TestProvider().isSupported(new MediaType("text", "special+json")));
		Assert.assertFalse(new TestProvider().isSupported(MediaType.APPLICATION_XML_TYPE));
	}
}
