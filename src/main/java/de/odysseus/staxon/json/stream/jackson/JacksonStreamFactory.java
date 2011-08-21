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
package de.odysseus.staxon.json.stream.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;

import de.odysseus.staxon.json.stream.JsonStreamFactory;
import de.odysseus.staxon.json.stream.JsonStreamSource;
import de.odysseus.staxon.json.stream.JsonStreamTarget;

public class JacksonStreamFactory extends JsonStreamFactory {
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

	@Override
	public JsonStreamSource createJsonStreamSource(InputStream input) throws IOException {
		return new JacksonStreamSource(configure(jsonFactory.createJsonParser(input)));
	}
	
	@Override
	public JsonStreamSource createJsonStreamSource(Reader reader) throws IOException {
		return new JacksonStreamSource(configure(jsonFactory.createJsonParser(reader)));
	}

	@Override
	public JsonStreamTarget createJsonStreamTarget(OutputStream output, boolean pretty) throws IOException {
		return new JacksonStreamTarget(configure(jsonFactory.createJsonGenerator(output, JsonEncoding.UTF8), pretty));
	}
	
	@Override
	public JsonStreamTarget createJsonStreamTarget(Writer writer, boolean pretty) throws IOException {
		return new JacksonStreamTarget(configure(jsonFactory.createJsonGenerator(writer), pretty));
	}
}
