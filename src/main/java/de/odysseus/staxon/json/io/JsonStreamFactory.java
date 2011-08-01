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
package de.odysseus.staxon.json.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public interface JsonStreamFactory {
	public JsonStreamSource createJsonStreamSource(InputStream input) throws IOException;
	public JsonStreamSource createJsonStreamSource(Reader reader) throws IOException;
		
	public JsonStreamTarget createJsonStreamTarget(OutputStream output, boolean pretty) throws IOException;
	public JsonStreamTarget createJsonStreamTarget(Writer writer, boolean pretty) throws IOException;
}
