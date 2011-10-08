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
package de.odysseus.staxon;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Abstract XML stream writer.
 */
public abstract class AbstractXMLStreamWriter<T> implements XMLStreamWriter {
	private XMLStreamWriterScope<T> scope;
	private boolean startDocumentWritten;
	
	/**
	 * Create writer instance.
	 * @param rootInfo root scope information
	 */
	public AbstractXMLStreamWriter(T rootInfo) {
		scope = new XMLStreamWriterScope<T>(XMLConstants.NULL_NS_URI, rootInfo);
		startDocumentWritten = false;
	}
	
	private void ensureStartTagClosed() throws XMLStreamException {
		if (!scope.isStartTagClosed()) {
			writeStartElementTagEnd();
			scope.setStartTagClosed(true);
			if (scope.isEmptyElement()) {
				scope = scope.getParent();
			}
		}
	}

	private void writeStartElement(String prefix, String localPart, boolean emptyElement) throws XMLStreamException {
		if (localPart == null) {
			throw new XMLStreamException("Local name must not be null");
		}
		ensureStartTagClosed();
		if (startDocumentWritten && scope.isRoot() && scope.getLastChild() != null) {
			throw new XMLStreamException("Multiple roots within document");
		}
		XMLStreamWriterScope<T> newScope = new XMLStreamWriterScope<T>(scope, prefix, localPart, emptyElement);
		writeStartElementTag(newScope);
		scope = newScope;
	}

	/**
	 * @return current scope
	 */
	protected XMLStreamWriterScope<T> getScope() {
		return scope;
	}

	/**
	 * Write open start element tag.
	 * @param newScope new scope
	 * @throws XMLStreamException
	 */
	protected abstract void writeStartElementTag(XMLStreamWriterScope<T> newScope) throws XMLStreamException;
	
	/**
	 * Write close start element tag.
	 * @throws XMLStreamException
	 */
	protected abstract void writeStartElementTagEnd() throws XMLStreamException;
	
	/**
	 * Write end element tag.
	 * @throws XMLStreamException
	 */
	protected abstract void writeEndElementTag() throws XMLStreamException;
	
	/**
	 * Write attribute (or namespace declaration).
	 * @param prefix attribute prefix (may be <code>XMLConstants.DEFAULT_NS_PREFIX</code>)
	 * @param localName local name
	 * @param value attribute value
	 * @throws XMLStreamException
	 */
	protected abstract void writeAttr(String prefix, String localName, String value) throws XMLStreamException;
	
	/**
	 * Write characters/comment/dtd/entity data.
	 * @param data text/data
	 * @param type one of <code>CHARACTERS, COMMENT, CDATA, DTD, ENTITY_REFERENCE, SPACE</code>
	 * @throws XMLStreamException
	 */
	protected abstract void writeData(String data, int type) throws XMLStreamException;
	
