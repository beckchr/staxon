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
import java.io.Writer;
import java.util.Arrays;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.odysseus.staxon.AbstractXMLOutputFactory;
import de.odysseus.staxon.event.SimpleXMLEventWriter;
import de.odysseus.staxon.json.stream.JsonStreamFactory;
import de.odysseus.staxon.json.stream.JsonStreamTarget;
import de.odysseus.staxon.json.stream.util.AutoArrayTarget;
import de.odysseus.staxon.json.stream.util.RemoveRootTarget;

/**
 * XML output factory for streaming to JSON.
 */
public class JsonXMLOutputFactory extends AbstractXMLOutputFactory {
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
	 * <p>JSON documents may have have multiple root properties. However,
	 * XML requires a single root element. This property takes the name
	 * of a "virtual" root element, which will be removed from the stream
	 * when writing.</p>
	 * 
	 * <p>The default value is <code>null</code>.</p>
	 */
	public static final String PROP_VIRTUAL_ROOT = "JsonXMLOutputFactory.virtualRoot";

	/**
	 * <p>Namespace prefix separator.</p>
	 * 
	 * <p>The default value is <code>':'</code>.</p>
	 */
	public static final String PROP_NAMESPACE_SEPARATOR = "JsonXMLOutputFactory.namespaceSeparator";

	/**
	 * <p>Whether to write namespace declarations.</p>
	 * 
	 * <p>The default value is <code>true</code>.</p>
	 */
	public static final String PROP_NAMESPACE_DECLARATIONS = "JsonXMLOutputFactory.namespaceDeclarations";

	/**
	 * <p>Format output for better readability?</p>
	 * 
	 * <p>The default value is <code>false</code>.</p>
	 */
	public static final String PROP_PRETTY_PRINT = "JsonXMLOutputFactory.prettyPrint";

	private JsonStreamFactory streamFactory;
	private boolean multiplePI;
	private QName virtualRoot;
	private boolean autoArray;
	private boolean prettyPrint;
	private char namespaceSeparator;
	private boolean namespaceDeclarations;

	public JsonXMLOutputFactory() throws FactoryConfigurationError {
		this(JsonXMLConfig.DEFAULT);
	}

	public JsonXMLOutputFactory(JsonStreamFactory streamFactory) {
		this(JsonXMLConfig.DEFAULT, streamFactory);
	}

	public JsonXMLOutputFactory(JsonXMLConfig config) throws FactoryConfigurationError {
		this(config, JsonStreamFactory.newFactory());
	}
	
	public JsonXMLOutputFactory(JsonXMLConfig config, JsonStreamFactory streamFactory) {
		this.multiplePI = config.isMultiplePI();
		this.virtualRoot = config.getVirtualRoot();
		this.autoArray = config.isAutoArray();
		this.prettyPrint = config.isPrettyPrint();
		this.namespaceSeparator = config.getNamespaceSeparator();
		this.namespaceDeclarations = config.isNamespaceDeclarations();
		this.streamFactory = streamFactory;

		/*
		 * initialize standard properties
		 */
		super.setProperty(IS_REPAIRING_NAMESPACES, Boolean.FALSE);
	}
		
	private JsonStreamTarget decorate(JsonStreamTarget target) {
		if (virtualRoot != null) {
			target = new RemoveRootTarget(target, virtualRoot, namespaceSeparator);
		}
		if (autoArray) {
			target = new AutoArrayTarget(target);
		}
		return target;
	}

	@Override
	public XMLStreamWriter createXMLStreamWriter(Writer stream) throws XMLStreamException {
		try {
			return new JsonXMLStreamWriter(decorate(streamFactory.createJsonStreamTarget(stream, prettyPrint)), multiplePI, namespaceSeparator, namespaceDeclarations);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public XMLStreamWriter createXMLStreamWriter(OutputStream stream) throws XMLStreamException {
		try {
			return new JsonXMLStreamWriter(decorate(streamFactory.createJsonStreamTarget(stream, prettyPrint)), multiplePI, namespaceSeparator, namespaceDeclarations);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public XMLEventWriter createXMLEventWriter(XMLStreamWriter writer) throws XMLStreamException {
		return new SimpleXMLEventWriter(writer);
	}

	@Override
	public boolean isPropertySupported(String name) {
		return super.isPropertySupported(name)
			|| Arrays.asList(PROP_AUTO_ARRAY, PROP_MULTIPLE_PI, PROP_VIRTUAL_ROOT, PROP_NAMESPACE_SEPARATOR, PROP_NAMESPACE_DECLARATIONS, PROP_PRETTY_PRINT).contains(name);
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		if (super.isPropertySupported(name)) {
			return super.getProperty(name);
		} else { // proprietary properties
			if (PROP_AUTO_ARRAY.equals(name)) {
				return Boolean.valueOf(autoArray);
			} else if (PROP_MULTIPLE_PI.equals(name)) {
				return Boolean.valueOf(multiplePI);
			} else if (PROP_VIRTUAL_ROOT.equals(name)) {
				return virtualRoot;
			} else if (PROP_PRETTY_PRINT.equals(name)) {
				return Boolean.valueOf(prettyPrint);
			} else if (PROP_NAMESPACE_SEPARATOR.equals(name)) {
				return namespaceSeparator;
			} else if (PROP_NAMESPACE_DECLARATIONS.equals(name)) {
				return Boolean.valueOf(namespaceDeclarations);
			} else {
				throw new IllegalArgumentException("Unsupported property: " + name);
			}
		}
	}

	@Override
	public void setProperty(String name, Object value) throws IllegalArgumentException {
		if (XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals(name)) {
			if (!getProperty(name).equals(value)) {
				throw new IllegalArgumentException("Cannot change property: " + name);
			}
		} else if (super.isPropertySupported(name)) {
			super.setProperty(name, value);
		} else { // proprietary properties
			if (PROP_AUTO_ARRAY.equals(name)) {
				autoArray = ((Boolean)value).booleanValue();
			} else if (PROP_MULTIPLE_PI.equals(name)) {
				multiplePI = ((Boolean)value).booleanValue();
			} else if (PROP_VIRTUAL_ROOT.equals(name)) {
				virtualRoot = value instanceof String ? QName.valueOf((String)value) : (QName)value;
			} else if (PROP_PRETTY_PRINT.equals(name)) {
				prettyPrint = ((Boolean)value).booleanValue();
			} else if (PROP_NAMESPACE_SEPARATOR.equals(name)) {
				namespaceSeparator = (Character)value;
			} else if (PROP_NAMESPACE_DECLARATIONS.equals(name)) {
				namespaceDeclarations = ((Boolean)value).booleanValue();
			} else {
				throw new IllegalArgumentException("Unsupported property: " + name);
			}
		}
	}
}
