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
import java.io.Reader;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import de.odysseus.staxon.AbstractXMLStreamReader;
import de.odysseus.staxon.XMLStreamReaderScope;

/**
 * Simple XML Stream Reader.
 * 
 * The scope info is the element tag name.
 */
public class SimpleXMLStreamReader extends AbstractXMLStreamReader<String> {
	private final Reader reader;
	private int ch;
	
	public SimpleXMLStreamReader(Reader reader) throws XMLStreamException {
		super(null);
		this.reader = reader;
		try {
			nextChar();
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
		init();
	}

	@Override
	protected boolean consume(XMLStreamReaderScope<String> scope) throws XMLStreamException, IOException {
		skipWhitespace();

		if (ch == -1) {
			readEndDocument();
			return false;
		}

		if (ch == '<') {
			nextChar();
			if (ch == '/') { // END_ELEMENT
				nextChar();
				String tagName = readName('>');
				if (scope.getInfo().equals(tagName)) {
					readEndElementTag();
				} else {
					throw new XMLStreamException("not well-formed");
				}
			} else if (ch == '?') { // START_DOPCUMENT | PROCESSING_INSTRUCTION
				nextChar();
				String target = readName('?');
				String data = null;
				if (ch != '?') {
					data = readText('?');
				}
				nextChar(); // please, let it be '>'
				if ("xml".equals(target)) {
					readStartDocument();
				} else {
					readPI(target, data);
				}
			} else if (ch == '!') { // COMMENT | CDATA
				nextChar();
				switch (ch) {
				case '-': // COMMENT
					String comment = readData('-');
					if (!comment.startsWith("-")) {
						throw new XMLStreamException("expected comment");
					}
					readText(comment.substring(1, comment.length() - 3), XMLStreamConstants.COMMENT);
					break;
				case '[': // CDATA
					String cdata = readData(']');
					if (!cdata.startsWith("CDATA[")) {
						throw new XMLStreamException("expected cdata");
					}
					readText(cdata.substring(6, cdata.length() - 3), XMLStreamConstants.CDATA);
					break;
				}
			} else { // START_ELEMENT
				String tagName = readName(' ');
				scope = readStartElementTag(tagName);
				scope.setInfo(tagName);
				while (ch != '>' && ch != '/') {
					String name = readName('=');
					nextChar();
					skipWhitespace();
					int quote = ch;
					nextChar();
					String value = readText(quote);
					nextChar();
					skipWhitespace();
					readProperty(name, value);
				}
				if (ch == '/') {
					nextChar(); // please, let it be '>'
					readEndElementTag();
				} else {
					nextChar();
					return consume(scope);
				}
			}
			nextChar();
		} else {
			String text = readText('<');
			readText(text, XMLStreamConstants.CHARACTERS);
		}
		return true;
	}
	

	private void nextChar() throws IOException {
		ch = reader.read();
	}
	
	private void skipWhitespace() throws IOException {
		if (Character.isWhitespace(ch)) {
			do {
				nextChar();
			} while (Character.isWhitespace(ch));
		}
	}
	
	private String readData(final int end) throws IOException {
		StringBuilder data = new StringBuilder();
		int state = 0;
		do {
			nextChar();
			data.append((char)ch);
			if ((ch == end && state != 2) || (ch == '>' && state == 2)) {
				state++;
			} else {
				state = 0;
			}
		} while (state < 3);
		return data.toString();
	}
	
	private String readText(final int end) throws IOException {
		final StringBuilder builder = new StringBuilder();
		while (ch != end && ch >= 0) {
			if (ch == '&') {
				nextChar();
				String entity = readName(';');
				if ("lt".equals(entity)) {
					builder.append('<');
				} else if ("gt".equals(entity)) {
					builder.append('>');
				} else if ("amp".equals(entity)) {
					builder.append('&');
				} else if ("quot".equals(entity)) {
					builder.append('"');
				} else if ("apos".equals(entity)) {
					builder.append('\'');
				} else {
					builder.append('?');
				}
			} else {
				builder.append((char)ch);
			}
			nextChar();
		}
		return builder.toString();
	}

	private String readName(final int end) throws IOException {
		skipWhitespace();
		final StringBuilder builder = new StringBuilder();
		do {
			builder.append((char)ch);
			nextChar();
		} while (ch != end && ch != '>' && ch != '/' && !Character.isWhitespace(ch));
		skipWhitespace();
		return builder.toString();
	}
}
