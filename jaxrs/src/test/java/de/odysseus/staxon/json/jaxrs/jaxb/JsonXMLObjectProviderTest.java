package de.odysseus.staxon.json.jaxrs.jaxb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.junit.Test;

import de.odysseus.staxon.json.jaxrs.JsonXML;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleRootElement;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleType;

public class JsonXMLObjectProviderTest {
	@JsonXML
	static class JsonXMLDefault {}

	@JsonXML(virtualRoot = "sampleRootElement")
	static class JsonXMLVirtualSampleRootElement {}

	@Test
	public void testIsWritable() {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		Annotation[] jsonXMLAnnotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};

		Assert.assertTrue(provider.isWriteable(SampleRootElement.class, null, jsonXMLAnnotations, MediaType.APPLICATION_JSON_TYPE));
		Assert.assertTrue(provider.isWriteable(SampleType.class, null, jsonXMLAnnotations, MediaType.APPLICATION_JSON_TYPE));
		Assert.assertFalse(provider.isWriteable(Object.class, null, jsonXMLAnnotations, MediaType.APPLICATION_JSON_TYPE));
		Assert.assertFalse(provider.isWriteable(SampleType.class, null, new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
		Assert.assertFalse(provider.isWriteable(SampleRootElement.class, null, jsonXMLAnnotations, MediaType.APPLICATION_XML_TYPE));
	}

	@Test
	public void testIsReadable() {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		Annotation[] jsonXMLAnnotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};

		Assert.assertTrue(provider.isReadable(SampleRootElement.class, null, jsonXMLAnnotations, MediaType.APPLICATION_JSON_TYPE));
		Assert.assertTrue(provider.isReadable(SampleType.class, null, jsonXMLAnnotations, MediaType.APPLICATION_JSON_TYPE));
		Assert.assertFalse(provider.isReadable(Object.class, null, jsonXMLAnnotations, MediaType.APPLICATION_JSON_TYPE));
		Assert.assertFalse(provider.isReadable(SampleType.class, null, new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
		Assert.assertFalse(provider.isReadable(SampleRootElement.class, null, jsonXMLAnnotations, MediaType.APPLICATION_XML_TYPE));
	}

	@Test
	public void testReadSampleRootElement() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[0];
		String json = "{\"sampleRootElement\":{\"@attribute\":\"hello\",\"elements\":[\"world\"]}}";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));

		SampleRootElement sampleRootElement = (SampleRootElement)provider.read(SampleRootElement.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, input);

		Assert.assertEquals("hello", sampleRootElement.attribute);
		Assert.assertEquals("world", sampleRootElement.elements.get(0));
	}

	@Test
	public void testReadSampleType() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};
		String json = "{\"sampleType\":{\"element\":\"hi!\"}}";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));		

		SampleType sampleType = (SampleType)provider.read(SampleType.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, input);

		Assert.assertEquals("hi!", sampleType.element);
	}

	@Test
	public void testWriteSampleRootElement() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[0];
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		SampleRootElement sampleRootElement = new SampleRootElement();
		sampleRootElement.attribute = "hello";
		sampleRootElement.elements = Arrays.asList("world");	

		provider.write(SampleRootElement.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, output, sampleRootElement);

		String json = "{\"sampleRootElement\":{\"@attribute\":\"hello\",\"elements\":\"world\"}}";
		Assert.assertEquals(json, new String(output.toByteArray(), encoding));
	}

	@Test
	public void testWriteSampleType() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		SampleType sampleType = new SampleType();
		sampleType.element = "hi!";

		provider.write(SampleType.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, output, sampleType);

		String json = "{\"sampleType\":{\"element\":\"hi!\"}}";
		Assert.assertEquals(json, new String(output.toByteArray(), encoding));
	}
	
	@Test
	public void testReadNull() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[0];
		String json = "null";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));

		SampleRootElement sampleRootElement = (SampleRootElement)provider.read(SampleRootElement.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, input);

		Assert.assertNull(sampleRootElement);
	}

	@Test
	public void testReadNullWithVirtualRoot() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[]{JsonXMLVirtualSampleRootElement.class.getAnnotation(JsonXML.class)};
		String json = "null";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));

		SampleRootElement sampleRootElement = (SampleRootElement)provider.read(SampleRootElement.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, input);

		Assert.assertNotNull(sampleRootElement);
	}

	@Test
	public void testWriteNull() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[0];
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		provider.write(SampleRootElement.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, output, null);

		String json = "null";
		Assert.assertEquals(json, new String(output.toByteArray(), encoding));
	}


	@Test
	public void testWriteNullWithVirtualRoot() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);
		Annotation[] annotations = new Annotation[]{JsonXMLVirtualSampleRootElement.class.getAnnotation(JsonXML.class)};
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		provider.write(SampleRootElement.class,
				null, annotations, MediaType.APPLICATION_JSON_TYPE, null, output, null);

		String json = "null";
		Assert.assertEquals(json, new String(output.toByteArray(), encoding));
	}
}
