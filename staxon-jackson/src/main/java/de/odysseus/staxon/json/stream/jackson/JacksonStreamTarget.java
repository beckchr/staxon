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

import org.codehaus.jackson.JsonGenerator;

import de.odysseus.staxon.json.stream.JsonStreamTarget;

class JacksonStreamTarget implements JsonStreamTarget {
	private final JsonGenerator generator;
	
	JacksonStreamTarget(JsonGenerator generator) {
		this.generator = generator;
	}

	@Override
	public void endArray() throws IOException {
		generator.writeEndArray();
	}

	@Override
	public void endObject() throws IOException {
		generator.writeEndObject();
	}

	@Override
	public void name(String name) throws IOException {
		generator.writeFieldName(name);
	}

	@Override
	public void startArray() throws IOException {
		generator.writeStartArray();
	}

	@Override
	public void startObject() throws IOException {
		generator.writeStartObject();
	}

	@Override
	public void value(String value) throws IOException {
		if (value == null) {
			generator.writeNull();
		} else {
			generator.writeString(value);
		}
	}

	@Override
	public void flush() throws IOException {
		generator.flush();
	}

	@Override
	public void close() throws IOException {
		generator.close();
	}
}
