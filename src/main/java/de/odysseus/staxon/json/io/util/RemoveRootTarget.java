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
