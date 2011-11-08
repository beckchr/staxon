package de.odysseus.staxon.json.jaxrs.jaxb;

import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.junit.Test;

import de.odysseus.staxon.json.jaxrs.JsonXML;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleRootElement;
import de.odysseus.staxon.json.jaxrs.jaxb.model.SampleType;

public class JsonXMLObjectProviderTest {
	@JsonXML
	static class JsonXMLDefault {}

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
	public void testReadFrom() throws Exception {
		JsonXMLObjectProvider provider = new JsonXMLObjectProvider(null);
		String encoding = provider.getEncoding(MediaType.APPLICATION_JSON_TYPE);

		String json = "{\"sampleRootElement\":{\"@attribute\":\"hello\",\"elements\":[\"world\"]}}";
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(encoding));
		SampleRootElement sampleRootElement = (SampleRootElement)provider.readFrom(
				Object.class, null, new Annotation[0], MediaType.APPLICATION_JSON_TYPE, null, input);
		Assert.assertEquals("hello", sampleRootElement.attribute);
		Assert.assertEquals("world", sampleRootElement.elements.get(0));
		
		Annotation[] jsonXMLAnnotations = new Annotation[]{JsonXMLDefault.class.getAnnotation(JsonXML.class)};
		json = "{\"sampleType\":{\"element\":\"hi!\"}}";
		input = new ByteArrayInputStream(json.getBytes(encoding));
		SampleType sampleType = (SampleType)provider.readFrom(
				Object.class, null, jsonXMLAnnotations, MediaType.APPLICATION_JSON_TYPE, null, input);
		Assert.assertEquals("hi!", sampleType.element);
	}
}
