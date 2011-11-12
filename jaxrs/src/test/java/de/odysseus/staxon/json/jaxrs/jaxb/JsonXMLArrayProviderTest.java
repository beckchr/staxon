package de.odysseus.staxon.json.jaxrs.jaxb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.junit.Test;

import de.odysseus.staxon.json.jaxrs.JsonXML;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleRootElement;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleType;

public class JsonXMLArrayProviderTest {
	@JsonXML
	static class JsonXMLDefault {}

	@JsonXML(virtualRoot = "sampleRootElement")
	static class JsonXMLVirtualSampleRootElement {}

	static List<SampleRootElement> sampleRootElementList = new ArrayList<SampleRootElement>();
	static List<SampleType> sampleTypeList = new ArrayList<SampleType>();
	static List<Object> objectList = new ArrayList<Object>();
	static Iterable<SampleType> sampleTypeIterable = sampleTypeList;
	static SampleRootElement[] sampleRootELementArray = new SampleRootElement[0];
	static SampleType[] sampleTypeArray = new SampleType[0];
	static Object[] objectArray = new Object[0];
	
	@Test
	public void testIsWritable() throws NoSuchFieldException {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};

		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		Assert.assertTrue(provider.isWriteable(List.class, type, annotations, MediaType.APPLICATION_JSON_TYPE));
		type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		Assert.assertTrue(provider.isWriteable(List.class, type, annotations, MediaType.APPLICATION_JSON_TYPE));
		type = getClass().getDeclaredField("objectList").getGenericType();
		Assert.assertFalse(provider.isWriteable(List.class, type, annotations, MediaType.APPLICATION_JSON_TYPE));
		type = getClass().getDeclaredField("sampleTypeIterable").getGenericType();
		Assert.assertFalse(provider.isWriteable(Iterable.class, type, annotations, MediaType.APPLICATION_JSON_TYPE));
		type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		Assert.assertFalse(provider.isWriteable(List.class, type, new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
		Assert.assertFalse(provider.isWriteable(List.class, type, annotations, MediaType.APPLICATION_XML_TYPE));
	}

	@Test
	public void testIsReadable() throws NoSuchFieldException {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};

		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		Assert.assertTrue(provider.isReadable(List.class, type, annotations, MediaType.APPLICATION_JSON_TYPE));
		type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		Assert.assertTrue(provider.isReadable(List.class, type, annotations, MediaType.APPLICATION_JSON_TYPE));
		type = getClass().getDeclaredField("objectList").getGenericType();
		Assert.assertFalse(provider.isReadable(List.class, type, annotations, MediaType.APPLICATION_JSON_TYPE));
		type = getClass().getDeclaredField("sampleTypeIterable").getGenericType();
		Assert.assertFalse(provider.isReadable(Iterable.class, type, annotations, MediaType.APPLICATION_JSON_TYPE));
		type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		Assert.assertFalse(provider.isReadable(List.class, type, new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
		Assert.assertFalse(provider.isReadable(List.class, type, annotations, MediaType.APPLICATION_XML_TYPE));
	}

	@Test
	public void testReadRootElementList() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		String json = "{\"sampleRootElement\":[{\"@attribute\":\"hello\"},{\"@attribute\":\"world\"}]}";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));

		@SuppressWarnings("unchecked")
		List<SampleRootElement> list = (List<SampleRootElement>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, input);

