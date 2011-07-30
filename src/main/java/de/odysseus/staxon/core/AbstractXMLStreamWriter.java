package de.odysseus.staxon.core;

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
	
	public AbstractXMLStreamWriter(T rootInfo) {
		scope = new XMLStreamWriterScope<T>(XMLConstants.NULL_NS_URI, rootInfo);
		startDocumentWritten = false;
	}
	
	protected XMLStreamWriterScope<T> getScope() {
		return scope;
	}

	protected abstract void writeElementTagStart(XMLStreamWriterScope<T> newScope) throws XMLStreamException;
	protected abstract void writeElementTagEnd() throws XMLStreamException;
	protected abstract void writeEndElementTag() throws XMLStreamException;
	protected abstract void writeProperty(String name, String value) throws XMLStreamException;
	protected abstract void writeData(String text, int type) throws XMLStreamException;
	protected abstract void writePI(String target, String data) throws XMLStreamException;

	private void ensureStartTagClosed() throws XMLStreamException {
		if (!scope.isStartTagClosed()) {
			writeElementTagEnd();
			scope.setStartTagClosed(true);
			if (scope.isEmptyElement()) {
				scope = scope.getParent();
			}
		}
	}

	public void writeStartElement(String localName) throws XMLStreamException {
		if (scope.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX) == null) {
			throw new XMLStreamException("Default namespace URI has not been set");
		}
		writeStartElement(XMLConstants.DEFAULT_NS_PREFIX, localName, false);
	}

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

	private void writeStartElement(String prefix, String localPart, boolean emptyElement) throws XMLStreamException {
		if (localPart == null) {
			throw new XMLStreamException("Local name must not be null");
		}
		ensureStartTagClosed();
		if (startDocumentWritten && scope.isRoot() && scope.getLastChild() != null) {
			throw new XMLStreamException("Multiple roots within document");
		}
		XMLStreamWriterScope<T> newScope = new XMLStreamWriterScope<T>(scope, prefix, localPart, emptyElement);
		writeElementTagStart(newScope);
		scope = newScope;
	}

	public void writeEndElement() throws XMLStreamException {
		ensureStartTagClosed();
		if (scope.isRoot()) {
			throw new XMLStreamException("Cannot write end element in root scope");
		}
		writeEndElementTag();
		scope = scope.getParent();
	}

	public void writeEmptyElement(String localName) throws XMLStreamException {
		if (scope.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX) == null) {
			throw new XMLStreamException("Default namespace URI has not been set");
		}
		writeStartElement(XMLConstants.DEFAULT_NS_PREFIX, localName, true);
	}

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

	public void writeAttribute(String localName, String value) throws XMLStreamException {
		writeAttribute(null, null, localName, value);
	}

	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
		writeAttribute(null, namespaceURI, localName, value);
	}

	public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
		if (scope.isStartTagClosed()) {
			throw new XMLStreamException("Cannot write attribute: element has children or text");
		}
		String name;
		if (prefix == null && namespaceURI == null) {
			name = localName;
		} else if (namespaceURI != null) { // need a non-empty prefix
			if (prefix == null) {
				prefix = scope.getPrefix(namespaceURI);
				if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
					Iterator<String> prefixes = scope.getPrefixes(namespaceURI);
					while (prefixes.hasNext() && XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
						prefix = prefixes.next();
					}
				}
				if (prefix == null || XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
					throw new XMLStreamException("Namespace URI has not been bound to a prefix: " + namespaceURI);
				}
			}
			name = prefix + ':' + localName;
		} else {
			throw new XMLStreamException("Cannot write attribute: non-null prefix, but namespaceURI is null");
		}
		writeProperty(name, value);
	}

	public void writeCharacters(String text) throws XMLStreamException {
		ensureStartTagClosed();
		writeData(text, XMLStreamConstants.CHARACTERS);
	}
	
	public void writeCharacters(char[] text, int start, int length) throws XMLStreamException {
		writeCharacters(new String(text, start, length));
	}

	public void writeCData(String data) throws XMLStreamException {
		ensureStartTagClosed();
		writeData(data, XMLStreamConstants.CDATA);
	}

	public void writeStartDocument() throws XMLStreamException {
		writeStartDocument("1.0");
	}

	public void writeStartDocument(String version) throws XMLStreamException {
		writeStartDocument("UTF-8", version);
	}

	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		if (startDocumentWritten || !scope.isRoot()) {
			throw new XMLStreamException("Cannot start document");
		}
		writePI("xml", String.format("version=\"%s\" encoding=\"%s\"", version, encoding));
		startDocumentWritten = true;
	}

	public void writeEndDocument() throws XMLStreamException {
		if (!scope.isRoot()) {
			ensureStartTagClosed();
			do {
				writeEndElement();
			} while (!scope.isRoot());
		}
		startDocumentWritten = false;
	}
	
	public void close() throws XMLStreamException {
		ensureStartTagClosed();
		flush();
	}

	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		if (scope.isStartTagClosed()) {
			throw new XMLStreamException("Cannot write namespace: element has children or text");
		}
		if (prefix == null || XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
			prefix = XMLConstants.DEFAULT_NS_PREFIX;
		}
		if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
			writeAttribute(XMLConstants.XMLNS_ATTRIBUTE, namespaceURI);
		} else {
			writeAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix, namespaceURI);
		}
		setPrefix(prefix, namespaceURI);
	}

	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		writeNamespace(XMLConstants.DEFAULT_NS_PREFIX, namespaceURI);
	}

	public String getPrefix(String namespaceURI) throws XMLStreamException {
		return scope.getPrefix(namespaceURI);
	}

	public void setPrefix(String prefix, String namespaceURI) throws XMLStreamException {
		scope.setPrefix(prefix, namespaceURI);
	}

	public void setDefaultNamespace(String namespaceURI) throws XMLStreamException {
		setPrefix(XMLConstants.DEFAULT_NS_PREFIX, namespaceURI);
	}

	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		if (!scope.isRoot()) {
			throw new XMLStreamException("This method may only be called once at the start of the document");
		}
		scope = new XMLStreamWriterScope<T>(context, scope.getInfo());
	}

	public NamespaceContext getNamespaceContext() {
		return scope;
	}

	public void writeComment(String data) throws XMLStreamException {
		ensureStartTagClosed();
		writeData(data, XMLStreamConstants.COMMENT);
	}

	public void writeProcessingInstruction(String target) throws XMLStreamException {
		ensureStartTagClosed();
		writePI(target, null);
	}

	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		ensureStartTagClosed();
		writePI(target, data);
	}

	public void writeDTD(String dtd) throws XMLStreamException {
		ensureStartTagClosed();
		writeData(dtd, XMLStreamConstants.DTD);
	}

	public void writeEntityRef(String name) throws XMLStreamException {
		ensureStartTagClosed();
		writeData(name, XMLStreamConstants.ENTITY_REFERENCE);
	}

	public Object getProperty(String name) throws IllegalArgumentException {
		throw new IllegalArgumentException("Unsupported property: " + name);
	}
}
