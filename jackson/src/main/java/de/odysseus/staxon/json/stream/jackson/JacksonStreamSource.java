/*
 * Copyright 2011, 2012 Odysseus Software GmbH
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
package de.odysseus.staxon.json.stream.jackson;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import de.odysseus.staxon.json.stream.JsonStreamSource;
import de.odysseus.staxon.json.stream.JsonStreamToken;

class JacksonStreamSource implements JsonStreamSource {
	private final JsonParser parser;

	private JsonStreamToken peek = null;
	private JsonLocation location = JsonLocation.NA;
	
	JacksonStreamSource(JsonParser parser) {
		this.parser = parser;
	}

	private JsonStreamToken read() throws IOException {
		JsonToken token = parser.nextToken();
		location = parser.getCurrentLocation();
		if (token == null) {
			return JsonStreamToken.NONE;
		}
		switch (token) {
		case FIELD_NAME:
			return JsonStreamToken.NAME;
		case VALUE_FALSE:
		case VALUE_TRUE:
		case VALUE_NULL:
		case VALUE_STRING:
		case VALUE_NUMBER_FLOAT:
		case VALUE_NUMBER_INT:
			return JsonStreamToken.VALUE;
		case START_OBJECT:
			return JsonStreamToken.START_OBJECT;
		case END_OBJECT:
			return JsonStreamToken.END_OBJECT;
		case START_ARRAY:
			return JsonStreamToken.START_ARRAY;
		case END_ARRAY:
			return JsonStreamToken.END_ARRAY;
		case NOT_AVAILABLE:
			return JsonStreamToken.NONE;
		default:
			throw new IllegalStateException("Unexpected GSON token: " + parser.getCurrentToken());
		}
	}
	
	private JacksonStreamSource expect(JsonStreamToken token) throws IOException {
		if (peek() != token) {
			throw new IllegalStateException("Expected token: " + token + ", but was: " + peek());
		}
		return this;
	}

	private void consume() {
		peek = null;
	}

	private String consume(String result) {
		peek = null;
		return result;
	}

	private String text() throws IOException {
		return parser.getCurrentToken() == JsonToken.VALUE_NULL ? null : parser.getText();
	}
	
	@Override
	public void endArray() throws IOException {
		expect(JsonStreamToken.END_ARRAY).consume();
	}

	@Override
	public void endObject() throws IOException {
		expect(JsonStreamToken.END_OBJECT).consume();
	}

	@Override
	public String name() throws IOException {
		return expect(JsonStreamToken.NAME).consume(parser.getCurrentName());
	}

	@Override
	public JsonStreamToken peek() throws IOException {
		return peek == null ? peek = read() : peek;
	}

	@Override
	public void startArray() throws IOException {
		expect(JsonStreamToken.START_ARRAY).consume();
	}

	@Override
	public void startObject() throws IOException {
		expect(JsonStreamToken.START_OBJECT).consume();
	}

	@Override
 	public String value() throws IOException {
		return expect(JsonStreamToken.VALUE).consume(text());
	}

	@Override
	public void close() throws IOException {
		parser.close();
	}

	@Override
	public int getLineNumber() {
		return location.getLineNr();
	}

	@Override
	public int getColumnNumber() {
		return location.getColumnNr();
	}

	@Override
	public int getCharacterOffset() {
		return (int)location.getCharOffset();
	}
	
	@Override
	public String getPublicId() {
		return null;
	}

	@Override
	public String getSystemId() {
		if (location.getSourceRef() instanceof File) {
			return ((File)location.getSourceRef()).toURI().toASCIIString();
		}
		if (location.getSourceRef() instanceof URL) {
			return ((URL)location.getSourceRef()).toExternalForm();
		}
		return null;
	}
}
