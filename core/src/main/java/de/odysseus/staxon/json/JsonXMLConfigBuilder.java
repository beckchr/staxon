/*
 * Copyright 2011, 2012 Odysseus Software GmbH
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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * <p>Configuration builder with "fluid" interface.</p>
 * <pre>
 * JsonXMLConfig config = new JsonXMLConfigBuilder().virtualRoot("foo").prettyPrint(true).build();
 * </pre>
 * <p>Initially, values are set according to {@link JsonXMLConfig#DEFAULT}.</p>
 * @see JsonXMLConfig
 */
public class JsonXMLConfigBuilder {
	protected final JsonXMLConfigImpl config;
	
	/**
	 * Create a new builder.
	 */
	public JsonXMLConfigBuilder() {
		this(new JsonXMLConfigImpl());
	}
	
	protected JsonXMLConfigBuilder(JsonXMLConfigImpl config) {
		this.config = config;
	}
	
	/**
	 * Build a new configuration.
	 * @return configuration instance
	 */
	public JsonXMLConfig build() {
		return config.clone();
	}
	
	/**
	 * Set autoArray property and return receiver.
	 * @param autoArray
	 * @return this
	 */
	public JsonXMLConfigBuilder autoArray(boolean autoArray) {
		config.setAutoArray(autoArray);
		return this;
	}

	/**
	 * Set autoPrimitive property and return receiver.
	 * @param autoPrimitive
	 * @return this
	 */
	public JsonXMLConfigBuilder autoPrimitive(boolean autoPrimitive) {
		config.setAutoPrimitive(autoPrimitive);
		return this;
	}

	/**
	 * Set multiplePI property and return receiver.
	 * @param multiplePI
	 * @return this
	 */
	public JsonXMLConfigBuilder multiplePI(boolean multiplePI) {
		config.setMultiplePI(multiplePI);
		return this;
	}

	/**
	 * Set namespaceDeclarations property and return receiver.
	 * @param namespaceDeclarations
	 * @return this
	 */
	public JsonXMLConfigBuilder namespaceDeclarations(boolean namespaceDeclarations) {
		config.setNamespaceDeclarations(namespaceDeclarations);
		return this;
	}
	
	/**
	 * Set namespaceSeparator property and return receiver.
	 * @param namespaceSeparator
	 * @return this
	 */
	public JsonXMLConfigBuilder namespaceSeparator(char namespaceSeparator) {
		config.setNamespaceSeparator(namespaceSeparator);
		return this;
	}
	
	/**
	 * Set prettyPrint property and return receiver.
	 * @param prettyPrint
	 * @return this
	 */
	public JsonXMLConfigBuilder prettyPrint(boolean prettyPrint) {
		config.setPrettyPrint(prettyPrint);
		return this;
	}

	/**
	 * Set virtualRoot property and return receiver.
	 * @param virtualRoot
	 * @return this
	 */
	public JsonXMLConfigBuilder virtualRoot(QName virtualRoot) {
		config.setVirtualRoot(virtualRoot);
		return this;
	}
	
	/**
	 * Set virtualRoot property and return receiver.
	 * @param virtualRoot (parsed with {@link QName#valueOf(String)})
	 * @return this
	 */
	public JsonXMLConfigBuilder virtualRoot(String virtualRoot) {
		config.setVirtualRoot(QName.valueOf(virtualRoot));
		return this;
	}

	/**
	 * Set repairingNamespaces property and return receiver.
	 * @param repairingNamespaces
	 * @return this
	 */
	public JsonXMLConfigBuilder repairingNamespaces(boolean repairingNamespaces) {
		config.setRepairingNamespaces(repairingNamespaces);
		return this;
	}

	/**
	 * Set namespace mappings property and return receiver.
	 * @param repairingNamespaces
	 * @return this
	 */
	public JsonXMLConfigBuilder namespaceMappings(Map<String, String> namespaceMappings) {
		config.setNamespaceMappings(namespaceMappings);
		return this;
	}

	/**
	 * Add a namespace mapping and return receiver.
	 * @param repairingNamespaces
	 * @return this
	 */
	public JsonXMLConfigBuilder namespaceMapping(String prefix, String namespaceURI) {
		Map<String, String> namespaceMappings = new HashMap<String, String>();
		if (config.getNamespaceMappings() != null) {
			namespaceMappings.putAll(config.getNamespaceMappings());
		}
		namespaceMappings.put(prefix, namespaceURI);
		config.setNamespaceMappings(namespaceMappings);
		return this;
	}

	/**
	 * Formats the JSON Output during XML->JSON transformation.<br/>
	 * Supports reading of XML Nil values as defined by http://www.w3.org/TR/xmlschema-1/#xsi_nil.
	 * Enabling this configuration will produce corresponding JSON fields with null values for XML elements that define nil.
	 * <br/><br/> Example:
	 * <br/> If set to <tt>true</tt><br/>
	 * <br/>&lt;test xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" >&lt;/test> <br/>will produce
	 * <br/>{"test":null}
	 * <br/>AND
	 * <br/>&lt;test>&lt;/test> <br/>will produce
	 * <br/>{"test":""}
	 * @param read
	 * @return
	 */
	public JsonXMLConfigBuilder readXmlNil(boolean read) {
		config.setReadXmlNil(read);
		return this;
	}

	/**
	 * Formats the XML Output during JSON->XML transformation.<br/>
	 * Supports writing of XML Nil elements as defined by http://www.w3.org/TR/xmlschema-1/#xsi_nil when the
	 * JSON input contains null values.
	 * <br/><br/> Example: <br/>{"test":null} <br/>will produce
	 * <br/>&lt;test xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" >&lt;/test>
	 * <br/>AND
	 * <br/>{"test":""}<br/>will produce
	 * <br/>&lt;test>&lt;/test>
	 * @param write
	 * @return
	 */
	public JsonXMLConfigBuilder writeXmlNil(boolean write) {
		config.setWriteXmlNil(write);
		return this;
	}
}
