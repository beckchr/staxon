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
package de.odysseus.staxon.json.jaxb;

import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * Read/write instances of JAXB-annotated classes from/to JSON.
 */
public class JsonXMLMapper<T> {
	private final Class<T> type;
	private final JsonXMLConfig config;
	private final JsonXMLBinder binder;
	private final JAXBContext context;
	
	public JsonXMLMapper(Class<T> type) throws JAXBException {
		this(type, type.getAnnotation(JsonXML.class));
	}

	public JsonXMLMapper(Class<T> type, JsonXML annotation) throws JAXBException {
		this(type, new DefaultJsonXMLConfig(annotation));
	}

	public JsonXMLMapper(Class<T> type, JsonXMLConfig config) throws JAXBException {
		this.type = type;
		this.config = config;
		this.binder = createBinder(config);
		this.context = createContext(config);
	}
	
	protected JsonXMLBinder createBinder(JsonXMLConfig config) {
		return new JsonXMLBinder();
	}
	
	protected JAXBContext createContext(JsonXMLConfig config) throws JAXBException {
		return JAXBContext.newInstance(type);
	}
	
	public T readObject(Reader reader) throws JAXBException, XMLStreamException {
		return binder.readObject(type, config, context, reader);
	}

	public void writeObject(Writer writer, T value) throws JAXBException, XMLStreamException {
		binder.writeObject(type, config, context, writer, value);
	}
	
	public List<T> readArray(Reader reader) throws JAXBException, XMLStreamException {
		return binder.readArray(type, config, context, reader);
	}

	public void writeArray(Writer writer, Collection<T> collection) throws JAXBException, XMLStreamException {
		binder.writeArray(type, config, context, writer, collection);
	}
}
