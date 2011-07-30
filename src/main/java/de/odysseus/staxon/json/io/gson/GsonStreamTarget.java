package de.odysseus.staxon.json.io.gson;

import java.io.IOException;

import com.google.gson.stream.JsonWriter;

import de.odysseus.staxon.json.io.JsonStreamTarget;

class GsonStreamTarget implements JsonStreamTarget {
	private final JsonWriter writer;
	
	GsonStreamTarget(JsonWriter writer) {
		this.writer = writer;
	}

	public void endArray() throws IOException {
		writer.endArray();
	}

	public void endObject() throws IOException {
		writer.endObject();
	}

	public void name(String name) throws IOException {
		writer.name(name);
	}

	public void startArray() throws IOException {
		writer.beginArray();
	}

	public void startObject() throws IOException {
		writer.beginObject();
	}

	public void value(String value) throws IOException {
		if (value == null) {
			writer.nullValue();
		} else {
			writer.value(value);
		}
	}

	public void flush() throws IOException {
		writer.flush();
	}
	
	public void close() throws IOException {
		writer.close();
	}
}
