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
package de.odysseus.staxon.json.stream.impl;

import java.io.IOException;
import java.io.Writer;

import de.odysseus.staxon.json.stream.JsonStreamTarget;

/**
 * Simple <code>JsonStreamTarget</code> implementation.
 */
class JsonStreamTargetImpl implements JsonStreamTarget {
	private final Writer writer;
	private final int[] namePos = new int[64];
	private final int[] arrayPos = new int[64];
	private final StringBuilder buffer = new StringBuilder();
	private final boolean closeWriter;
	
	private final boolean pretty;
	private final String[] indent;
	private final String newline;

	private int depth = 0;
	
	JsonStreamTargetImpl(Writer writer, boolean closeWriter, boolean pretty) {
		this.writer = writer;
		this.closeWriter = closeWriter;
		this.pretty = pretty;
		
		if (pretty) {
			this.newline = "\n";
			this.indent = new String[64];
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < 64; i++) {
				indent[i] = builder.toString();
				builder.append("\t");
			}
		} else {
			this.newline = null;
			this.indent = null;
		}
	}
	
	private String encode(String value) {
		buffer.setLength(0);
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '"':
				buffer.append("\\\"");
				break;
			case '\\':
				buffer.append("\\\\");
				break;
			case '\b':
				buffer.append("\\b");
				break;
			case '\f':
				buffer.append("\\f");
				break;
			case '\n':
				buffer.append("\\n");
				break;
			case '\r':
				buffer.append("\\r");
				break;
			case '\t':
				buffer.append("\\t");
				break;
			default:
				if (c < ' ') {
					buffer.append(String.format("\\u%04X", (int) c));
				} else {
					buffer.append(c);
				}
			}
		}
		return buffer.toString();
	}

	@Override
	public void close() throws IOException {
		if (closeWriter) {
			writer.close();
		} else {
			writer.flush();
		}
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}

	@Override
	public void name(String name) throws IOException {
		if (namePos[depth] > 1) {
			writer.write(',');
		}
		namePos[depth]++;
		if (pretty) {
			writer.write(newline);
			writer.write(indent[depth]);
		}
		writer.write('"');
		writer.write(name);
		writer.write('"');
		if (pretty) {
			writer.write(' ');
		}
		writer.write(':');
	}

	@Override
	public void value(String value) throws IOException {
		if (arrayPos[depth] > 0) {
			if (arrayPos[depth] > 1) {
				writer.write(',');
			}
			arrayPos[depth]++;
		}
		if (pretty) {
			writer.write(' ');
		}
		if (value == null) {
			writer.write("null");
		} else {
			writer.write('"');
			writer.write(encode(value));
			writer.write('"');
		}
	}

	@Override
	public void startObject() throws IOException {
		if (arrayPos[depth] > 0) {
			if (arrayPos[depth] > 1) {
				writer.write(',');
			}
			arrayPos[depth]++;
		}
		if (pretty && (depth > 0 || arrayPos[depth] > 0)) {
			writer.write(' ');
		}
		writer.write('{');
		depth++;
		namePos[depth] = 1;
	}

	@Override
	public void endObject() throws IOException {
		namePos[depth] = 0;
		depth--;
		if (pretty) {
			writer.write(newline);
			writer.write(indent[depth]);
		}
		writer.write('}');
		if (depth == 0) {
			writer.flush();
		}
	}

	@Override
	public void startArray() throws IOException {
		if (arrayPos[depth] > 0) {
			throw new IOException("Nested arrays are not supported!");
		}
		if (pretty && depth > 0) {
			writer.write(' ');
		}
		writer.write('[');
		arrayPos[depth] = 1;
	}

	@Override
	public void endArray() throws IOException {
		arrayPos[depth] = 0;
		if (pretty) {
			writer.write(' ');
		}
		writer.write(']');
	}
}
