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
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;

import de.odysseus.staxon.event.SimpleXMLEventAllocator;
import de.odysseus.staxon.event.SimpleXMLEventReader;
import de.odysseus.staxon.json.stream.JsonStreamFactory;
import de.odysseus.staxon.json.stream.JsonStreamSource;
import de.odysseus.staxon.json.stream.util.AddRootSource;

public class JsonXMLInputFactory extends XMLInputFactory {
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

	/**
	 * <p>JSON documents may have have multiple root properties. However,
	 * XML requires a single root element. This property takes the name
	 * of a "virtual" root element, which will be added to the stream
	 * when reading.</p>
	 */
	public static final String PROP_VIRTUAL_ROOT = "JsonXMLInputFactory.virtualRoot";

	private final JsonStreamFactory streamFactory;

	private boolean multiplePI = true;
	private String virtualRoot = null;
	private boolean coalescing;
	private XMLEventAllocator allocator = new SimpleXMLEventAllocator();
	
	public JsonXMLInputFactory() throws FactoryConfigurationError {
		this(JsonStreamFactory.newFactory());
	}

	public JsonXMLInputFactory(JsonStreamFactory streamFactory) {
		this.streamFactory = streamFactory;
	}
	
	private JsonStreamSource decorate(JsonStreamSource source) {
		if (virtualRoot != null) {
			source = new AddRootSource(source, virtualRoot);
		}
		return source;
	}
	
	@Override
	public JsonXMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
		try {
			return new JsonXMLStreamReader(decorate(streamFactory.createJsonStreamSource(reader)), multiplePI);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public JsonXMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
		try {
			return new JsonXMLStreamReader(decorate(streamFactory.createJsonStreamSource(stream)), multiplePI);
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
		return createXMLEventReader(createXMLStreamReader(reader));
	}

	@Override
	public XMLEventReader createXMLEventReader(String systemId, Reader reader) throws XMLStreamException {
		return createXMLEventReader(createXMLStreamReader(systemId, reader));
	}

	@Override
	public XMLEventReader createXMLEventReader(XMLStreamReader reader) throws XMLStreamException {
		return new SimpleXMLEventReader(getEventAllocator().newInstance(), reader);
	}

	@Override
	public XMLEventReader createXMLEventReader(Source source) throws XMLStreamException {
		return createXMLEventReader(createXMLStreamReader(source));
	}

	@Override
	public XMLEventReader createXMLEventReader(InputStream stream) throws XMLStreamException {
		return createXMLEventReader(createXMLStreamReader(stream));
	}

	@Override
	public XMLEventReader createXMLEventReader(InputStream stream, String encoding) throws XMLStreamException {
		return createXMLEventReader(createXMLStreamReader(stream, encoding));
	}

	@Override
	public XMLEventReader createXMLEventReader(String systemId, InputStream stream) throws XMLStreamException {
		return createXMLEventReader(createXMLStreamReader(systemId, stream));
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
			} else if (PROP_VIRTUAL_ROOT.equals(name)) {
				virtualRoot = (String)value;
			} else {
				throw new IllegalArgumentException("Unsupported input property: " + name);
			}
		}
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
			} else if (PROP_VIRTUAL_ROOT.equals(name)) {
				return virtualRoot;
			} else {
				throw new IllegalArgumentException("Unsupported input property: " + name);
			}
		}
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
			return Arrays.asList(PROP_MULTIPLE_PI, PROP_VIRTUAL_ROOT).contains(name);
		}
	}

	@Override
	public void setEventAllocator(XMLEventAllocator allocator) {
		this.allocator = allocator;
	}

	@Override
	public XMLEventAllocator getEventAllocator() {
		return allocator;
	}
}
