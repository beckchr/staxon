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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;

import de.odysseus.staxon.json.io.JsonStreamFactory;
import de.odysseus.staxon.json.io.gson.GsonStreamFactory;
import de.odysseus.staxon.json.io.jackson.JacksonStreamFactory;

public class JsonXMLInputFactory extends XMLInputFactory {
	/**
	 * <p>JSON stream factory.</p>
	 * 
	 * <p>The default is to try <em>Jackson</em> first, the <em>Gson</em>.</p>
	 */
	public static final String PROP_STREAM_FACTORY = "JsonXMLInputFactory.streamFactory";
	
	/**
	 * <p>Whether to use the {@link JsonXMLStreamConstants#MULTIPLE_PI_TARGET}
	 * processing instruction to indicate an array start.
	 * If not <code>true</code>, this reader will insert a PI with the field
	 * name as PI data. 
	 *  
	 * <p>Note that the element given in the PI may occur zero times,
	 * indicating an "empty array".</p>
	 * 
	 * <p>The default value is <code>true</code>.</p>
	 */
	public static final String PROP_MULTIPLE_PI = "JsonXMLInputFactory.multiplePI";

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
	private boolean coalescing;
	
	private JsonStreamFactory streamFactory() {
		if (streamFactory == null) {
			streamFactory = getDefaultFactory();
		}
		return streamFactory;
	}
	
	@Override
	public JsonXMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
		try {
			return new JsonXMLStreamReader(streamFactory().createJsonStreamSource(reader), multiplePI);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public JsonXMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
		try {
			return new JsonXMLStreamReader(streamFactory().createJsonStreamSource(stream), multiplePI);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public JsonXMLStreamReader createXMLStreamReader(InputStream stream, String encoding) throws XMLStreamException {
		try {
			return createXMLStreamReader(new InputStreamReader(stream, encoding));
		} catch (UnsupportedEncodingException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public JsonXMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public JsonXMLStreamReader createXMLStreamReader(String systemId, InputStream stream) throws XMLStreamException {
		return createXMLStreamReader(stream);
	}

	@Override
	public JsonXMLStreamReader createXMLStreamReader(String systemId, Reader reader) throws XMLStreamException {
		return createXMLStreamReader(reader);
	}

	@Override
	public XMLEventReader createXMLEventReader(Reader reader) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventReader createXMLEventReader(String systemId, Reader reader) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventReader createXMLEventReader(XMLStreamReader reader) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventReader createXMLEventReader(Source source) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventReader createXMLEventReader(InputStream stream) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventReader createXMLEventReader(InputStream stream, String encoding) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventReader createXMLEventReader(String systemId, InputStream stream) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLStreamReader createFilteredReader(XMLStreamReader reader, StreamFilter filter) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventReader createFilteredReader(XMLEventReader reader, EventFilter filter) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLResolver getXMLResolver() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setXMLResolver(XMLResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLReporter getXMLReporter() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setXMLReporter(XMLReporter reporter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setProperty(String name, Object value) throws IllegalArgumentException {
		if (XMLInputFactory.IS_COALESCING.equals(name)) {
			coalescing = Boolean.valueOf(value.toString());
		} else if (XMLInputFactory.IS_NAMESPACE_AWARE.equals(name)) {
			if (!Boolean.valueOf(value.toString())) {
				throw new IllegalArgumentException();
			}
		} else if (XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES.equals(name)) {
			if (!Boolean.valueOf(value.toString())) {
				throw new IllegalArgumentException();
			}
		} else if (XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES.equals(name)) {
			if (Boolean.valueOf(value.toString())) {
				throw new IllegalArgumentException();
			}
		} else if (XMLInputFactory.IS_VALIDATING.equals(name)) {
			if (Boolean.valueOf(value.toString())) {
				throw new IllegalArgumentException();
			}
		} else { // proprietary properties
			if (PROP_MULTIPLE_PI.equals(name)) {
				multiplePI = Boolean.valueOf(value.toString());
			} else if (PROP_STREAM_FACTORY.equals(name)) {
				streamFactory = (JsonStreamFactory)value;
			}
		}
		throw new IllegalArgumentException("Unsupported property: " + name);
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		if (XMLInputFactory.IS_COALESCING.equals(name)) {
			return coalescing;
		} else if (XMLInputFactory.IS_NAMESPACE_AWARE.equals(name)) {
			return true;
		} else if (XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES.equals(name)) {
			return true;
		} else if (XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES.equals(name)) {
			return false;
		} else if (XMLInputFactory.IS_VALIDATING.equals(name)) {
			return false;
		} else { // proprietary properties
			 if (PROP_MULTIPLE_PI.equals(name)) {
				return multiplePI;
			} else if (PROP_STREAM_FACTORY.equals(name)) {
				return streamFactory;
			}
		}
		throw new IllegalArgumentException("Unsupported property: " + name);
	}

	@Override
	public boolean isPropertySupported(String name) {
		if (XMLInputFactory.IS_COALESCING.equals(name)) {
			return true;
		} else if (XMLInputFactory.IS_NAMESPACE_AWARE.equals(name)) {
			return true;
		} else if (XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES.equals(name)) {
			return true;
		} else if (XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES.equals(name)) {
			return true;
		} else if (XMLInputFactory.IS_VALIDATING.equals(name)) {
			return true;
		} else { // proprietary properties
			return Arrays.asList(PROP_MULTIPLE_PI, PROP_STREAM_FACTORY).contains(name);
		}
	}

	@Override
	public void setEventAllocator(XMLEventAllocator allocator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventAllocator getEventAllocator() {
		throw new UnsupportedOperationException();
	}
}
