/*
 * Copyright 2011-2014 Odysseus Software GmbH
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
package de.odysseus.staxon.json.stream.jsr353;

import java.io.IOException;

import javax.json.JsonException;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import de.odysseus.staxon.json.stream.JsonStreamSource;
import de.odysseus.staxon.json.stream.JsonStreamToken;

/**
 * JSON-P (<code>javax.json</code>) <code>JsonStreamSource</code> implementation.
 */
public class JsonProcessingStreamSource implements JsonStreamSource {
	/**
	 * Map given event to corresponding token
	 * @param event
	 * @return token
	 */
	private static final JsonStreamToken toToken(Event event) {
		if (event != null) {
			switch (event) {
			case KEY_NAME:
				return JsonStreamToken.NAME;
			case VALUE_TRUE:
			case VALUE_FALSE:
			case VALUE_NULL:
			case VALUE_NUMBER:
			case VALUE_STRING:
				return JsonStreamToken.VALUE;
			case START_OBJECT:
				return JsonStreamToken.START_OBJECT;
			case END_OBJECT:
				return JsonStreamToken.END_OBJECT;
			case START_ARRAY:
				return JsonStreamToken.START_ARRAY;
			case END_ARRAY:
				return JsonStreamToken.END_ARRAY;
			default:
				throw new IllegalStateException("Unexpected event");
			}
		} else {
			return JsonStreamToken.NONE;
		}
	}
	
	private final JsonParser parser;	
	private Event peekEvent;

	public JsonProcessingStreamSource(JsonParser parser) {
		this.parser = parser;
	}

	private Event consume(JsonStreamToken token) throws IOException {
		Event event = peekEvent();
		if (toToken(event) != token) {
			throw new IOException("Expected token: " + token + ", but was: " + peek());
		}
		peekEvent = null;
		return event;
	}
	
	private Event peekEvent() throws IOException {
		if (peekEvent == null) {
			try {
				if (parser.hasNext()) {
					peekEvent = parser.next();
				}
			} catch (JsonException e) {
				if (e.getCause() instanceof IOException) {
					throw (IOException) e.getCause();
				} else {
					throw e;
				}
			}
		}
		return peekEvent;
	}
	
	@Override
	public void close() throws IOException {
		try {
			parser.close();
		} catch (JsonException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			} else {
				throw e;
			}
		}
	}

	@Override
	public int getLineNumber() {
		return (int)parser.getLocation().getLineNumber();
	}

	@Override
	public int getColumnNumber() {
		return (int)parser.getLocation().getColumnNumber();
	}

	@Override
	public int getCharacterOffset() {
		return (int)parser.getLocation().getStreamOffset();
	}

	@Override
	public String getPublicId() {
		return null;
	}

	@Override
	public String getSystemId() {
		return null;
	}

	@Override
	public String name() throws IOException {
		consume(JsonStreamToken.NAME);
		return parser.getString();
	}

	@Override
	public Value value() throws IOException {
		switch (consume(JsonStreamToken.VALUE)) {
		case VALUE_STRING:
			return new Value(parser.getString());
		case VALUE_TRUE:
			return TRUE;
		case VALUE_FALSE:
			return FALSE;
		case VALUE_NUMBER:
			if (parser.isIntegralNumber()) {
				return new Value(parser.getString(), Long.valueOf(parser.getLong()));
			} else {
				return new Value(parser.getString(), parser.getBigDecimal());
			}
		case VALUE_NULL:
			return NULL;
		default:
			throw new IllegalStateException("Unexpected event");
		}
	}

	@Override
	public void startObject() throws IOException {
		consume(JsonStreamToken.START_OBJECT);
	}

	@Override
	public void endObject() throws IOException {
		consume(JsonStreamToken.END_OBJECT);
	}

	@Override
	public void startArray() throws IOException {
		consume(JsonStreamToken.START_ARRAY);
	}

	@Override
	public void endArray() throws IOException {
		consume(JsonStreamToken.END_ARRAY);
	}
	
	@Override
	public JsonStreamToken peek() throws IOException {
		return toToken(peekEvent());
	}
}
