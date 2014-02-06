/*
 * Copyright 2011-2014 Odysseus Software GmbH
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
package de.odysseus.staxon.json.stream.jsr353;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.json.JsonException;
import javax.json.stream.JsonGenerator;

import de.odysseus.staxon.json.stream.JsonStreamTarget;

/**
 * JSON-P (<code>javax.json</code>) <code>JsonStreamTarget</code> implementation.
 */
public class JsonProcessingStreamTarget implements JsonStreamTarget {
	private final JsonGenerator generator;
	private String name;

	public JsonProcessingStreamTarget(JsonGenerator generator) {
		this.generator = generator;
	}

	@Override
	public void close() throws IOException {
		try {
			generator.close();
		} catch (JsonException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			} else {
				throw e;
			}
		}
	}

	@Override
	public void flush() throws IOException {
		try {
			generator.flush();
		} catch (JsonException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			} else {
				throw e;
			}
		}
	}

	@Override
	public void name(String name) throws IOException {
		if (this.name != null) {
			throw new IOException("expected JSON value");
		}
		this.name = name;
	}

	@Override
	public void value(Object value) throws IOException {
		try {
			if (name != null) {
				if (value == null) {
					generator.writeNull(name);
				} else if (value instanceof String) {
					generator.write(name, (String) value);
				} else if (value instanceof Number) {
					if (value instanceof BigDecimal) {
						generator.write(name, (BigDecimal) value);
					} else if (value instanceof BigInteger) {
						generator.write(name, (BigInteger) value);
					} else if (value instanceof Long) {
						generator.write(name, (Long) value);
					} else if (value instanceof Integer) {
						generator.write(name, (Integer) value);
					} else {
						generator.write(name, ((Number) value).doubleValue());
					}
				} else if (value instanceof Boolean) {
					generator.write(name, (Boolean) value);
				} else {
					throw new IOException("Cannot write value: " + value);
				}
				name = null;
			} else {
				if (value == null) {
					generator.writeNull();
				} else if (value instanceof String) {
					generator.write((String) value);
				} else if (value instanceof Number) {
					if (value instanceof BigDecimal) {
						generator.write((BigDecimal) value);
					} else if (value instanceof BigInteger) {
						generator.write((BigInteger) value);
					} else if (value instanceof Long) {
						generator.write((Long) value);
					} else if (value instanceof Integer) {
						generator.write((Integer) value);
					} else {
						generator.write(((Number) value).doubleValue());
					}
				} else if (value instanceof Boolean) {
					generator.write((Boolean) value);
				} else {
					throw new IOException("Cannot write value: " + value);
				}
			}
		} catch (JsonException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			} else {
				throw e;
			}
		}
	}

	@Override
	public void startObject() throws IOException {
		try {
			if (name != null) {
				generator.writeStartObject(name);
				name = null;
			} else {
				generator.writeStartObject();
			}
		} catch (JsonException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			} else {
				throw e;
			}
		}
	}

	@Override
	public void endObject() throws IOException {
		if (name != null) {
			throw new IOException("expected JSON value");
		}
		try {
			generator.writeEnd();
		} catch (JsonException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			} else {
				throw e;
			}
		}
	}

	@Override
	public void startArray() throws IOException {
		try {
			if (name != null) {
				generator.writeStartArray(name);
				name = null;
			} else {
				generator.writeStartArray();
			}
		} catch (JsonException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			} else {
				throw e;
			}
		}
	}

	@Override
	public void endArray() throws IOException {
		if (name != null) {
			throw new IOException("expected JSON value");
		}
		try {
			generator.writeEnd();
		} catch (JsonException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			} else {
				throw e;
			}
		}
	}
}
