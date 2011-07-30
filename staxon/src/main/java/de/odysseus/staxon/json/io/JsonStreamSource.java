package de.odysseus.staxon.json.io;

import java.io.Closeable;
import java.io.IOException;

public interface JsonStreamSource extends Closeable {
	public String name() throws IOException;
	public String value() throws IOException;
	public void startObject() throws IOException;
	public void endObject() throws IOException;
	public void startArray() throws IOException;
	public void endArray() throws IOException;

	public JsonStreamToken peek() throws IOException;
}
