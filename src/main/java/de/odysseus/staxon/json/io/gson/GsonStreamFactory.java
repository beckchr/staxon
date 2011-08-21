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

public class GsonStreamFactory extends JsonStreamFactory {
	@Override
	public JsonStreamSource createJsonStreamSource(InputStream input) throws IOException {
		return createJsonStreamSource(new InputStreamReader(input));
	}
	
	@Override
	public JsonStreamSource createJsonStreamSource(Reader reader) {
		JsonReader jsonReader = new JsonReader(reader);
		jsonReader.setLenient(false);
		return new GsonStreamSource(jsonReader);
	}

	@Override
	public JsonStreamTarget createJsonStreamTarget(OutputStream output, boolean pretty) throws IOException {
		return createJsonStreamTarget(new OutputStreamWriter(output), pretty);
	}
	
	@Override
	public JsonStreamTarget createJsonStreamTarget(Writer writer, boolean pretty) {
		JsonWriter jsonWriter = new JsonWriter(new BufferedWriter(writer));
		jsonWriter.setLenient(false);
		jsonWriter.setIndent(pretty ? "\t" : "");
		return new GsonStreamTarget(jsonWriter);
	}
}
