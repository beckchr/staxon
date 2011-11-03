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
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.json.JsonXMLStreamConstants;
import de.odysseus.staxon.json.jaxrs.JsonXML;
import de.odysseus.staxon.json.util.XMLMultipleStreamWriter;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonXMLArrayProvider extends AbstractJsonXMLProvider<Object> {
	private final JsonXMLContextStore store;

	public JsonXMLArrayProvider(@Context Providers providers) {
		this.store = new JsonXMLContextStore(providers);
	}

	protected Class<?> getRawType(Type type) {
		if (type instanceof Class<?>) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		} else if (type instanceof TypeVariable) {
			Type[] bounds = ((TypeVariable<?>) type).getBounds();
			if (bounds != null && bounds.length > 0) {
				return getRawType(bounds[0]); // first upper bound
			}
		}
		return null;
	}

	protected Class<?> getComponentType(Class<?> type, Type genericType) {
		if (Collection.class.isAssignableFrom(type)) {
			if (genericType instanceof ParameterizedType) {
				return getRawType(((ParameterizedType) genericType).getActualTypeArguments()[0]);
			} else if (genericType instanceof GenericArrayType) {
				return getRawType(((GenericArrayType) genericType).getGenericComponentType());
			}
			return null;
		} else if (type.isArray()) {
			return type.getComponentType();
		} else {
			return null;
		}
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		if (!isJson(mediaType) || getConfig(annotations) == null) {
			return false;
		}
		Class<?> componentType = getComponentType(type, genericType);
		return componentType != null && componentType.isAnnotationPresent(XmlRootElement.class);
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		if (!isJson(mediaType) || getConfig(annotations) == null) {
			return false;
		}
		Class<?> componentType = getComponentType(type, genericType);
		return componentType != null && componentType.isAnnotationPresent(XmlRootElement.class);
	}

	protected Collection<Object> createDefaultCollection(Class<?> type) {
		if (List.class.isAssignableFrom(type)) {
			return new ArrayList<Object>();
		} else if (Set.class.isAssignableFrom(type)) {
			return new HashSet<Object>();
		} else {
			return new ArrayList<Object>();
		}
	}

	@SuppressWarnings("unchecked")
	protected Collection<Object> createCollection(Class<?> type) {
		if (type.isInterface()) {
			return createDefaultCollection(type);
		} else {
			try {
				return (Collection<Object>) type.newInstance();
			} catch (Exception e) {
				return createDefaultCollection(type);
			}
		}
	}
	
	protected Object toArray(List<?> list, Class<?> componentType) {
		Object result = Array.newInstance(componentType, list.size());
		for (int index = 0; index < list.size(); index++) {
			Array.set(result, index++, list.get(index));
		}
		return result;
	}
	
	@Override
	public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
			WebApplicationException {
		Class<?> componentType = getComponentType(type, genericType);
		JsonXML config = getConfig(annotations);
		XMLInputFactory factory = createInputFactory(config);
		try {
			JAXBContext context = store.getContext(componentType, mediaType);
			XMLStreamReader reader = factory.createXMLStreamReader(entityStream);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			reader.require(XMLStreamConstants.START_DOCUMENT, null, null);
			reader.next();
			if (Boolean.TRUE.equals(factory.getProperty(JsonXMLInputFactory.PROP_MULTIPLE_PI))) {
				reader.require(XMLStreamConstants.PROCESSING_INSTRUCTION, null, null);
				if (!JsonXMLStreamConstants.MULTIPLE_PI_TARGET.equals(reader.getPITarget())) {
					throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
				}
				reader.next();
			}
			reader.require(XMLStreamConstants.START_ELEMENT, null, null);
			Collection<Object> collection = type.isArray() ? new ArrayList<Object>() : createCollection(componentType);
			while (reader.hasNext()) {
				collection.add(unmarshaller.unmarshal(reader));
			}
			if (type.isArray()) {
				return toArray((List<?>) collection, componentType);
			} else {
				return collection;
			}
		} catch (XMLStreamException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (JAXBException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void writeTo(Object entry, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
			WebApplicationException {
		Class<?> componentTypeType = getComponentType(type, genericType);
		JsonXML config = getConfig(annotations);
		XMLOutputFactory factory = createOutputFactory(config);
		try {
			JAXBContext context = store.getContext(componentTypeType, mediaType);
			XMLStreamWriter writer = factory.createXMLStreamWriter(entityStream);
			if (config.multiplePaths().length > 0) {
				writer = new XMLMultipleStreamWriter(writer, config.multiplePaths());
			}
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
			writer.writeStartDocument();
			if (Boolean.TRUE.equals(factory.getProperty(JsonXMLOutputFactory.PROP_MULTIPLE_PI))) {
				writer.writeProcessingInstruction(JsonXMLStreamConstants.MULTIPLE_PI_TARGET);
			}
			if (type.isArray()) {
				for (Object obj : (Object[]) entry) {
					marshaller.marshal(obj, writer);
				}
			} else {
				for (Object obj : (Collection<?>) entry)
					marshaller.marshal(obj, writer);
			}
			writer.writeEndDocument();
			writer.close();
		} catch (XMLStreamException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (JAXBException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
	}
}