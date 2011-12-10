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
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import de.odysseus.staxon.json.jaxb.JsonXML;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonXMLObjectProvider extends AbstractJsonXMLProvider {
	public JsonXMLObjectProvider(@Context Providers providers) {
		super(providers);
	}

	@Override
	protected boolean isReadWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return isSupported(mediaType) && getJsonXML(type, annotations) != null && isBindable(type);
	}

	@Override
	public Object read(
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders,
			Reader stream) throws IOException, WebApplicationException {
		JsonXML config = getJsonXML(type, annotations);
		try {
			return readObject(type, config, getContext(type, mediaType), stream);
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
			Writer stream,
			Object value) throws IOException, WebApplicationException {
		JsonXML config = getJsonXML(type, annotations);
		try {
			writeObject(type, config, getContext(type, mediaType), stream, value);
		} catch (XMLStreamException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (JAXBException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
	}
}
