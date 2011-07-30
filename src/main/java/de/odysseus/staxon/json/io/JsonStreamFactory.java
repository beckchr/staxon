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
