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
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
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
	
	protected JsonXMLInputFactory createInputFactory(Class<?> type, JsonXMLConfig config) {
		JsonXMLInputFactory factory = new JsonXMLInputFactory();
		factory.setProperty(JsonXMLInputFactory.PROP_MULTIPLE_PI, true);
		factory.setProperty(JsonXMLInputFactory.PROP_NAMESPACE_SEPARATOR, config.getNamespaceSeparator());
		factory.setProperty(JsonXMLInputFactory.PROP_VIRTUAL_ROOT, config.isVirtualRoot() ? getName(type) : null);
		return factory;
	}
	
	protected XMLStreamReader createXMLStreamReader(Class<?> type, JsonXMLConfig config, Reader stream) throws XMLStreamException, JAXBException {
		return createInputFactory(type, config).createXMLStreamReader(stream);
	}
	
	protected JsonXMLOutputFactory createOutputFactory(Class<?> type, JsonXMLConfig config) {
		JsonXMLOutputFactory factory = new JsonXMLOutputFactory();
		factory.setProperty(JsonXMLOutputFactory.PROP_MULTIPLE_PI, true);
		factory.setProperty(JsonXMLOutputFactory.PROP_AUTO_ARRAY, config.isAutoArray());
		factory.setProperty(JsonXMLOutputFactory.PROP_PRETTY_PRINT, config.isPrettyPrint());
		factory.setProperty(JsonXMLOutputFactory.PROP_NAMESPACE_SEPARATOR, config.getNamespaceSeparator());
		factory.setProperty(JsonXMLOutputFactory.PROP_NAMESPACE_DECLARATIONS, config.isNamespaceDeclarations());
		factory.setProperty(JsonXMLOutputFactory.PROP_VIRTUAL_ROOT, config.isVirtualRoot() ? getName(type) : null);
		return factory;
	}

	protected XMLStreamWriter createXMLStreamWriter(Class<?> type, JsonXMLConfig config, Writer stream) throws XMLStreamException, JAXBException {
		XMLStreamWriter writer = createOutputFactory(type, config).createXMLStreamWriter(stream);
		String[] multiplePaths = config.getMultiplePaths();
		if (multiplePaths != null && multiplePaths.length > 0) {
			writer = new XMLMultipleStreamWriter(writer, false, multiplePaths);
		}
		return writer;
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

	protected String getNamespaceURI(XmlRootElement xmlRootElement, XmlSchema xmlSchema) {
		if ("##default".equals(xmlRootElement.namespace())) {
			return xmlSchema == null ? XMLConstants.NULL_NS_URI : xmlSchema.namespace();
		} else {
			return xmlRootElement.namespace();
		}
	}

	protected String getNamespaceURI(XmlElementDecl xmlElementDecl, XmlSchema xmlSchema) {
		if ("##default".equals(xmlElementDecl.namespace())) {
			return xmlSchema == null ? XMLConstants.NULL_NS_URI : xmlSchema.namespace();
		} else {
			return xmlElementDecl.namespace();
		}
	}

	protected String getPrefix(String namespaceURI, XmlSchema xmlSchema) {
		if (xmlSchema != null) {
			for (XmlNs xmlns : xmlSchema.xmlns()) {
				if (xmlns.namespaceURI().equals(namespaceURI)) {
					return xmlns.prefix();
				}
			}
		}
		return XMLConstants.DEFAULT_NS_PREFIX;
	}

	/**
	 * Calculate root element name for an <code>@XmlRootElement</code>-annotated type.
	 * @param type
	 * @return element name
	 */
	protected QName getXmlRootElementName(Class<?> type) {
		XmlRootElement xmlRootElement = type.getAnnotation(XmlRootElement.class);
		if (xmlRootElement == null) {
			return null;
		}
		String localName;
		if ("##default".equals(xmlRootElement.name())) {
			localName = Character.toLowerCase(type.getSimpleName().charAt(0)) + type.getSimpleName().substring(1);
		} else {
			localName = xmlRootElement.name();
		}
		XmlSchema xmlSchema = type.getPackage().getAnnotation(XmlSchema.class);
		String namespaceURI = getNamespaceURI(xmlRootElement, xmlSchema);
		return new QName(namespaceURI, localName, getPrefix(namespaceURI, xmlSchema));
	}

	/**
	 * Calculate root element name for an <code>@XmlType</code>-annotated type.
	 * @param type
	 * @return element name
	 */
	protected QName getXmlTypeName(Class<?> type) {
		Method method = getXmlElementDeclMethod(type);
		if (method == null) {
			return null;
		}
		XmlElementDecl xmlElementDecl = method.getAnnotation(XmlElementDecl.class);
		if (xmlElementDecl == null) {
			return null;
		}
		XmlSchema xmlSchema = type.getPackage().getAnnotation(XmlSchema.class);
		String namespaceURI = getNamespaceURI(xmlElementDecl, xmlSchema);
		return new QName(namespaceURI, xmlElementDecl.name(), getPrefix(namespaceURI, xmlSchema));
	}

	/**
	 * Calculate root element name for an
	 * <code>@XmlRootElement</code> or <code>@XmlType</code>-annotaed type.
	 * @param type
	 * @return name or <code>null</code>
	 */
	protected QName getName(Class<?> type) {
		if (type.getAnnotation(XmlRootElement.class) != null) {
			return getXmlRootElementName(type);
		} else if (type.getAnnotation(XmlType.class) != null) {
			return getXmlTypeName(type);
		} else {
			return null;
		}
	}

	/**
	 * Determine <code>@XmlElementDecl</code>-annotated factory method to create {@link JAXBElement}
	 * for an <code>@XmlType</code>-annotated type
	 * @param type
	 * @return element
	 */
	protected Method getXmlElementDeclMethod(Class<?> type) {
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
				if (method.getReturnType() == JAXBElement.class) {
					Class<?>[] parameterTypes = method.getParameterTypes();
					if (parameterTypes.length == 1 && parameterTypes[0] == type) {
						return method;
					}
				}
			}
		}
		return null;
	}
	
	protected void marshal(Class<?> type, Marshaller marshaller, XMLStreamWriter writer, Object value)
			throws JAXBException, XMLStreamException {
		Object element = null;
		if (type.isAnnotationPresent(XmlRootElement.class)) {
			element = value;
		} else if (type.isAnnotationPresent(XmlType.class)) {
			/*
			 * create JAXBElement
			 */
			Method method = getXmlElementDeclMethod(type);
			if (method != null) {
				try {
					element = method.invoke(method.getDeclaringClass().newInstance(), value);
				} catch (Exception e) {
					throw new JAXBException("Cannot create JAXBElement", e);
				}
			}
			if (element == null) {
				throw new JAXBException("Cannot create JAXBElement");
			}
		} else { // good luck...
			element = value;
		}
		marshaller.marshal(element, writer);
	}
	
	public <T> T readObject(Class<? extends T> type, JsonXMLConfig config, JAXBContext context, Reader stream)
			throws XMLStreamException, JAXBException {
		checkBindable(type);
		XMLStreamReader reader = createXMLStreamReader(type, config, stream);
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

	public void writeObject(Class<?> type, JsonXMLConfig config, JAXBContext context, Writer stream, Object value)
			throws XMLStreamException, JAXBException {
		checkBindable(type);
		XMLStreamWriter writer = createXMLStreamWriter(type, config, stream);
		if (value == null) { // hack: write null
			writer.writeCharacters(null);
		} else {
			Marshaller marshaller = context.createMarshaller();
			marshal(type, marshaller, writer, value);
		}
		writer.close();
	}
	
	public <T> List<T> readArray(Class<? extends T> type, JsonXMLConfig config, JAXBContext context, Reader stream)
			throws XMLStreamException, JAXBException {
		checkBindable(type);
		XMLStreamReader reader = createXMLStreamReader(type, config, stream);
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

	public void writeArray(Class<?> type, JsonXMLConfig config, JAXBContext context, Writer stream, Collection<?> collection)
			throws XMLStreamException, JAXBException {
		checkBindable(type);
		XMLStreamWriter writer = createXMLStreamWriter(type, config, stream);
		if (collection == null) { // hack: write null
			writer.writeCharacters(null);
		} else {
			Marshaller marshaller = context.createMarshaller();
			if (!writeDocumentArray) {
				writer.writeStartDocument();
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
			}
			writer.writeProcessingInstruction(JsonXMLStreamConstants.MULTIPLE_PI_TARGET);
			for (Object value : collection) {
				if (value == null) { // hack: write null
					writer.writeCharacters(null);
				} else {							
					marshal(type, marshaller, writer, value);
				}
			}
			if (!writeDocumentArray) {
				writer.writeEndDocument();
			}
		}
		writer.close();
	}
}
