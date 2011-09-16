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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import de.odysseus.staxon.event.SimpleXMLEventWriter;


public class SimpleXMLOutputFactory extends XMLOutputFactory {
	@Override
	public XMLStreamWriter createXMLStreamWriter(Writer stream) throws XMLStreamException {
		return new SimpleXMLStreamWriter(stream);
	}

	@Override
	public XMLStreamWriter createXMLStreamWriter(OutputStream stream) throws XMLStreamException {
		return createXMLStreamWriter(stream, "UTF-8");
	}

	@Override
	public XMLStreamWriter createXMLStreamWriter(OutputStream stream, String encoding) throws XMLStreamException {
		try {
			return new SimpleXMLStreamWriter(new OutputStreamWriter(stream, encoding));
		} catch (UnsupportedEncodingException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public XMLStreamWriter createXMLStreamWriter(Result result) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventWriter createXMLEventWriter(Result result) throws XMLStreamException {
		return createXMLEventWriter(createXMLStreamWriter(result));
	}

	@Override
	public XMLEventWriter createXMLEventWriter(OutputStream stream) throws XMLStreamException {
		return createXMLEventWriter(createXMLStreamWriter(stream));
	}

	@Override
	public XMLEventWriter createXMLEventWriter(OutputStream stream, String encoding) throws XMLStreamException {
		return createXMLEventWriter(createXMLStreamWriter(stream, encoding));
	}

	@Override
	public XMLEventWriter createXMLEventWriter(Writer stream) throws XMLStreamException {
		return createXMLEventWriter(createXMLStreamWriter(stream));
	}

	public XMLEventWriter createXMLEventWriter(XMLStreamWriter writer) throws XMLStreamException {
		return new SimpleXMLEventWriter(writer);
	}

	@Override
	public void setProperty(String name, Object value) throws IllegalArgumentException {
		if (XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals(name)) {
			if (Boolean.valueOf(value.toString())) {
				throw new IllegalArgumentException();
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		if (XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals(name)) {
			return false;
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public boolean isPropertySupported(String name) {
		if (XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals(name)) {
			return true;
		} else {
			return false;
		}
	}
}
