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
package de.odysseus.staxon.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import de.odysseus.staxon.util.EventWriterDelegate;

/**
 * Pretty printing XML event writer.
 */
public class PrettyXMLEventWriter extends EventWriterDelegate {
	static class WhitespaceEvent implements Characters {
		final String whitespace;
		public WhitespaceEvent(String whitespace) {
			this.whitespace = whitespace;
		}
		@Override
		public Characters asCharacters() {
			return this;
		}
		@Override
		public StartElement asStartElement() {
			return (StartElement) this;
		}
		@Override
		public EndElement asEndElement() {
			return (EndElement) this;
		}
		@Override
		public int getEventType() {
			return XMLStreamConstants.CHARACTERS;
		}
		@Override
		public Location getLocation() {
			return null;
		}
		@Override
		public QName getSchemaType() {
			return null;
		}
		@Override
		public boolean isAttribute() {
			return false;
		}
		@Override
		public boolean isCharacters() {
			return true;
		}
		@Override
		public boolean isEndDocument() {
			return false;
		}
		@Override
		public boolean isEndElement() {
			return false;
		}
		@Override
		public boolean isEntityReference() {
			return false;
		}
		@Override
		public boolean isNamespace() {
			return false;
		}
		@Override
		public boolean isProcessingInstruction() {
			return false;
		}
		@Override
		public boolean isStartDocument() {
			return false;
		}
		@Override
		public boolean isStartElement() {
			return false;
		}
		@Override
		public String toString() {
			try {
				Writer writer = new StringWriter();
				writer.write(getClass().getSimpleName());
				writer.write('(');
				writer.write(whitespace);
				writer.write(')');
				return writer.toString();
			} catch (IOException e) {
				return super.toString();
			}
		}
		@Override
		public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
			try {
				writer.write(whitespace);
			} catch (IOException e) {
				throw new XMLStreamException(e);
			}
			
		}
		@Override
		public String getData() {
			return whitespace;
		}
		@Override
		public boolean isCData() {
			return false;
		}
		@Override
		public boolean isIgnorableWhiteSpace() {
			return false;
		}
		@Override
		public boolean isWhiteSpace() {
			return true;
		}
	}

	private final Characters newline;
	private final Characters[] indent = new Characters[64];

	private int depth = 0;
	private boolean text = false;
	private boolean leaf = false;

	public PrettyXMLEventWriter(XMLEventWriter writer) {
		// this(writer, "  ", System.getProperty("line.separator"));
		this(writer, "\t", "\n");
	}

	public PrettyXMLEventWriter(XMLEventWriter writer, String indentation, String newline) {
		super(writer);
		this.newline = new WhitespaceEvent(newline);

		/*
		 * initialize indentation strings
		 */
		StringBuilder builder = new StringBuilder();
		for (int i = 1; i < indent.length; i++) {
			indent[i] = new WhitespaceEvent(builder.append(indentation).toString());
		}
		indent[0] = null;
	}


	private void preStartElement() throws XMLStreamException {
		if (text) {
			text = false;
		} else if (depth > 0) {
			super.add(newline);
			super.add(indent[depth]);
		}
		depth++;
		leaf = true;
	}

	private void preEndElement() throws XMLStreamException {
		depth--;
		if (text) {
			text = false;
		} else if (!leaf) {
			super.add(newline);
			if (depth > 0) {
				super.add(indent[depth]);
			}
		}
		leaf = false;
	}

	private void preSimpleStructure() throws XMLStreamException {
		if (text) {
			text = false;
		} else if (depth > 0) {
			super.add(newline);
			super.add(indent[depth]);
		}
		leaf = false;
	}

	private void preCharacters() {
		text = true;
	}
	
	@Override
	public void setParent(XMLEventWriter parent) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void add(XMLEventReader reader) throws XMLStreamException {
		while (reader.hasNext()) {
			add(reader.nextEvent());
		}
	}

	@Override
	public void add(XMLEvent event) throws XMLStreamException {
		switch (event.getEventType()) {
		case XMLStreamConstants.END_DOCUMENT:
			super.add(newline);
			super.add(event);
			break;
		case XMLStreamConstants.END_ELEMENT:
			preEndElement();
			super.add(event);
			break;
		case XMLStreamConstants.START_DOCUMENT:
			super.add(event);
			super.add(newline);
			break;
		case XMLStreamConstants.START_ELEMENT:
			preStartElement();
			super.add(event);
			break;
		case XMLStreamConstants.CHARACTERS:
		case XMLStreamConstants.CDATA:
			preCharacters();
			super.add(event);
			break;
		case XMLStreamConstants.COMMENT:
			preSimpleStructure();
			super.add(event);
			break;
		case XMLStreamConstants.PROCESSING_INSTRUCTION:
			preSimpleStructure();
			super.add(event);
			break;
		default:
			super.add(event);
		}
	}
}
