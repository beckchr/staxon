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

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.junit.Test;

import de.odysseus.staxon.json.jaxb.JsonXML;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleRootElement;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleType;

public class JsonXMLObjectProviderTest {
	@JsonXML
	static class JsonXMLDefault {}

	@JsonXML(virtualRoot = true, multiplePaths = "/elements")
	static class JsonXMLVirtualSampleRootElement {}

	@Test
	public void testIsReadWriteable() {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		Annotation[] jsonXMLAnnotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};

		Assert.assertTrue(provider.isReadWriteable(SampleRootElement.class, null, jsonXMLAnnotations, MediaType.APPLICATION_JSON_TYPE));
		Assert.assertTrue(provider.isReadWriteable(SampleType.class, null, jsonXMLAnnotations, MediaType.APPLICATION_JSON_TYPE));
		Assert.assertFalse(provider.isReadWriteable(Object.class, null, jsonXMLAnnotations, MediaType.APPLICATION_JSON_TYPE));
		Assert.assertFalse(provider.isReadWriteable(SampleType.class, null, new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
		Assert.assertFalse(provider.isReadWriteable(SampleRootElement.class, null, jsonXMLAnnotations, MediaType.APPLICATION_XML_TYPE));
	}

	@Test
	public void testReadSampleRootElement() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		Annotation[] annotations = new Annotation[0];
		String json = "{\"sampleRootElement\":{\"@attribute\":\"hello\",\"elements\":[\"world\"]}}";

		SampleRootElement sampleRootElement = (SampleRootElement)provider.read(SampleRootElement.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, new StringReader(json));

		Assert.assertEquals("hello", sampleRootElement.attribute);
		Assert.assertEquals("world", sampleRootElement.elements.get(0));
	}

	@Test
	public void testReadSampleType() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};
		String json = "{\"sampleType\":{\"element\":\"hi!\"}}";

		SampleType sampleType = (SampleType)provider.read(SampleType.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, new StringReader(json));

		Assert.assertEquals("hi!", sampleType.element);
	}

	@Test
	public void testWriteSampleRootElement() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		Annotation[] annotations = new Annotation[0];
		
		SampleRootElement sampleRootElement = new SampleRootElement();
		sampleRootElement.attribute = "hello";
		sampleRootElement.elements = Arrays.asList("world");	

		StringWriter writer = new StringWriter();
		provider.write(SampleRootElement.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, writer, sampleRootElement);

		String json = "{\"sampleRootElement\":{\"@attribute\":\"hello\",\"elements\":[\"world\"]}}";
		Assert.assertEquals(json, writer.toString());
	}

	@Test
	public void testWriteSampleType() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};

		SampleType sampleType = new SampleType();
		sampleType.element = "hi!";

		StringWriter writer = new StringWriter();
		provider.write(SampleType.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, writer, sampleType);

		String json = "{\"sampleType\":{\"element\":\"hi!\"}}";
		Assert.assertEquals(json, writer.toString());
	}
	
	@Test
	public void testReadNull() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		Annotation[] annotations = new Annotation[0];
		String json = "null";

		SampleRootElement sampleRootElement = (SampleRootElement)provider.read(SampleRootElement.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, new StringReader(json));

		Assert.assertNull(sampleRootElement);
	}

	@Test
	public void testReadNullWithVirtualRoot() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		Annotation[] annotations = new Annotation[]{JsonXMLVirtualSampleRootElement.class.getAnnotation(JsonXML.class)};
		String json = "null";

		SampleRootElement sampleRootElement = (SampleRootElement)provider.read(SampleRootElement.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, new StringReader(json));

		Assert.assertNotNull(sampleRootElement);
	}

	@Test
	public void testWriteNull() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		Annotation[] annotations = new Annotation[0];

		StringWriter writer = new StringWriter();
		provider.write(SampleRootElement.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, writer, null);

		String json = "null";
		Assert.assertEquals(json, writer.toString());
	}


	@Test
	public void testWriteNullWithVirtualRoot() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		Annotation[] annotations = new Annotation[]{JsonXMLVirtualSampleRootElement.class.getAnnotation(JsonXML.class)};

		StringWriter writer = new StringWriter();
		provider.write(SampleRootElement.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, writer, null);

		String json = "null";
		Assert.assertEquals(json, writer.toString());
	}
}
