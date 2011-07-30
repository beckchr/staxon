package de.odysseus.staxon.json.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public interface JsonStreamTarget extends Closeable, Flushable {
	public void name(String name) throws IOException;
	public void value(String value) throws IOException;
	public void startObject() throws IOException;
	public void endObject() throws IOException;
	public void startArray() throws IOException;
	public void endArray() throws IOException;
}
