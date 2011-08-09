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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;

import de.odysseus.staxon.json.io.JsonStreamFactory;
import de.odysseus.staxon.json.io.gson.GsonStreamFactory;
import de.odysseus.staxon.json.io.jackson.JacksonStreamFactory;

public class JsonXMLOutputFactory extends XMLOutputFactory {
	/**
	 * <p>JSON stream factory.</p>
	 * 
	 * <p>The default is to try <em>Jackson</em> first, the <em>Gson</em>.</p>
	 */
	public static final String PROP_STREAM_FACTORY = "JsonXMLOutputFactory.streamFactory";
	
	/**
	 * <p>Start/end arrays automatically?</p>
	 * 
	 * <p>The default value is <code>false</code>.</p>
	 */
	public static final String PROP_AUTO_ARRAY = "JsonXMLOutputFactory.autoArray";
	
	/**
	 * <p>Whether to use the {@link JsonXMLStreamConstants#MULTIPLE_PI_TARGET}
	 * processing instruction target to trigger an array start.
	 * If <code>true</code>, a PI is used to inform the writer to begin an array,
	 * passing the name of following multiple elements as data.
	 * The writer will close arrays automatically.</p>
	 *  
	 * <p>Note that the element given in the PI may be written zero times,
	 * indicating an empty array.</p>
	 * 
	 * <p>The default value is true.</p>
	 */
	public static final String PROP_MULTIPLE_PI = "JsonXMLOutputFactory.multiplePI";

	/**
	 * Format output for better readability?
	 * 
	 * <p>The default value is <code>false</code>.</p>
	 */
	public static final String PROP_PRETTY_PRINT = "JsonXMLOutputFactory.prettyPrint";

	private static JsonStreamFactory getDefaultFactory() {
		try {
			return JacksonStreamFactory.class.newInstance();
		} catch (NoClassDefFoundError e) {
			try {
				return GsonStreamFactory.class.newInstance();
			} catch (Exception e2) {
				throw new RuntimeException("Failed to create GsonStreamFactory", e);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to create JacksonStreamFactory", e);
		}
	}
	
	private JsonStreamFactory streamFactory = null;
	private boolean multiplePI = true;
	private boolean autoArray = false;
	private boolean prettyPrint = false;

	private JsonStreamFactory streamFactory() {
		if (streamFactory == null) {
			streamFactory = getDefaultFactory();
		}
		return streamFactory;
	}

	@Override
	public JsonXMLStreamWriter createXMLStreamWriter(Writer stream) throws XMLStreamException {
		try {
			return new JsonXMLStreamWriter(streamFactory().createJsonStreamTarget(stream, prettyPrint), multiplePI);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public JsonXMLStreamWriter createXMLStreamWriter(OutputStream stream) throws XMLStreamException {
		try {
			return new JsonXMLStreamWriter(streamFactory().createJsonStreamTarget(stream, prettyPrint), multiplePI);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public JsonXMLStreamWriter createXMLStreamWriter(OutputStream stream, String encoding) throws XMLStreamException {
		try {
			return createXMLStreamWriter(new OutputStreamWriter(stream, encoding));
		} catch (UnsupportedEncodingException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public JsonXMLStreamWriter createXMLStreamWriter(Result result) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventWriter createXMLEventWriter(Result result) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventWriter createXMLEventWriter(OutputStream stream) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventWriter createXMLEventWriter(OutputStream stream, String encoding) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventWriter createXMLEventWriter(Writer stream) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setProperty(String name, Object value) throws IllegalArgumentException {
		if (XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals(name)) {
			if (Boolean.valueOf(value.toString())) {
				throw new IllegalArgumentException();
			}
		} else { // proprietary properties
			if (PROP_AUTO_ARRAY.equals(name)) {
				autoArray = Boolean.valueOf(value.toString());
			} else if (PROP_MULTIPLE_PI.equals(name)) {
				multiplePI = Boolean.valueOf(value.toString());;
			} else if (PROP_PRETTY_PRINT.equals(name)) {
				prettyPrint = Boolean.valueOf(value.toString());
			} else if (PROP_STREAM_FACTORY.equals(name)) {
				streamFactory = (JsonStreamFactory)value;
			} else {
				throw new IllegalArgumentException("Unsupported output property: " + name);
			}
		}
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		if (XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals(name)) {
			return false;
		} else { // proprietary properties
			 if (PROP_AUTO_ARRAY.equals(name)) {
				return autoArray;
			} else if (PROP_MULTIPLE_PI.equals(name)) {
				return multiplePI;
			} else if (PROP_PRETTY_PRINT.equals(name)) {
				return prettyPrint;
			} else if (PROP_STREAM_FACTORY.equals(name)) {
				return streamFactory;
			} else {
				throw new IllegalArgumentException("Unsupported output property: " + name);
			}
		}
	}

	@Override
	public boolean isPropertySupported(String name) {
		if (XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals(name)) {
			return true;
		} else { // proprietary properties
			return Arrays.asList(PROP_AUTO_ARRAY, PROP_MULTIPLE_PI, PROP_PRETTY_PRINT, PROP_STREAM_FACTORY).contains(name);
		}
	}
}
