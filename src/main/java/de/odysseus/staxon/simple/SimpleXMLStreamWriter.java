package de.odysseus.staxon.simple;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import de.odysseus.staxon.core.AbstractXMLStreamWriter;
import de.odysseus.staxon.core.XMLStreamWriterScope;

/**
 * Simple XML Stream Writer
 */
public class SimpleXMLStreamWriter extends AbstractXMLStreamWriter<Object> {
	private final Writer writer;
	
	public SimpleXMLStreamWriter(Writer writer) {
		super(null);
		this.writer = writer;
	}
	
	private void writeEscaped(String string, boolean attribute) throws IOException {
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			switch (c) {
			case '<':
				writer.write("&lt;");
				break;
			case '>':
				writer.write("&gt;");
				break;
			case '&':
				writer.write("&amp;");
				break;
			default:
				if (c == '"' && attribute) {
					writer.write("&quot;");
				} else {
					writer.write(c);
				}
			}
		}
	}
	
	@Override
	public void flush() throws XMLStreamException {
		try {
			writer.flush();
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
 	}

	@Override
	protected void writeElementTagStart(XMLStreamWriterScope<Object> newScope) throws XMLStreamException {
		try {
			writer.write('<');
			writer.write(newScope.getTagName());
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	protected void writeElementTagEnd() throws XMLStreamException {
		try {
			if (getScope().isEmptyElement()) {
				writer.write('/');
			}
			writer.write('>');
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	protected void writeEndElementTag() throws XMLStreamException {
		try {
			writer.write('<');
			writer.write('/');
			writer.write(getScope().getTagName());
			writer.write('>');
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	protected void writeProperty(String name, String value) throws XMLStreamException {
		try {
			writer.write(' ');
			writer.write(name);
			writer.write('=');
			writer.write('"');
			writeEscaped(value, true);
			writer.write('"');
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	protected void writeData(String data, int type) throws XMLStreamException {
		try {
			switch(type) {
			case XMLStreamConstants.CHARACTERS:
				writeEscaped(data, false);
				break;
			case XMLStreamConstants.CDATA:
				writer.write("<![CDATA[");
				writer.write(data);
				writer.write("]]>");
				break;
			case XMLStreamConstants.COMMENT:
				writer.write("<!--");
				writeEscaped(data, false);
				writer.write("-->");
				break;
			case XMLStreamConstants.ENTITY_REFERENCE:
				writer.write('&');
				writer.write(data);
				writer.write(';');				
				break;
			default:
				throw new UnsupportedOperationException("Cannot write data of type " + type);
			}
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}		
	}
	
	@Override
	protected void writePI(String target, String data) throws XMLStreamException {
		try {
			writer.write("<?");
			writer.write(target);
			if (data != null) {
				writer.write(' ');
				writer.write(data);
			}
			writer.write("?>");
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}
}
