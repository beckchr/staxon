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
package de.odysseus.staxon.json.stream.gson;

import java.io.IOException;

import com.google.gson.stream.JsonReader;

import de.odysseus.staxon.json.stream.JsonStreamSource;
import de.odysseus.staxon.json.stream.JsonStreamToken;

class GsonStreamSource implements JsonStreamSource {
	private final JsonReader reader;
	private JsonStreamToken peek;

	GsonStreamSource(JsonReader reader) {
		this.reader = reader;
	}

	private void consume(JsonStreamToken token) throws IOException {
		if (peek() != token) {
			throw new IllegalStateException("Expected token: " + token + ", but was: " + peek());
		}
		peek = null;
	}

	private JsonStreamToken read() throws IOException {
		switch (reader.peek()) {
		case NAME:
			return JsonStreamToken.NAME;
		case BOOLEAN:
		case NULL:
		case NUMBER:
		case STRING:
			return JsonStreamToken.VALUE;
		case BEGIN_OBJECT:
			return JsonStreamToken.START_OBJECT;
		case END_OBJECT:
			return JsonStreamToken.END_OBJECT;
		case BEGIN_ARRAY:
			return JsonStreamToken.START_ARRAY;
		case END_ARRAY:
			return JsonStreamToken.END_ARRAY;
		case END_DOCUMENT:
			return JsonStreamToken.NONE;
		default:
			throw new IllegalStateException("Unexpected GSON token: " + reader.peek());
		}
	}

	@Override
	public void endArray() throws IOException {
		consume(JsonStreamToken.END_ARRAY);
		reader.endArray();
	}

	@Override
	public void endObject() throws IOException {
		consume(JsonStreamToken.END_OBJECT);
		reader.endObject();
	}

	@Override
	public String name() throws IOException {
		consume(JsonStreamToken.NAME);
		return reader.nextName();
	}

	@Override
	public JsonStreamToken peek() throws IOException {
		return peek == null ? peek = read() : peek;
	}

	@Override
	public void startArray() throws IOException {
		consume(JsonStreamToken.START_ARRAY);
		reader.beginArray();
	}

	@Override
	public void startObject() throws IOException {
		consume(JsonStreamToken.START_OBJECT);
		reader.beginObject();
	}

	@Override
	public Value value() throws IOException {
		consume(JsonStreamToken.VALUE);
		switch (reader.peek()) {
		case BOOLEAN:
			return reader.nextBoolean() ? TRUE : FALSE;
		case NULL:
			reader.nextNull();
			return NULL;
		case NUMBER:
			String s = reader.nextString();
			try {
				return new Value(s, Long.valueOf(s));
			} catch (NumberFormatException e) {
				return new Value(s, Double.valueOf(s));
			}
		case STRING:
			return new Value(reader.nextString());
		default:
			throw new IOException("Not a value token: " + peek());
		}
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	@Override
	public int getLineNumber() {
		return -1;
	}

	@Override
	public int getColumnNumber() {
		return -1;
	}

	@Override
	public int getCharacterOffset() {
		return -1;
	}
	
	@Override
	public String getPublicId() {
		return null;
	}

	@Override
	public String getSystemId() {
		return null;
	}
}
