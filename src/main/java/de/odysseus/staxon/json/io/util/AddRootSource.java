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

	public String name() throws IOException {
		if (state == State.ROOT_NAME) {
			state = State.DELEGATE;
			return root;
		}
		return delegate.name();
	}

	public String value() throws IOException {
		return delegate.value();
	}

	public void startObject() throws IOException {
		if (state == State.START_DOC) {
			state = State.ROOT_NAME;
		} else {
			delegate.startObject();
		}
		depth++;
	}

	public void endObject() throws IOException {
		if (depth == 1 && state == State.DELEGATE && delegate.peek() == JsonStreamToken.NONE) {
			state = State.END_DOC;
		}
		if (state != State.END_DOC) {
			delegate.endObject();
		}
		depth--;
	}

	public void startArray() throws IOException {
		delegate.startArray();
	}

	public void endArray() throws IOException {
		delegate.endArray();
	}

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

	public void close() throws IOException {
		delegate.close();
	}
}
