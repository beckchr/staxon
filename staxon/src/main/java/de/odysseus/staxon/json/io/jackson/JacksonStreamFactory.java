package de.odysseus.staxon.json.io.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;

import de.odysseus.staxon.json.io.JsonStreamFactory;
import de.odysseus.staxon.json.io.JsonStreamSource;
import de.odysseus.staxon.json.io.JsonStreamTarget;

public class JacksonStreamFactory implements JsonStreamFactory {
	private final JsonFactory jsonFactory;

	public JacksonStreamFactory() {
		this(new JsonFactory());
	}
	
	public JacksonStreamFactory(JsonFactory jsonFactory) {
		this.jsonFactory = jsonFactory;
	}
	
	protected JsonParser configure(JsonParser parser) {
		return parser.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
	}
	
	protected JsonGenerator configure(JsonGenerator generator, boolean pretty) {
		generator.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		if (pretty) {
			generator.useDefaultPrettyPrinter();
		}
		return generator;
	}

	public JsonStreamSource createJsonStreamSource(InputStream input) throws IOException {
		return new JacksonStreamSource(configure(jsonFactory.createJsonParser(input)));
	}
	
	public JsonStreamSource createJsonStreamSource(Reader reader) throws IOException {
		return new JacksonStreamSource(configure(jsonFactory.createJsonParser(reader)));
	}

	public JsonStreamTarget createJsonStreamTarget(OutputStream output, boolean pretty) throws IOException {
		return new JacksonStreamTarget(configure(jsonFactory.createJsonGenerator(output, JsonEncoding.UTF8), pretty));
	}
	
	public JsonStreamTarget createJsonStreamTarget(Writer writer, boolean pretty) throws IOException {
		return new JacksonStreamTarget(configure(jsonFactory.createJsonGenerator(writer), pretty));
	}
}
