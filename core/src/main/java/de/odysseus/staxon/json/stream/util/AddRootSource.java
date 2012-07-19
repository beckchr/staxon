/*
 * Copyright 2011, 2012 Odysseus Software GmbH
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
package de.odysseus.staxon.json.stream.util;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import de.odysseus.staxon.json.stream.JsonStreamSource;
import de.odysseus.staxon.json.stream.JsonStreamToken;

public class AddRootSource implements JsonStreamSource {
	private enum State {
		START_DOC,
		ROOT_NAME,
		ROOT_XMLNS_START,
		ROOT_XMLNS_NAME,
		ROOT_XMLNS_VALUE,
		DELEGATE,
		END_DOC
	}
	private final JsonStreamSource delegate;
	private final QName root;
	private final char namespaceSeparator;
	
	private State state = State.START_DOC;
	private int depth = 0;

	public AddRootSource(JsonStreamSource delegate, QName root, char namespaceSeparator) {
		this.delegate = delegate;
		this.root = root;
		this.namespaceSeparator = namespaceSeparator;
	}

	@Override
	public String name() throws IOException {
		if (state == State.ROOT_NAME) {
			if (XMLConstants.NULL_NS_URI.equals(root.getNamespaceURI())) {
				state = State.DELEGATE;
			} else { // declare namespace
				state = State.ROOT_XMLNS_START;
			}
			if (XMLConstants.DEFAULT_NS_PREFIX.equals(root.getPrefix())) {
				return root.getLocalPart();
			} else {
				return root.getPrefix() + namespaceSeparator + root.getLocalPart();
			}
		} else if (state == State.ROOT_XMLNS_NAME) {
			state = State.ROOT_XMLNS_VALUE;
			if (XMLConstants.DEFAULT_NS_PREFIX.equals(root.getPrefix())) {
				return '@' + XMLConstants.XMLNS_ATTRIBUTE;
			} else {
				return '@' + XMLConstants.XMLNS_ATTRIBUTE + namespaceSeparator + root.getLocalPart();
			}
		}
		return delegate.name();
	}

	@Override
	public String value() throws IOException {
		if (state == State.ROOT_XMLNS_VALUE) {
			state = State.DELEGATE;
			return root.getNamespaceURI();
		}
		return delegate.value();
	}

	@Override
	public void startObject() throws IOException {
		if (state == State.START_DOC) {
			state = State.ROOT_NAME;
		} else {
			if (state == State.ROOT_XMLNS_START) {
				state = State.ROOT_XMLNS_NAME;
			}
			delegate.startObject();
		}
		depth++;
	}

	@Override
	public void endObject() throws IOException {
		if (state == State.END_DOC) {
			state = null;
			return;
		}
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
		if (state == null) {
			return JsonStreamToken.NONE;
		}
		switch (state) {
		case START_DOC: return JsonStreamToken.START_OBJECT;
		case ROOT_NAME: return JsonStreamToken.NAME;
		case ROOT_XMLNS_NAME: return JsonStreamToken.NAME;
		case ROOT_XMLNS_VALUE: return JsonStreamToken.VALUE;
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
	
	@Override
	public int getLineNumber() {
		return delegate.getLineNumber();
	}
	
	@Override
	public int getColumnNumber() {
		return delegate.getColumnNumber();
	}
	
	@Override
	public int getCharacterOffset() {
		return delegate.getCharacterOffset();
	}
	
	@Override
	public String getPublicId() {
		return null;
	}

	@Override
	public String getSystemId() {
		return null;
	}
}
