package de.odysseus.staxon.json.io.gson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import de.odysseus.staxon.json.io.JsonStreamFactory;
import de.odysseus.staxon.json.io.JsonStreamSource;
import de.odysseus.staxon.json.io.JsonStreamTarget;

public class GsonStreamFactory implements JsonStreamFactory {
	public JsonStreamSource createJsonStreamSource(InputStream input) throws IOException {
		return createJsonStreamSource(new InputStreamReader(input));
	}
	
	public JsonStreamSource createJsonStreamSource(Reader reader) {
		JsonReader jsonReader = new JsonReader(reader);
		jsonReader.setLenient(false);
		return new GsonStreamSource(jsonReader);
	}

	public JsonStreamTarget createJsonStreamTarget(OutputStream output, boolean pretty) throws IOException {
		return createJsonStreamTarget(new OutputStreamWriter(output), pretty);
	}
	
	public JsonStreamTarget createJsonStreamTarget(Writer writer, boolean pretty) {
		JsonWriter jsonWriter = new JsonWriter(new BufferedWriter(writer));
		jsonWriter.setLenient(false);
		jsonWriter.setIndent(pretty ? "\t" : "");
		return new GsonStreamTarget(jsonWriter);
	}
}
