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
package de.odysseus.staxon.json.stream;

import java.io.Closeable;
import java.io.IOException;

/**
 * JSON stream source.
 */
public interface JsonStreamSource extends Closeable {
	/**
	 * Consume {@link JsonStreamToken#NAME} token.
	 * @return name
	 * @throws IOException
	 */
	public String name() throws IOException;
	
	/**
	 * Consume {@link JsonStreamToken#VALUE} token.
	 * Numbers and booleans are reported as strings, <code>null</code> values are reported as <code>null</code>.
	 * @return value
	 * @throws IOException
	 */
	public String value() throws IOException;
	
	/**
	 * Consume {@link JsonStreamToken#START_OBJECT} token.
	 * @throws IOException
	 */
	public void startObject() throws IOException;

	/**
	 * Consume {@link JsonStreamToken#END_OBJECT} token.
	 * @throws IOException
	 */
	public void endObject() throws IOException;

	/**
	 * Consume {@link JsonStreamToken#START_ARRAY} token.
	 * @throws IOException
	 */
	public void startArray() throws IOException;
	
	/**
	 * Consume {@link JsonStreamToken#END_ARRAY} token.
	 * @throws IOException
	 */
	public void endArray() throws IOException;

	/**
	 * Peek next token.
	 * @return token
	 * @throws IOException
	 */
	public JsonStreamToken peek() throws IOException;
}
