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
package de.odysseus.staxon.json.jaxb;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.json.JsonXMLStreamConstants;
import de.odysseus.staxon.json.util.XMLMultipleStreamWriter;

/**
 * Read/write instances of JAXB-annotated classes from/to JSON.
 */
public class JsonXMLBinder {
	private final boolean writeDocumentArray;
	
	public JsonXMLBinder() {
		this(true);
	}

	protected JsonXMLBinder(boolean writeDocumentArray) {
		this.writeDocumentArray = writeDocumentArray;
	}
	
	protected JsonXMLInputFactory createInputFactory(JsonXML config) {
		JsonXMLInputFactory factory = new JsonXMLInputFactory();
		factory.setProperty(JsonXMLInputFactory.PROP_MULTIPLE_PI, true);
		factory.setProperty(JsonXMLInputFactory.PROP_VIRTUAL_ROOT, config.virtualRoot().isEmpty() ? null : config.virtualRoot());
		factory.setProperty(JsonXMLInputFactory.PROP_NAMESPACE_SEPARATOR, config.namespaceSeparator());
		return factory;
	}
	
	protected JsonXMLOutputFactory createOutputFactory(JsonXML config) {
		JsonXMLOutputFactory factory = new JsonXMLOutputFactory();
		factory.setProperty(JsonXMLOutputFactory.PROP_MULTIPLE_PI, true);
		factory.setProperty(JsonXMLOutputFactory.PROP_AUTO_ARRAY, config.autoArray());
		factory.setProperty(JsonXMLOutputFactory.PROP_PRETTY_PRINT, config.prettyPrint());
		factory.setProperty(JsonXMLOutputFactory.PROP_VIRTUAL_ROOT, config.virtualRoot().isEmpty() ? null : config.virtualRoot());
		factory.setProperty(JsonXMLOutputFactory.PROP_NAMESPACE_SEPARATOR, config.namespaceSeparator());
		factory.setProperty(JsonXMLOutputFactory.PROP_NAMESPACE_DECLARATIONS, config.namespaceDeclarations());
		return factory;
	}
	
	public boolean isBindable(Class<?> type) {
		return type.isAnnotationPresent(XmlRootElement.class) || type.isAnnotationPresent(XmlType.class);
	}
	
	private void checkBindable(Class<?> type) throws JAXBException {
		if (!isBindable(type)) {
			throw new JAXBException("Cannot bind type: " + type.getName());
		}
	}
	
	protected <T> T unmarshal(Class<? extends T> type, Unmarshaller unmarshaller, XMLStreamReader reader) throws JAXBException, XMLStreamException {
		if (type.isAnnotationPresent(XmlRootElement.class)) {
			return type.cast(unmarshaller.unmarshal(reader));
		} else if (type.isAnnotationPresent(XmlType.class)) {
			return unmarshaller.unmarshal(reader, type).getValue();
		} else { // good luck
			return type.cast(unmarshaller.unmarshal(reader, type));
		}
	}
	
	protected String getNamespaceURI(XmlType xmlType, XmlSchema xmlSchema) {
		if ("##default".equals(xmlType.namespace())) {
			return xmlSchema == null ? XMLConstants.NULL_NS_URI : xmlSchema.namespace();
		} else {
			return xmlType.namespace();
		}
	}

	protected String getNamespaceURI(XmlElementDecl xmlElementDecl, XmlSchema xmlSchema) {
		if ("##default".equals(xmlElementDecl.namespace())) {
			return xmlSchema == null ? XMLConstants.NULL_NS_URI : xmlSchema.namespace();
		} else {
			return xmlElementDecl.namespace();
		}
	}

