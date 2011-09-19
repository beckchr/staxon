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
package de.odysseus.staxon;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import de.odysseus.staxon.event.SimpleXMLEventAllocator;
import de.odysseus.staxon.event.SimpleXMLEventReader;

/**
 * Abstract XML input factory.
 */
public abstract class AbstractXMLInputFactory extends XMLInputFactory {
	private XMLEventAllocator allocator = new SimpleXMLEventAllocator();
	
	@Override
	public XMLStreamReader createXMLStreamReader(InputStream stream, String encoding) throws XMLStreamException {
		try {
			return createXMLStreamReader(new InputStreamReader(stream, encoding));
		} catch (UnsupportedEncodingException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public XMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException {
		if (source instanceof StreamSource) {
			StreamSource streamSource = (StreamSource) source;
			InputStream input = streamSource.getInputStream();
			if (input != null) {
				if (streamSource.getSystemId() != null) {
					return createXMLStreamReader(streamSource.getSystemId(), input);
				} else {
					return createXMLStreamReader(input);
				}
			}
			Reader reader = streamSource.getReader();
			if (reader != null) {
				if (streamSource.getSystemId() != null) {
					return createXMLStreamReader(streamSource.getSystemId(), reader);
				} else {
					return createXMLStreamReader(reader);
				}
			}
			if (streamSource.getSystemId() != null) {
				// TODO this stream will never be closed!
				try {
					return createXMLStreamReader(streamSource.getSystemId(), new URI(source.getSystemId()).toURL().openStream());
				} catch (Exception e) {
					throw new XMLStreamException("Cannot open system id as URL for reading: " + source.getSystemId(), e);
				}
			} else {
				throw new XMLStreamException("Invalid stream source: none of input, reader, systemId set");
			}
		}
		throw new XMLStreamException("Unsupported source type: " + source.getClass());
	}

	@Override
	public XMLStreamReader createXMLStreamReader(String systemId, InputStream stream) throws XMLStreamException {
		return createXMLStreamReader(stream);
	}

	@Override
	public XMLStreamReader createXMLStreamReader(String systemId, Reader reader) throws XMLStreamException {
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
	public void setEventAllocator(XMLEventAllocator allocator) {
		this.allocator = allocator;
	}

	@Override
	public XMLEventAllocator getEventAllocator() {
		return allocator;
	}
}
