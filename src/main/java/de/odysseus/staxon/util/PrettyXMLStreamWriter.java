package de.odysseus.staxon.util;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Pretty printing XML stream writer.
 */
public class PrettyXMLStreamWriter extends StreamWriterDelegate {
	private final String newline;
	private final String[] indent = new String[64];

	private int depth = 0;
	private boolean text = false;
	private boolean leaf = false;

	/**
	 * Create instance using default indentation (\t) and line separator (\n).
	 * 
	 * @param writer
	 *            parent writer
	 */
	public PrettyXMLStreamWriter(XMLStreamWriter writer) {
		// this(writer, "  ", System.getProperty("line.separator"));
		this(writer, "\t", "\n");
	}

	/**
	 * Create instance
	 * 
	 * @param writer
	 *            parent writer
	 * @param indentation
	 *            line indentation
	 * @param newline
	 *            line separator
	 */
	public PrettyXMLStreamWriter(XMLStreamWriter writer, String indentation, String newline) {
		super(writer);
		this.newline = newline;

		/*
		 * initialize indentation strings
		 */
		StringBuilder builder = new StringBuilder();
		for (int i = 1; i < indent.length; i++) {
			indent[i] = builder.append(indentation).toString();
		}
		indent[0] = "";
	}

	private void preStartElement() throws XMLStreamException {
		if (text) {
			text = false;
		} else if (depth > 0) {
			super.writeCharacters(newline);
			super.writeCharacters(indent[depth]);
		}
		depth++;
		leaf = true;
	}

	private void preEndElement() throws XMLStreamException {
		depth--;
		if (text) {
			text = false;
		} else if (!leaf) {
			super.writeCharacters(newline);
			super.writeCharacters(indent[depth]);
		}
		leaf = false;
	}

	private void preEmptyElement_Comment_PI() throws XMLStreamException {
		if (text) {
			text = false;
		} else if (depth > 0) {
			super.writeCharacters(newline);
			super.writeCharacters(indent[depth]);
		}
		leaf = false;
	}

	private void preCharacters() {
		text = true;
	}

	@Override
	public void setParent(XMLStreamWriter parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeStartDocument() throws XMLStreamException {
		super.writeStartDocument();
		super.writeCharacters(newline);
	}

	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		super.writeStartDocument(version);
		super.writeCharacters(newline);
	}

	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		super.writeStartDocument(encoding, version);
		super.writeCharacters(newline);
	}

	@Override
	public void writeEndDocument() throws XMLStreamException {
		super.writeCharacters(newline);
		super.writeEndDocument();
	}

	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		preStartElement();
		super.writeStartElement(localName);
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		preStartElement();
		super.writeStartElement(namespaceURI, localName);
	}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		preStartElement();
		super.writeStartElement(prefix, localName, namespaceURI);
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		preEmptyElement_Comment_PI();
		super.writeEmptyElement(namespaceURI, localName);
	}

	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		preEmptyElement_Comment_PI();
		super.writeEmptyElement(prefix, localName, namespaceURI);
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		preEmptyElement_Comment_PI();
		super.writeEmptyElement(localName);
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		preEndElement();
		super.writeEndElement();
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
		preCharacters();
		super.writeCData(data);
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		preCharacters();
		super.writeCharacters(text);
	}

	@Override
	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
		preCharacters();
		super.writeCharacters(text, start, len);
	}
	
	@Override
	public void writeComment(String data) throws XMLStreamException {
		preEmptyElement_Comment_PI();
		super.writeComment(data);
	}
	
	@Override
	public void writeProcessingInstruction(String target) throws XMLStreamException {
		preEmptyElement_Comment_PI();
		super.writeProcessingInstruction(target);
	}
	
	@Override
	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		preEmptyElement_Comment_PI();
		super.writeProcessingInstruction(target, data);
	}
}
