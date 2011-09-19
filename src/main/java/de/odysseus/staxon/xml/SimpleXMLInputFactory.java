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
package de.odysseus.staxon.xml;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import de.odysseus.staxon.AbstractXMLInputFactory;

public class SimpleXMLInputFactory extends AbstractXMLInputFactory {
	private boolean coalescing = false;
	
	@Override
	public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
		return new SimpleXMLStreamReader(reader);
	}

	@Override
	public XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
		return createXMLStreamReader(stream, "UTF-8");
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
			coalescing = ((Boolean)value).booleanValue();
		} else if (XMLInputFactory.IS_NAMESPACE_AWARE.equals(name)) {
			if (!getProperty(name).equals(value)) {
				throw new IllegalArgumentException("Cannot change property: " + name);
			}
		} else if (XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES.equals(name)) {
			if (!getProperty(name).equals(value)) {
				throw new IllegalArgumentException("Cannot change property: " + name);
			}
		} else if (XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES.equals(name)) {
			if (!getProperty(name).equals(value)) {
				throw new IllegalArgumentException("Cannot change property: " + name);
			}
		} else if (XMLInputFactory.IS_VALIDATING.equals(name)) {
			if (!getProperty(name).equals(value)) {
				throw new IllegalArgumentException("Cannot change property: " + name);
			}
		} else {
			throw new IllegalArgumentException("Unsupported property: " + name);
		}
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		if (XMLInputFactory.IS_COALESCING.equals(name)) {
			return Boolean.valueOf(coalescing);
		} else if (XMLInputFactory.IS_NAMESPACE_AWARE.equals(name)) {
			return Boolean.TRUE;
		} else if (XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES.equals(name)) {
			return Boolean.TRUE;
		} else if (XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES.equals(name)) {
			return Boolean.FALSE;
		} else if (XMLInputFactory.IS_VALIDATING.equals(name)) {
			return Boolean.FALSE;
		} else {
			throw new IllegalArgumentException("Unsupported property: " + name);
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
		} else {
			return false;
		}
	}
}
