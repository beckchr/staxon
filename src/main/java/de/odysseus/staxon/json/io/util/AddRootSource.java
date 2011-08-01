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

import de.odysseus.staxon.json.io.JsonStreamSource;
import de.odysseus.staxon.json.io.JsonStreamToken;

public class AddRootSource implements JsonStreamSource {
	private enum State {
		START_DOC,
		ROOT_NAME,
		DELEGATE,
		END_DOC
	}
	private final JsonStreamSource delegate;
	private final String root;
	
	private State state = State.START_DOC;
	private int depth = 0;

	public AddRootSource(JsonStreamSource delegate, String root) {
		this.delegate = delegate;
		this.root = root;
	}

	@Override
	public String name() throws IOException {
		if (state == State.ROOT_NAME) {
			state = State.DELEGATE;
			return root;
		}
		return delegate.name();
	}

	@Override
	public String value() throws IOException {
		return delegate.value();
	}

	@Override
	public void startObject() throws IOException {
		if (state == State.START_DOC) {
			state = State.ROOT_NAME;
		} else {
			delegate.startObject();
		}
		depth++;
	}

	@Override
	public void endObject() throws IOException {
		if (depth == 1 && state == State.DELEGATE && delegate.peek() == JsonStreamToken.NONE) {
			state = State.END_DOC;
		}
		if (state != State.END_DOC) {
			delegate.endObject();
		}
		depth--;
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
	public JsonStreamToken peek() throws IOException {
		switch (state) {
		case START_DOC: return JsonStreamToken.START_OBJECT;
		case ROOT_NAME: return JsonStreamToken.NAME;
		case END_DOC: return JsonStreamToken.END_OBJECT;
		}
		JsonStreamToken result = delegate.peek();
		if (depth == 1 && result == JsonStreamToken.NONE) {
			result = JsonStreamToken.END_OBJECT;
		}
		return result;
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
