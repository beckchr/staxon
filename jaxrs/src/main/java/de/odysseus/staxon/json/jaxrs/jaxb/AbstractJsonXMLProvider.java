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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.json.jaxrs.JsonXML;

abstract class AbstractJsonXMLProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {
	protected static <A extends Annotation> A getAnnotation(Annotation[] annotations, Class<A> annotationType) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType() == annotationType) {
				return annotationType.cast(annotation);
			}
		}
		return null;
	}

	protected static JsonXML getConfig(Annotation[] annotations) {
		return getAnnotation(annotations, JsonXML.class);
	}
	
	protected static boolean isJson(MediaType mediaType) {
		return "json".equalsIgnoreCase(mediaType.getSubtype()) || mediaType.getSubtype().endsWith("+json");
	}

	protected static JsonXMLInputFactory createInputFactory(JsonXML config) {
		JsonXMLInputFactory factory = new JsonXMLInputFactory();
		factory.setProperty(JsonXMLInputFactory.PROP_MULTIPLE_PI, !config.autoArray());
		factory.setProperty(JsonXMLInputFactory.PROP_VIRTUAL_ROOT, config.virtualRoot().isEmpty() ? null : config.virtualRoot());
		factory.setProperty(JsonXMLInputFactory.PROP_NAMESPACE_SEPARATOR, config.namespaceSeparator());
		return factory;
	}
	
	protected static JsonXMLOutputFactory createOutputFactory(JsonXML config) {
		JsonXMLOutputFactory factory = new JsonXMLOutputFactory();
		factory.setProperty(JsonXMLOutputFactory.PROP_MULTIPLE_PI, !config.autoArray());
		factory.setProperty(JsonXMLOutputFactory.PROP_AUTO_ARRAY, config.autoArray());
		factory.setProperty(JsonXMLOutputFactory.PROP_PRETTY_PRINT, config.prettyPrint());
		factory.setProperty(JsonXMLOutputFactory.PROP_VIRTUAL_ROOT, config.virtualRoot().isEmpty() ? null : config.virtualRoot());
		factory.setProperty(JsonXMLOutputFactory.PROP_NAMESPACE_SEPARATOR, config.namespaceSeparator());
		factory.setProperty(JsonXMLOutputFactory.PROP_NAMESPACE_DECLARATIONS, config.namespaceDeclarations());
		return factory;
	}
	
	@Override
	public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}
}
