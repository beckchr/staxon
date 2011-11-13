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
import java.io.Reader;
import java.io.Writer;
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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import de.odysseus.staxon.json.jaxrs.JsonXML;
import de.odysseus.staxon.json.util.XMLMultipleStreamWriter;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonXMLObjectProvider extends AbstractJsonXMLProvider {
	private final JsonXMLContextStore store;
	
	public JsonXMLObjectProvider(@Context Providers providers) {
		this.store = new JsonXMLContextStore(providers);
	}

	@Override
	protected boolean isReadWritable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return isSupported(mediaType) && getJsonXML(type, annotations) != null && isMappable(type);
	}

	@Override
	public Object read(
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders,
			Reader entityStream) throws IOException, WebApplicationException {
		JsonXML config = getJsonXML(type, annotations);
		XMLInputFactory factory = createInputFactory(config);
		try {
			JAXBContext context = store.getContext(type, mediaType);
			XMLStreamReader reader = factory.createXMLStreamReader(entityStream);
			Object result;
			if (reader.isCharacters() && reader.getText() == null) { // hack: read null
				result = null;
			} else {
				reader.require(XMLStreamConstants.START_DOCUMENT, null, null);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				result = unmarshal(type, config, unmarshaller, reader);
				reader.require(XMLStreamConstants.END_DOCUMENT, null, null);
			}
			reader.close();
			return result;
		} catch (XMLStreamException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (JAXBException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void write(
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			Writer entityStream,
			Object entry) throws IOException, WebApplicationException {
		JsonXML config = getJsonXML(type, annotations);
		XMLOutputFactory factory = createOutputFactory(config);
		try {
			JAXBContext context = store.getContext(type, mediaType);
			XMLStreamWriter writer = factory.createXMLStreamWriter(entityStream);
			if (entry == null) { // hack: write null
				writer.writeCharacters(null);
			} else {
				if (config.multiplePaths().length > 0) {
					writer = new XMLMultipleStreamWriter(writer, config.multiplePaths());
				}
				Marshaller marshaller = context.createMarshaller();
				marshal(type, config, marshaller, writer, entry);
			}
			writer.close();
		} catch (XMLStreamException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (JAXBException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
	}
}
