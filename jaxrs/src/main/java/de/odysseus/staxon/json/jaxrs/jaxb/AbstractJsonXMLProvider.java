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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

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

	protected JsonXMLInputFactory createInputFactory(JsonXML config) {
		JsonXMLInputFactory factory = new JsonXMLInputFactory();
		factory.setProperty(JsonXMLInputFactory.PROP_MULTIPLE_PI, !config.autoArray());
		factory.setProperty(JsonXMLInputFactory.PROP_VIRTUAL_ROOT, config.virtualRoot().isEmpty() ? null : config.virtualRoot());
		factory.setProperty(JsonXMLInputFactory.PROP_NAMESPACE_SEPARATOR, config.namespaceSeparator());
		return factory;
	}
	
	protected JsonXMLOutputFactory createOutputFactory(JsonXML config) {
		JsonXMLOutputFactory factory = new JsonXMLOutputFactory();
		factory.setProperty(JsonXMLOutputFactory.PROP_MULTIPLE_PI, !config.autoArray());
		factory.setProperty(JsonXMLOutputFactory.PROP_AUTO_ARRAY, config.autoArray());
		factory.setProperty(JsonXMLOutputFactory.PROP_PRETTY_PRINT, config.prettyPrint());
		factory.setProperty(JsonXMLOutputFactory.PROP_VIRTUAL_ROOT, config.virtualRoot().isEmpty() ? null : config.virtualRoot());
		factory.setProperty(JsonXMLOutputFactory.PROP_NAMESPACE_SEPARATOR, config.namespaceSeparator());
		factory.setProperty(JsonXMLOutputFactory.PROP_NAMESPACE_DECLARATIONS, config.namespaceDeclarations());
		return factory;
	}
	
	protected boolean isMappable(Class<?> type) {
		return type.isAnnotationPresent(XmlRootElement.class) || type.isAnnotationPresent(XmlType.class);
	}

	protected String getEncoding(MediaType mediaType) {	
		Map<String, String> parameters = mediaType.getParameters();
		return parameters.containsKey("charset") ? parameters.get("charset") : "UTF-8";
	}
	
	protected Object unmarshal(Class<?> type, JsonXML config, Unmarshaller unmarshaller, XMLStreamReader reader) throws JAXBException, XMLStreamException {
		if (type.isAnnotationPresent(XmlRootElement.class)) {
			return unmarshaller.unmarshal(reader);
		} else if (type.isAnnotationPresent(XmlType.class)) {
			return unmarshaller.unmarshal(reader, type).getValue();
		} else {
			return unmarshaller.unmarshal(reader, type);
		}
	}
	
	protected JAXBElement<?> createJAXBElement(Class<?> type, String namespaceURI, String localName, Object value) {
		XmlType xmlType = type.getAnnotation(XmlType.class);
		if (xmlType == null) {
			return null;
		}
		Class<?> factoryClass = xmlType.factoryClass();
		if (factoryClass == XmlType.DEFAULT.class) {
			String defaultObjectFactoryName = type.getPackage().getName() + ".ObjectFactory";
			try {
				factoryClass = Thread.currentThread().getContextClassLoader().loadClass(defaultObjectFactoryName);
			} catch (Exception e) {
				factoryClass = type;
			}
		}
		if (factoryClass.getAnnotation(XmlRegistry.class) == null) {
			return null;
		}
		for (Method method : factoryClass.getDeclaredMethods()) {
			XmlElementDecl xmlElementDecl = method.getAnnotation(XmlElementDecl.class);
			if (xmlElementDecl != null) {
				if (localName == null || localName.equals(xmlElementDecl.name())) {
					if (namespaceURI == null || namespaceURI.equals(xmlElementDecl.namespace())) {
						if (method.getReturnType() == JAXBElement.class) {
							Class<?>[] parameterTypes = method.getParameterTypes();
							if (parameterTypes.length == 1 && parameterTypes[0] == type) {
								try {
									return (JAXBElement<?>)method.invoke(factoryClass.newInstance(), value);
								} catch (Exception e) {
									return null;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	protected void marshal(Class<?> type, JsonXML config, Marshaller marshaller, XMLStreamWriter writer, Object value) throws JAXBException, XMLStreamException {
		Object element = null;
		if (type.isAnnotationPresent(XmlRootElement.class)) {
			element = value;
		} else if (type.isAnnotationPresent(XmlType.class)) {
			XmlType annotation = type.getAnnotation(XmlType.class);
			/*
			 * determine expected localName
			 */
			String localPart = null;
			if (config.virtualRoot().length() > 0) {
				int colon = config.virtualRoot().indexOf(':');
				if (colon > 0) {
					localPart = config.virtualRoot().substring(colon + 1);
				} else {
					localPart = config.virtualRoot();
				}
			}
			/*
			 * determine expected namespace URI
			 */
			String namespaceURI = null;
			if ("##default".equals(annotation.namespace())) {
				XmlSchema schema = type.getPackage().getAnnotation(XmlSchema.class);
				if (schema != null) {
					namespaceURI = schema.namespace();
				}
			} else {
				namespaceURI = annotation.namespace();
			}
			/*
			 * create JAXBElement
			 */
			element = createJAXBElement(type, namespaceURI, localPart, value);
			if (element == null) {
				throw new JAXBException("Cannot create JAXBElement");
			}
		} else { // good luck...
			element = value;
		}
		marshaller.marshal(element, writer);
	}
	
	@Override
	public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}
}
