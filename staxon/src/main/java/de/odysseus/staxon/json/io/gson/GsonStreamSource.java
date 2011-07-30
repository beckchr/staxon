package de.odysseus.staxon.json.io.gson;

import java.io.IOException;

import com.google.gson.stream.JsonReader;

import de.odysseus.staxon.json.io.JsonStreamSource;
import de.odysseus.staxon.json.io.JsonStreamToken;

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

	public void endArray() throws IOException {
		consume(JsonStreamToken.END_ARRAY);
		reader.endArray();
	}

	public void endObject() throws IOException {
		consume(JsonStreamToken.END_OBJECT);
		reader.endObject();
	}

	public String name() throws IOException {
		consume(JsonStreamToken.NAME);
		return reader.nextName();
	}

	public JsonStreamToken peek() throws IOException {
		return peek == null ? peek = read() : peek;
	}

	public void startArray() throws IOException {
		consume(JsonStreamToken.START_ARRAY);
		reader.beginArray();
	}

	public void startObject() throws IOException {
		consume(JsonStreamToken.START_OBJECT);
		reader.beginObject();
	}

	public String value() throws IOException {
		consume(JsonStreamToken.VALUE);
		switch (reader.peek()) {
		case BOOLEAN:
			return String.valueOf(reader.nextBoolean());
		case NULL:
			reader.nextNull();
			return null;
		case NUMBER:
		case STRING:
			return reader.nextString();
		default:
			throw new IllegalStateException("Not a value token: " + peek());
		}
	}

	public void close() throws IOException {
		reader.close();
	}
}