	protected JAXBElement<?> createJAXBElement(Class<?> type, String localName, Object value) throws JAXBException {
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
		XmlSchema xmlSchema = type.getPackage().getAnnotation(XmlSchema.class);
		String namespaceURI = getNamespaceURI(xmlType, xmlSchema);
		for (Method method : factoryClass.getDeclaredMethods()) {
			XmlElementDecl xmlElementDecl = method.getAnnotation(XmlElementDecl.class);
			if (xmlElementDecl != null && namespaceURI.equals(getNamespaceURI(xmlElementDecl, xmlSchema))) {
				if (localName == null || localName.equals(xmlElementDecl.name())) {
					if (method.getReturnType() == JAXBElement.class) {
						Class<?>[] parameterTypes = method.getParameterTypes();
						if (parameterTypes.length == 1 && parameterTypes[0] == type) {
							try {
								return (JAXBElement<?>)method.invoke(factoryClass.newInstance(), value);
							} catch (Exception e) {
								throw new JAXBException("Cannot create JAXBElement", e);
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	protected void marshal(Class<?> type, Marshaller marshaller, XMLStreamWriter writer, String virtualRoot, Object value)
			throws JAXBException, XMLStreamException {
		Object element = null;
		if (type.isAnnotationPresent(XmlRootElement.class)) {
			element = value;
		} else if (type.isAnnotationPresent(XmlType.class)) {
			/*
			 * determine expected localName
			 */
			String localPart = null;
			if (virtualRoot != null) {
				int colon = virtualRoot.indexOf(':');
				if (colon > 0) {
					localPart = virtualRoot.substring(colon + 1);
				} else {
					localPart = virtualRoot;
				}
			}
			/*
			 * create JAXBElement
			 */
			element = createJAXBElement(type, localPart, value);
			if (element == null) {
				throw new JAXBException("Cannot create JAXBElement");
			}
		} else { // good luck...
			element = value;
		}
		marshaller.marshal(element, writer);
	}

	public <T> T readObject(Class<? extends T> type, JsonXML config, JAXBContext context, Reader stream)
			throws XMLStreamException, JAXBException {
		checkBindable(type);
		XMLStreamReader reader = createInputFactory(config).createXMLStreamReader(stream);
		T result;
		if (reader.isCharacters() && reader.getText() == null) { // hack: read null
			result = null;
		} else {
			reader.require(XMLStreamConstants.START_DOCUMENT, null, null);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			result = unmarshal(type, unmarshaller, reader);
			reader.require(XMLStreamConstants.END_DOCUMENT, null, null);
		}
		reader.close();
		return result;
	}

	public void writeObject(Class<?> type, JsonXML config, JAXBContext context, Writer stream, Object value)
			throws XMLStreamException, JAXBException {
		checkBindable(type);
		XMLStreamWriter writer = createOutputFactory(config).createXMLStreamWriter(stream);
		if (value == null) { // hack: write null
			writer.writeCharacters(null);
		} else {
			if (config.multiplePaths().length > 0) {
				writer = new XMLMultipleStreamWriter(writer, config.multiplePaths());
			}
			Marshaller marshaller = context.createMarshaller();
			marshal(type, marshaller, writer, config.virtualRoot().length() > 0 ? config.virtualRoot() : null, value);
		}
		writer.close();
	}
	
	public <T> List<T> readArray(Class<? extends T> type, JsonXML config, JAXBContext context, Reader stream)
			throws XMLStreamException, JAXBException {
		checkBindable(type);
		XMLStreamReader reader = createInputFactory(config).createXMLStreamReader(stream);
		List<T> result;
		if (reader.isCharacters() && reader.getText() == null) { // hack: read null
			result = null;
		} else {
			boolean documentArray = JsonXMLStreamConstants.MULTIPLE_PI_TARGET.equals(reader.getPITarget());
			Unmarshaller unmarshaller = context.createUnmarshaller();
			while (reader.hasNext() && !reader.isStartElement() && !reader.isCharacters()) {
				reader.next();
			}
			result = new ArrayList<T>();
			while (reader.hasNext() || reader.isCharacters() && reader.getText() == null) {
				if (reader.isCharacters() && reader.getText() == null) { // hack: read null
					result.add(null);
					if (reader.hasNext()) {
						reader.next();
					} else {
						break;
					}
				} else {
					result.add(unmarshal(type, unmarshaller, reader));
					if (documentArray && reader.hasNext()) { // move to next document
						reader.next();
					}
				}
			}
		}
		reader.close();
		return result;
	}

	public void writeArray(Class<?> type, JsonXML config, JAXBContext context, Writer stream, Collection<?> collection)
			throws XMLStreamException, JAXBException {
		checkBindable(type);
		XMLStreamWriter writer = createOutputFactory(config).createXMLStreamWriter(stream);
		if (collection == null) { // hack: write null
			writer.writeCharacters(null);
		} else {
			if (config.multiplePaths().length > 0) {
				writer = new XMLMultipleStreamWriter(writer, config.multiplePaths());
			}
			Marshaller marshaller = context.createMarshaller();
			if (writeDocumentArray) {
				writer.writeProcessingInstruction(JsonXMLStreamConstants.MULTIPLE_PI_TARGET);
			} else {
				writer.writeStartDocument();
				if (config.virtualRoot().length() > 0) {
					writer.writeProcessingInstruction(JsonXMLStreamConstants.MULTIPLE_PI_TARGET, config.virtualRoot());
				} else {
					writer.writeProcessingInstruction(JsonXMLStreamConstants.MULTIPLE_PI_TARGET);
				}
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
			}
			for (Object value : collection) {
				if (value == null) { // hack: write null
					writer.writeCharacters(null);
				} else {							
					marshal(type, marshaller, writer, config.virtualRoot().length() > 0 ? config.virtualRoot() : null, value);
				}
			}
			if (!writeDocumentArray) {
				writer.writeEndDocument();
			}
		}
		writer.close();
	}
}