	/**
	 * Read processing instruction.
	 * @param target PI target
	 * @param data PI data (may be <code>null</code>)
	 * @throws XMLStreamException
	 */
	protected abstract void writePI(String target, String data) throws XMLStreamException;

	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		if (scope.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX) == null) {
			throw new XMLStreamException("Default namespace URI has not been set");
		}
		writeStartElement(XMLConstants.DEFAULT_NS_PREFIX, localName, false);
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		if (namespaceURI == null) {
			throw new XMLStreamException("Namespace URI must not be null");
		}
		String prefix = scope.getPrefix(namespaceURI);
		if (prefix == null) {
			throw new XMLStreamException("Namespace URI has not been bound to a prefix: " + namespaceURI);
		}
		writeStartElement(prefix, localName, false);
	}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		if (prefix == null) {
			throw new XMLStreamException("Prefix must not be null");
		}
		if (namespaceURI == null) {
			throw new XMLStreamException("Namespace URI must not be null");
		}
		if (scope.getPrefix(namespaceURI) == null) {
			scope.setPrefix(prefix, namespaceURI);
		}
		writeStartElement(prefix, localName, false);
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		ensureStartTagClosed();
		if (scope.isRoot()) {
			throw new XMLStreamException("Cannot write end element in root scope");
		}
		writeEndElementTag();
		scope = scope.getParent();
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		if (scope.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX) == null) {
			throw new XMLStreamException("Default namespace URI has not been set");
		}
		writeStartElement(XMLConstants.DEFAULT_NS_PREFIX, localName, true);
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		if (namespaceURI == null) {
			throw new XMLStreamException("Namespace URI must not be null");
		}
		String prefix = scope.getPrefix(namespaceURI);
		if (prefix == null) {
			throw new XMLStreamException("Namespace URI has not been bound to a prefix: " + namespaceURI);
		}
		writeStartElement(prefix, localName, true);
	}

	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		if (prefix == null) {
			throw new XMLStreamException("Prefix must not be null");
		}
		if (namespaceURI == null) {
			throw new XMLStreamException("Namespace URI must not be null");
		}
		if (scope.getPrefix(namespaceURI) == null) {
			scope.setPrefix(prefix, namespaceURI);
		}
		writeStartElement(prefix, localName, true);
	}

	@Override
	public void writeAttribute(String localName, String value) throws XMLStreamException {
		writeAttribute(null, XMLConstants.NULL_NS_URI, localName, value);
	}

	@Override
	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
		writeAttribute(null, namespaceURI, localName, value);
	}

	@Override
	public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
		if (scope.isStartTagClosed()) {
			throw new XMLStreamException("Cannot write attribute: element has children or text");
		}
		if (XMLConstants.NULL_NS_URI.equals(namespaceURI)) { // no namespace -> no prefix
			if (prefix == null) {
				prefix = XMLConstants.DEFAULT_NS_PREFIX;
			} else if (!XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
				throw new XMLStreamException("Cannot write attribute without a namespace URI and prefix: " + prefix);
			}
		} else { // namespace -> prefixed attribute
			if (prefix == null) { // lookup prefix
				prefix = scope.getPrefix(namespaceURI);
				if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) { // need a non-empty prefix
					Iterator<String> prefixes = scope.getPrefixes(namespaceURI);
					while (prefixes.hasNext() && XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
						prefix = prefixes.next();
					}
				} else if (prefix == null) {
					throw new XMLStreamException("Namespace URI has not been bound to a prefix: " + namespaceURI);
				}
			} else if (!XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
				String boundNamespaceURI = scope.getNamespaceURI(prefix);
				if (XMLConstants.NULL_NS_URI.equals(boundNamespaceURI)) {
					getScope().setPrefix(prefix, namespaceURI);
				} else if (!namespaceURI.equals(boundNamespaceURI)) {
					throw new XMLStreamException("Another namespace URI has been bound to the given prefix: " + prefix);
				}
			}
			if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
				throw new XMLStreamException("Cannot write attribute without prefix for namespace URI: " + namespaceURI);
			}
		}
		writeAttr(prefix, localName, value);
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		ensureStartTagClosed();
		writeData(text, XMLStreamConstants.CHARACTERS);
	}
	
	@Override
	public void writeCharacters(char[] text, int start, int length) throws XMLStreamException {
		writeCharacters(new String(text, start, length));
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
		ensureStartTagClosed();
		writeData(data, XMLStreamConstants.CDATA);
	}

	@Override
	public void writeStartDocument() throws XMLStreamException {
		writeStartDocument("UTF-8", null);
	}

	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		writeStartDocument(null, version);
	}

	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		if (startDocumentWritten || !scope.isRoot()) {
			throw new XMLStreamException("Cannot start document");
		}
		if (version == null) {
			version = "1.0";
		}
		if (encoding == null) {
			writePI("xml", String.format("version=\"%s\"", version, encoding));
		} else {
			writePI("xml", String.format("version=\"%s\" encoding=\"%s\"", version, encoding));
		}
		startDocumentWritten = true;
	}

	@Override
	public void writeEndDocument() throws XMLStreamException {
		if (!scope.isRoot()) {
			ensureStartTagClosed();
			while (!scope.isRoot()) {
				writeEndElement();
			}
		}
		startDocumentWritten = false;
	}
	
	@Override
	public void close() throws XMLStreamException {
		ensureStartTagClosed();
		flush();
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		if (scope.isStartTagClosed()) {
			throw new XMLStreamException("Cannot write namespace: element has children or text");
		}
		if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
			writeAttr(prefix, XMLConstants.XMLNS_ATTRIBUTE, namespaceURI);
		} else {
			writeAttr(XMLConstants.XMLNS_ATTRIBUTE, prefix, namespaceURI);
		}
		setPrefix(prefix, namespaceURI);
	}

	@Override
	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		writeNamespace(XMLConstants.DEFAULT_NS_PREFIX, namespaceURI);
	}

	@Override
	public String getPrefix(String namespaceURI) throws XMLStreamException {
		return scope.getPrefix(namespaceURI);
	}

	@Override
	public void setPrefix(String prefix, String namespaceURI) throws XMLStreamException {
		scope.setPrefix(prefix, namespaceURI);
	}

	@Override
	public void setDefaultNamespace(String namespaceURI) throws XMLStreamException {
		setPrefix(XMLConstants.DEFAULT_NS_PREFIX, namespaceURI);
	}

	@Override
	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		if (!scope.isRoot()) {
			throw new XMLStreamException("This method may only be called once at the start of the document");
		}
		scope = new XMLStreamWriterScope<T>(context, scope.getInfo());
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return scope;
	}

	@Override
	public void writeComment(String data) throws XMLStreamException {
		ensureStartTagClosed();
		writeData(data, XMLStreamConstants.COMMENT);
	}

	@Override
	public void writeProcessingInstruction(String target) throws XMLStreamException {
		ensureStartTagClosed();
		writePI(target, null);
	}

	@Override
	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		ensureStartTagClosed();
		writePI(target, data);
	}

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
		ensureStartTagClosed();
		writeData(dtd, XMLStreamConstants.DTD);
	}

	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
		ensureStartTagClosed();
		writeData(name, XMLStreamConstants.ENTITY_REFERENCE);
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		throw new IllegalArgumentException("Unsupported property: " + name);
	}
}
