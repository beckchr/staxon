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
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

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

import de.odysseus.staxon.json.jaxb.JsonXMLConfig;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonXMLArrayProvider extends AbstractJsonXMLProvider {
	public JsonXMLArrayProvider(@Context Providers providers) {
		super(providers);
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

	protected Collection<Object> createDefaultCollection(Class<?> type) {
		if (type.isAssignableFrom(ArrayList.class)) {
			return new ArrayList<Object>();
		} else if (type.isAssignableFrom(LinkedList.class)) {
			return new LinkedList<Object>();
		} if (type.isAssignableFrom(HashSet.class)) {
			return new HashSet<Object>();
		} else if (type.isAssignableFrom(TreeSet.class)) {
			return new TreeSet<Object>();
		} else {
			return null;
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
	protected boolean isReadWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		if (!isSupported(mediaType)) {
			return false;
		}
		Class<?> componentType = getComponentType(type, genericType);
		return componentType != null && getJsonXMLConfig(componentType, annotations, mediaType) != null && isBindable(componentType);
	}

	@Override
	public Object read(
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders,
			Reader stream) throws IOException, WebApplicationException {
		Class<?> componentType = getComponentType(type, genericType);
		JsonXMLConfig config = getJsonXMLConfig(componentType, annotations, mediaType);	
		List<?> list;
		try {
			list = readArray(componentType, config, getContext(componentType, mediaType), stream);
		} catch (XMLStreamException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (JAXBException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
		if (list == null) {
			return null;
		} else if (type.isArray()) {
			return toArray(list, componentType);
		} else {
			Collection<Object> collection = createCollection(type);
			if (collection == null) {
				throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
			}
			collection.addAll(list);
			return collection;
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
			Object entry) throws IOException, WebApplicationException {
		Class<?> componentType = getComponentType(type, genericType);
		JsonXMLConfig config = getJsonXMLConfig(componentType, annotations, mediaType);
		Collection<?> collection;
		if (entry == null) {
			collection = null;
		} else if (type.isArray()) {
			collection = Arrays.asList((Object[]) entry);
		} else {
			collection = (Collection<?>) entry;
		}
		try {
			writeArray(componentType, config, getContext(componentType, mediaType), stream, collection);
		} catch (XMLStreamException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (JAXBException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
	}
}