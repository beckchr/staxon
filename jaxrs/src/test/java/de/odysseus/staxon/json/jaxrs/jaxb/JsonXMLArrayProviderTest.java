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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.junit.Test;

import de.odysseus.staxon.json.jaxb.JsonXML;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleRootElement;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleType;

public class JsonXMLArrayProviderTest {
	@JsonXML
	static class JsonXMLDefault {}

	@JsonXML(virtualRoot = true, multiplePaths = "/elements")
	static class JsonXMLVirtualSampleRootElement {}

	static List<SampleRootElement> sampleRootElementList = new ArrayList<SampleRootElement>();
	static List<SampleType> sampleTypeList = new ArrayList<SampleType>();
	static List<Object> objectList = new ArrayList<Object>();
	static Iterable<SampleType> sampleTypeIterable = sampleTypeList;
	static SampleRootElement[] sampleRootELementArray = new SampleRootElement[0];
	static SampleType[] sampleTypeArray = new SampleType[0];
	static Object[] objectArray = new Object[0];
	
	@Test
	public void testIsReadWriteable() throws NoSuchFieldException {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};

		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		Assert.assertTrue(provider.isReadWriteable(List.class, type, annotations, MediaType.APPLICATION_JSON_TYPE));
		type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		Assert.assertTrue(provider.isReadWriteable(List.class, type, annotations, MediaType.APPLICATION_JSON_TYPE));
		type = getClass().getDeclaredField("objectList").getGenericType();
		Assert.assertFalse(provider.isReadWriteable(List.class, type, annotations, MediaType.APPLICATION_JSON_TYPE));
		type = getClass().getDeclaredField("sampleTypeIterable").getGenericType();
		Assert.assertFalse(provider.isReadWriteable(Iterable.class, type, annotations, MediaType.APPLICATION_JSON_TYPE));
		type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		Assert.assertFalse(provider.isReadWriteable(List.class, type, new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
		Assert.assertFalse(provider.isReadWriteable(List.class, type, annotations, MediaType.APPLICATION_XML_TYPE));
	}

	@Test
	public void testReadRootElementList() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		String json = "{\"sampleRootElement\":[{\"@attribute\":\"hello\"},{\"@attribute\":\"world\"}]}";

		@SuppressWarnings("unchecked")
		List<SampleRootElement> list = (List<SampleRootElement>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, new StringReader(json));

		Assert.assertEquals(2, list.size());
		Assert.assertEquals("hello", list.get(0).attribute);
		Assert.assertEquals("world", list.get(1).attribute);
	}

	@Test
	public void testReadRootElementList_DocumentArray() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		String json = "[{\"sampleRootElement\":{\"@attribute\":\"hello\"}},{\"sampleRootElement\":{\"@attribute\":\"world\"}}]";

		@SuppressWarnings("unchecked")
		List<SampleRootElement> list = (List<SampleRootElement>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, new StringReader(json));

		Assert.assertEquals(2, list.size());
		Assert.assertEquals("hello", list.get(0).attribute);
		Assert.assertEquals("world", list.get(1).attribute);
	}

	@Test
	public void testReadSampleTypeList() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};
		Type type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		String json = "{\"sampleType\":[{\"element\":\"hello\"},{\"element\":\"world\"}]}";

		@SuppressWarnings("unchecked")
		List<SampleType> list = (List<SampleType>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, new StringReader(json));

		Assert.assertEquals(2, list.size());
		Assert.assertEquals("hello", list.get(0).element);
		Assert.assertEquals("world", list.get(1).element);
	}

	@Test
	public void testReadSampleTypeList_DocumentArray() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};
		Type type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		String json = "[{\"sampleType\":{\"element\":\"hello\"}},{\"sampleType\":{\"element\":\"world\"}}]";

		@SuppressWarnings("unchecked")
		List<SampleType> list = (List<SampleType>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, new StringReader(json));

		Assert.assertEquals(2, list.size());
		Assert.assertEquals("hello", list.get(0).element);
		Assert.assertEquals("world", list.get(1).element);
	}

	@Test
	public void testReadSampleTypeListWithNullValues() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};
		Type type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		String json = "[null,{\"sampleType\":{\"element\":\"hi!\"}},null]";

		@SuppressWarnings("unchecked")
		List<SampleType> list = (List<SampleType>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, new StringReader(json));

		Assert.assertEquals(3, list.size());
		Assert.assertNull(list.get(0));
		Assert.assertEquals("hi!", list.get(1).element);
		Assert.assertNull(list.get(2));
	}

	@Test
	public void testReadEmptyList() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		String json = "{}";

		@SuppressWarnings("unchecked")
		List<SampleRootElement> list = (List<SampleRootElement>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, new StringReader(json));

		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testReadEmptyList2() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		String json = "{\"sampleRootElement\":[]}";

		@SuppressWarnings("unchecked")
		List<SampleRootElement> list = (List<SampleRootElement>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, new StringReader(json));

		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testReadEmptyList3() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		String json = "[]";

		@SuppressWarnings("unchecked")
		List<SampleRootElement> list = (List<SampleRootElement>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, new StringReader(json));

		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testReadEmptyListWithVirtualRoot() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[]{JsonXMLVirtualSampleRootElement.class.getAnnotation(JsonXML.class)};
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		String json = "[]";

		@SuppressWarnings("unchecked")
		List<SampleRootElement> list = (List<SampleRootElement>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, new StringReader(json));

		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testWriteSampleRootElementList() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		
		List<SampleRootElement> list = new ArrayList<SampleRootElement>();
		list.add(new SampleRootElement());
		list.get(0).attribute = "hello";
		list.add(new SampleRootElement());
		list.get(1).attribute = "world";

		StringWriter writer = new StringWriter();
		provider.write(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, writer, list);

		String json = "[{\"sampleRootElement\":{\"@attribute\":\"hello\"}},{\"sampleRootElement\":{\"@attribute\":\"world\"}}]";
		Assert.assertEquals(json, writer.toString());
	}

	@Test
	public void testWriteSampleTypeList() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};
		Type type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		
		List<SampleType> list = new ArrayList<SampleType>();
		list.add(new SampleType());
		list.get(0).element = "hello";
		list.add(new SampleType());
		list.get(1).element = "world";

		StringWriter writer = new StringWriter();
		provider.write(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, writer, list);

		String json = "[{\"sampleType\":{\"element\":\"hello\"}},{\"sampleType\":{\"element\":\"world\"}}]";
		Assert.assertEquals(json, writer.toString());
	}

	@Test
	public void testWriteEmptyList() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		
		List<SampleRootElement> list = new ArrayList<SampleRootElement>();

		StringWriter writer = new StringWriter();
		provider.write(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, writer, list);

		String json = "[]";
		Assert.assertEquals(json, writer.toString());
	}

	@Test
	public void testWriteEmptyListWithVirtualRoot() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[]{JsonXMLVirtualSampleRootElement.class.getAnnotation(JsonXML.class)};
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		
		List<SampleRootElement> list = new ArrayList<SampleRootElement>();

		StringWriter writer = new StringWriter();
		provider.write(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, writer, list);

		String json = "[]";
		Assert.assertEquals(json, writer.toString());
	}
}
