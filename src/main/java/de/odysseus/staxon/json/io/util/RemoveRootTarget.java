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
package de.odysseus.staxon.json.io.util;

import java.io.IOException;

import de.odysseus.staxon.json.io.JsonStreamTarget;

public class RemoveRootTarget implements JsonStreamTarget {
	private final JsonStreamTarget delegate;
	
	private int depth;

	public RemoveRootTarget(JsonStreamTarget delegate) {
		this.delegate = delegate;
	}

	@Override
	public void name(String name) throws IOException {
		if (depth > 1) {
			delegate.name(name);
		}
	}

	@Override
	public void value(String value) throws IOException {
		delegate.value(value);
	}

	@Override
	public void startObject() throws IOException {
		if (depth++ > 0) {
			delegate.startObject();
		}
	}

	@Override
	public void endObject() throws IOException {
		if (--depth > 0) {
			delegate.endObject();
		}
	}

	@Override
	public void startArray() throws IOException {
		delegate.startArray();
	}

	@Override
	public void endArray() throws IOException {
		delegate.endArray();
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
