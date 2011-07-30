package de.odysseus.staxon.json.io.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;

import de.odysseus.staxon.json.io.JsonStreamTarget;

class JacksonStreamTarget implements JsonStreamTarget {
	private final JsonGenerator generator;
	
	JacksonStreamTarget(JsonGenerator generator) {
		this.generator = generator;
	}

	public void endArray() throws IOException {
		generator.writeEndArray();
	}

	public void endObject() throws IOException {
		generator.writeEndObject();
	}

	public void name(String name) throws IOException {
		generator.writeFieldName(name);
	}

	public void startArray() throws IOException {
		generator.writeStartArray();
	}

	public void startObject() throws IOException {
		generator.writeStartObject();
	}

	public void value(String value) throws IOException {
		if (value == null) {
			generator.writeNull();
		} else {
			generator.writeString(value);
		}
	}

	public void flush() throws IOException {
		generator.flush();
	}

	public void close() throws IOException {
		generator.close();
	}
}
