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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import de.odysseus.staxon.json.jaxb.JsonXML;
import de.odysseus.staxon.json.jaxb.JsonXMLBinder;

abstract class AbstractJsonXMLProvider extends JsonXMLBinder implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
	protected static <A extends Annotation> A getAnnotation(Annotation[] annotations, Class<A> annotationType) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType() == annotationType) {
				return annotationType.cast(annotation);
			}
		}
		return null;
	}
	
	private final JsonXMLContextStore store;
	
	public AbstractJsonXMLProvider(Providers providers) {
		super(true);
		this.store = new JsonXMLContextStore(providers);
	}

	protected JsonXML getJsonXML(Class<?> type, Annotation[] resourceAnnotations) {
		JsonXML result = getAnnotation(resourceAnnotations, JsonXML.class);
		if (result == null) {
			result = type.getAnnotation(JsonXML.class);
		}
		return result;
	}
	
	protected boolean isSupported(MediaType mediaType) {
		return "json".equalsIgnoreCase(mediaType.getSubtype()) || mediaType.getSubtype().endsWith("+json");
	}
	
	protected String getCharset(MediaType mediaType) {	
		Map<String, String> parameters = mediaType.getParameters();
		return parameters.containsKey("charset") ? parameters.get("charset") : "UTF-8";
	}

	protected JAXBContext getContext(Class<?> type, MediaType mediaType) throws JAXBException {
		return store.getContext(type, mediaType);
	}
	
	protected abstract boolean isReadWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType);

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return isReadWriteable(type, genericType, annotations, mediaType);
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return isReadWriteable(type, genericType, annotations, mediaType);
	}

	@Override
	public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	public abstract Object read(
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders,
			Reader entityStream) throws IOException, WebApplicationException;

	@Override
	public final Object readFrom(
			Class<Object> type, // <-- how sad...
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException, WebApplicationException {
		Reader reader = new InputStreamReader(entityStream, getCharset(mediaType));
		return read(type, genericType, annotations, mediaType, httpHeaders, reader);
	}
	
	public abstract void write(
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			Writer entityStream,
			Object entry) throws IOException, WebApplicationException;

	@Override
	public final void writeTo(
			Object entry,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException {
		Writer writer = new OutputStreamWriter(entityStream, getCharset(mediaType));
		write(type, genericType, annotations, mediaType, httpHeaders, writer, entry);
	}
}
