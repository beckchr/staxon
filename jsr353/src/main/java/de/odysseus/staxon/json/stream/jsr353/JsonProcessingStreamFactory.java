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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParserFactory;

import de.odysseus.staxon.json.stream.JsonStreamFactory;
import de.odysseus.staxon.json.stream.JsonStreamSource;
import de.odysseus.staxon.json.stream.JsonStreamTarget;

/**
 * JSON-P (<code>javax.json</code>) <code>JsonStreamFactory</code> implementation.
 */
public class JsonProcessingStreamFactory extends JsonStreamFactory {
	private final JsonProvider provider;
	
	/**
	 * Create factory using the default provider to create parsers/generators
	 */
	public JsonProcessingStreamFactory() {
		this(JsonProvider.provider());
	}
	
	/**
	 * Create factory using the specified provider to create parsers/generators
	 */
	public JsonProcessingStreamFactory(JsonProvider provider) {
		this.provider = provider;
	}
	
	/**
	 * @return configuration used to create <code>javax.json.stream.JsonParser</code>
	 */
	protected Map<String, ?> getParserConfig() {
		return null;
	}
	
	/**
	 * @return configuration used to create <code>javax.json.stream.JsonGenerator</code>
	 */
	protected Map<String, ?> getGeneratorConfig(boolean pretty) {
		Map<String, Object> config = null;
		if (pretty) {
			config = new HashMap<String, Object>();
			config.put(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
		}
		return config;
	}
	
	private JsonParserFactory createJsonParserFactory() {
		return provider.createParserFactory(getParserConfig());
	}
	
	private JsonGeneratorFactory createJsonGeneratorFactory(boolean pretty) {
		return provider.createGeneratorFactory(getGeneratorConfig(pretty));
	}
	
	@Override
	public JsonStreamSource createJsonStreamSource(InputStream input) throws IOException {
		return new JsonProcessingStreamSource(createJsonParserFactory().createParser(input));
	}
	
	@Override
	public JsonStreamSource createJsonStreamSource(Reader reader) {
		return new JsonProcessingStreamSource(createJsonParserFactory().createParser(reader));
	}

	@Override
	public JsonStreamTarget createJsonStreamTarget(OutputStream output, boolean pretty) throws IOException {
		return new JsonProcessingStreamTarget(createJsonGeneratorFactory(pretty).createGenerator(output));
	}
	
	@Override
	public JsonStreamTarget createJsonStreamTarget(Writer writer, boolean pretty) {
		return new JsonProcessingStreamTarget(createJsonGeneratorFactory(pretty).createGenerator(writer));
	}
}
