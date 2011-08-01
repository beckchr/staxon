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
package de.odysseus.staxon.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import de.odysseus.staxon.json.io.JsonStreamFactory;
import de.odysseus.staxon.json.io.gson.GsonStreamFactory;
import de.odysseus.staxon.json.io.jackson.JacksonStreamFactory;

/**
 * Create JSON StAX readers and writers.
 */
public class JsonXMLStreamUtil {
	/**
	 * The name of the processing instruction used to indicate collections:
	 * <code>&lt;?xml-multiple bob?&gt;</code>
	 * <p>When using StAX to write a format other than XML (e.g. JSON),
	 * information about starting a "collection" as in
	 * <code>&lt;alice&gt;&lt;bob&gt;charlie&lt;/bob&gt;&lt;bob&gt;david&lt;/bob&gt;&lt;/alice&gt;</code>
	 * may be required by the writer. This PI may be used to pass the name
	 * of the following muliple element to the writer. Note that the element
	 * given in the PI may be written zero times, indicating an "empty array". 
	 */
	public static final String PI_MULTIPLE_TARGET = "xml-multiple";

	private static JsonStreamFactory factory;
	
	private static JsonStreamFactory getDefaultFactory() {
		try {
			return JacksonStreamFactory.class.newInstance();
		} catch (Exception e1) {
			try {
				return GsonStreamFactory.class.newInstance();
			} catch (Exception e2) {
				throw new RuntimeException("Failed to create JsonStreamFactory");
			}
		}
	}
	
	public static JsonStreamFactory getFactory() {
		if (factory == null) {
			factory = getDefaultFactory();
		}
		return factory;
	}
	
	public static void setFactory(JsonStreamFactory factory) {
		JsonXMLStreamUtil.factory = factory;
	}
	
	/**
	 * Create a JSON XML stream reader from the given input stream.
	 * @param input input stream
	 * @return XML stream reader
	 * @throws XMLStreamException
	 */
	public static XMLStreamReader createJsonXMLStreamReader(InputStream input) throws XMLStreamException {
		try {
			return new JsonXMLStreamReader(getFactory().createJsonStreamSource(input));
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	/**
	 * Create a JSON XML stream reader from the given reader.
	 * @param reader
	 * @return XML stream reader
	 * @throws XMLStreamException
	 */
	public static XMLStreamReader createJsonXMLStreamReader(Reader reader) throws XMLStreamException {
		try {
			return new JsonXMLStreamReader(getFactory().createJsonStreamSource(reader));
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	/**
	 * Create a JSON XML stream writer from the given output stream (using UTF-8 character encoding).
	 * @param output output stream
	 * @param pretty pretty printing
	 * @return XML stream writer
	 * @throws XMLStreamException
	 */
	public static XMLStreamWriter createJsonXMLStreamWriter(OutputStream output, boolean pretty) throws XMLStreamException {
		try {
			return new JsonXMLStreamWriter(getFactory().createJsonStreamTarget(output, pretty));
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	/**
	 * Create a JSON XML stream writer from the given writer.
	 * @param writer writer
	 * @param pretty pretty printing
	 * @return XML stream writer
	 * @throws XMLStreamException
	 */
	public static XMLStreamWriter createJsonXMLStreamWriter(Writer writer, boolean pretty) throws XMLStreamException {
		try {
			return new JsonXMLStreamWriter(getFactory().createJsonStreamTarget(writer, pretty));
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}
}
