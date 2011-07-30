package de.odysseus.staxon.json.io.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import de.odysseus.staxon.json.io.JsonStreamSource;
import de.odysseus.staxon.json.io.JsonStreamToken;

class JacksonStreamSource implements JsonStreamSource {
	private final JsonParser parser;
	private JsonStreamToken peek;
	
	JacksonStreamSource(JsonParser parser) {
		this.parser = parser;
	}

	private JsonStreamToken read() throws IOException {
		JsonToken token = parser.nextToken();
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
	
	public void endArray() throws IOException {
		expect(JsonStreamToken.END_ARRAY).consume();
	}

	public void endObject() throws IOException {
		expect(JsonStreamToken.END_OBJECT).consume();
	}

	public String name() throws IOException {
		return expect(JsonStreamToken.NAME).consume(parser.getCurrentName());
	}

	public JsonStreamToken peek() throws IOException {
		return peek == null ? peek = read() : peek;
	}

	public void startArray() throws IOException {
		expect(JsonStreamToken.START_ARRAY).consume();
	}

	public void startObject() throws IOException {
		expect(JsonStreamToken.START_OBJECT).consume();
	}

	public String value() throws IOException {
		return expect(JsonStreamToken.VALUE).consume(text());
	}

	public void close() throws IOException {
		parser.close();
	}
}
