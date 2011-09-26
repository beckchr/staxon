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

import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

/**
 * Package-private helper used by {@link PrettyXMLStreamWriter} and {@link PrettyXMLEventWriter}
 * to handle pretty printing state and insert indentation and newline characters events.
 */
class PrettyXMLWhitespaceHandler {
	/**
	 * Whitespace (indentation/newline) event
	 */
	static class WhitespaceEvent implements Characters {
		final String whitespace;
		final String description;
		WhitespaceEvent(String whitespace, String description) {
			this.whitespace = whitespace;
			this.description = description;
		}
		@Override
		public Characters asCharacters() {
			return this;
		}
		@Override
		public StartElement asStartElement() {
			return null;
		}
		@Override
		public EndElement asEndElement() {
			return null;
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
			return description;
		}
		@Override
		public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
			// API doc: No indentation or whitespace should be "outputted".
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

	/**
	 * Whitespace writer.
	 */
	static abstract class WhitespaceWriter {
		abstract void add(WhitespaceEvent event) throws XMLStreamException;
	}
	
	private static final int MAX_DEPTH = 64;
	
	private final WhitespaceEvent newline;
	private final WhitespaceEvent[] indent;
	private final WhitespaceWriter writer;

	private int depth = 0;
	private boolean text = false;
	private boolean leaf = false;

	/**
	 * Create whitespace handler for an {@link XMLStreamWriter}.
	 * 
	 * @param writer
	 *            stream writer
	 * @param indentation
	 *            line indentation
	 * @param newline
	 *            line separator
	 */
	PrettyXMLWhitespaceHandler(final XMLStreamWriter writer, String indentation, String newline) {
		this(indentation, newline, new WhitespaceWriter() {
			@Override
			public void add(WhitespaceEvent event) throws XMLStreamException {
				writer.writeCharacters(event.getData());
			}
		});
	}

	/**
	 * Create whitespace handler for an {@link XMLEventWriter}.
	 * 
	 * @param writer
	 *            event writer
	 * @param indentation
	 *            line indentation
	 * @param newline
	 *            line separator
	 */
	PrettyXMLWhitespaceHandler(final XMLEventWriter writer, String indentation, String newline) {
		this(indentation, newline, new WhitespaceWriter() {
			@Override
			public void add(WhitespaceEvent event) throws XMLStreamException {
				writer.add(event);
			}
		});
	}

	private PrettyXMLWhitespaceHandler(String indentation, String newline, WhitespaceWriter writer) {
		this.newline = new WhitespaceEvent(newline, "<<newline>>");
		this.indent = new WhitespaceEvent[MAX_DEPTH];
		this.writer = writer;

		/*
		 * initialize indentation whitespace events
		 */
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < indent.length; i++) {
			indent[i] = new WhitespaceEvent(builder.toString(), "<<indentation>>");
			builder.append(indentation);
		}
	}

	private void preSimpleStructure() throws XMLStreamException {
		if (text) {
			text = false;
		} else if (depth > 0) {
			writer.add(newline);
			writer.add(indent[depth]);
		}
	}

	private void postSimpleStructure() throws XMLStreamException {
		leaf = false;
		if (depth == 0) {
			writer.add(newline);
		}
	}

	void preStartDocument() throws XMLStreamException {
		preSimpleStructure();
	}

	void postStartDocument() throws XMLStreamException {
		postSimpleStructure();
	}

	void preComment() throws XMLStreamException {
		preSimpleStructure();
	}

	void postComment() throws XMLStreamException {
		postSimpleStructure();
	}

	void preProcessingInstruction() throws XMLStreamException {
		preSimpleStructure();
	}

	void postProcessingInstruction() throws XMLStreamException {
		postSimpleStructure();
	}

	void preStartElement() throws XMLStreamException {
		if (text) {
			text = false;
		} else if (depth > 0) {
			writer.add(newline);
			writer.add(indent[depth]);
		}
	}

	void postStartElement() throws XMLStreamException {
		depth++;
		leaf = true;
	}

	void preEndElement() throws XMLStreamException {
		depth--;
		if (text) {
			text = false;
		} else if (!leaf) {
			writer.add(newline);
			if (depth > 0) {
				writer.add(indent[depth]);
			}
		}
	}

	void postEndElement() throws XMLStreamException {
		leaf = false;
	}

	void preEmptyELement() throws XMLStreamException {
		preStartElement();
	}

	void postEmptyELement() throws XMLStreamException {
		postStartElement();
	}

	void preCharacters() {
		text = true;
	}

	void postCharacters() {
	}

	void preEndDocument() throws XMLStreamException {
	}

	void postEndDocument() throws XMLStreamException {
		writer.add(newline);
	}
}