		Assert.assertEquals(2, list.size());
		Assert.assertEquals("hello", list.get(0).attribute);
		Assert.assertEquals("world", list.get(1).attribute);
	}

	@Test
	public void testReadRootElementList_DocumentArray() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		String json = "[{\"sampleRootElement\":{\"@attribute\":\"hello\"}},{\"sampleRootElement\":{\"@attribute\":\"world\"}}]";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));

		@SuppressWarnings("unchecked")
		List<SampleRootElement> list = (List<SampleRootElement>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, input);

		Assert.assertEquals(2, list.size());
		Assert.assertEquals("hello", list.get(0).attribute);
		Assert.assertEquals("world", list.get(1).attribute);
	}

	@Test
	public void testReadSampleTypeList() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};
		Type type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		String json = "{\"sampleType\":[{\"element\":\"hello\"},{\"element\":\"world\"}]}";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));

		@SuppressWarnings("unchecked")
		List<SampleType> list = (List<SampleType>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, input);

		Assert.assertEquals(2, list.size());
		Assert.assertEquals("hello", list.get(0).element);
		Assert.assertEquals("world", list.get(1).element);
	}

	@Test
	public void testReadSampleTypeList_DocumentArray() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};
		Type type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		String json = "[{\"sampleType\":{\"element\":\"hello\"}},{\"sampleType\":{\"element\":\"world\"}}]";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));

		@SuppressWarnings("unchecked")
		List<SampleType> list = (List<SampleType>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, input);

		Assert.assertEquals(2, list.size());
		Assert.assertEquals("hello", list.get(0).element);
		Assert.assertEquals("world", list.get(1).element);
	}

	@Test
	public void testReadSampleTypeListWithNullValues() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};
		Type type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		String json = "[null,{\"sampleType\":{\"element\":\"hi!\"}},null]";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));

		@SuppressWarnings("unchecked")
		List<SampleType> list = (List<SampleType>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, input);

		Assert.assertEquals(3, list.size());
		Assert.assertNull(list.get(0));
		Assert.assertEquals("hi!", list.get(1).element);
		Assert.assertNull(list.get(2));
	}

	@Test
	public void testReadEmptyList() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		String json = "{}";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));

		@SuppressWarnings("unchecked")
		List<SampleRootElement> list = (List<SampleRootElement>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, input);

		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testReadEmptyList2() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		String json = "{\"sampleRootElement\":[]}";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));

		@SuppressWarnings("unchecked")
		List<SampleRootElement> list = (List<SampleRootElement>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, input);

		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testReadEmptyList3() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		String json = "[]";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));

		@SuppressWarnings("unchecked")
		List<SampleRootElement> list = (List<SampleRootElement>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, input);

		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testReadEmptyListWithVirtualRoot() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[]{JsonXMLVirtualSampleRootElement.class.getAnnotation(JsonXML.class)};
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		String json = "[]";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));

		@SuppressWarnings("unchecked")
		List<SampleRootElement> list = (List<SampleRootElement>)provider.read(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, input);

		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testWriteSampleRootElementList() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		List<SampleRootElement> list = new ArrayList<SampleRootElement>();
		list.add(new SampleRootElement());
		list.get(0).attribute = "hello";
		list.add(new SampleRootElement());
		list.get(1).attribute = "world";

		provider.write(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, output, list);

		String json = "{\"sampleRootElement\":[{\"@attribute\":\"hello\"},{\"@attribute\":\"world\"}]}";
		Assert.assertEquals(json, new String(output.toByteArray(), encoding));
	}

	@Test
	public void testWriteSampleTypeList() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};
		Type type = getClass().getDeclaredField("sampleTypeList").getGenericType();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		List<SampleType> list = new ArrayList<SampleType>();
		list.add(new SampleType());
		list.get(0).element = "hello";
		list.add(new SampleType());
		list.get(1).element = "world";

		provider.write(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, output, list);

		String json = "{\"sampleType\":[{\"element\":\"hello\"},{\"element\":\"world\"}]}";
		Assert.assertEquals(json, new String(output.toByteArray(), encoding));
	}

	@Test
	public void testWriteEmptyList() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[0];
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		List<SampleRootElement> list = new ArrayList<SampleRootElement>();

		provider.write(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, output, list);

		String json = "{}";
		Assert.assertEquals(json, new String(output.toByteArray(), encoding));
	}

	@Test
	public void testWriteEmptyListWithVirtualRoot() throws Exception {
		JsonXMLArrayProvider provider = new JsonXMLArrayProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[]{JsonXMLVirtualSampleRootElement.class.getAnnotation(JsonXML.class)};
		Type type = getClass().getDeclaredField("sampleRootElementList").getGenericType();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		List<SampleRootElement> list = new ArrayList<SampleRootElement>();

		provider.write(List.class,
				type, annotations, MediaType.APPLICATION_JSON_TYPE, null, output, list);

		String json = "[]";
		Assert.assertEquals(json, new String(output.toByteArray(), encoding));
	}
}
