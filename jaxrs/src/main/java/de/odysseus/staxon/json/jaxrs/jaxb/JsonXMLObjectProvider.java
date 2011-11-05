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
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import de.odysseus.staxon.json.jaxrs.JsonXML;
import de.odysseus.staxon.json.util.XMLMultipleStreamWriter;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonXMLObjectProvider extends AbstractJsonXMLProvider<Object> {
	private final JsonXMLContextStore store;
	
	public JsonXMLObjectProvider(@Context Providers providers) {
		this.store = new JsonXMLContextStore(providers);
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return isJson(mediaType) && getConfig(type, annotations) != null
				&& (type.isAnnotationPresent(XmlRootElement.class) || type.isAnnotationPresent(XmlType.class));
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return isJson(mediaType) && getConfig(type, annotations) != null
				&& (type.isAnnotationPresent(XmlRootElement.class) || type.isAnnotationPresent(XmlType.class));
	}

	@Override
	public Object readFrom(
			Class<Object> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException, WebApplicationException {
		JsonXML config = getConfig(type, annotations);
		XMLInputFactory factory = createInputFactory(config);
		try {
			JAXBContext context = store.getContext(type, mediaType);
			XMLStreamReader reader = factory.createXMLStreamReader(entityStream);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			return unmarshal(unmarshaller, reader, type);
		} catch (XMLStreamException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (JAXBException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void writeTo(
			Object entry,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException {
		JsonXML config = getConfig(type, annotations);
		XMLOutputFactory factory = createOutputFactory(config);
		try {
			JAXBContext context = store.getContext(type, mediaType);
			XMLStreamWriter writer = factory.createXMLStreamWriter(entityStream);
			if (config.multiplePaths().length > 0) {
				writer = new XMLMultipleStreamWriter(writer, config.multiplePaths());
			}
			Marshaller marshaller = context.createMarshaller();
			marshal(marshaller, writer, entry);
		} catch (XMLStreamException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (JAXBException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
	}
}
