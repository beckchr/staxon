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

import javax.xml.namespace.QName;

/**
 * <p>Simple JSON XML configuration.</p>
 * 
 * <p>This class provides a simple "fluid" interface, e.g.</p>
 * <pre>
 * JsonXMLConfig config = new JsonXMLConfig().withVirtualRoot("foo").withPrettyPrint(true);
 * </pre>
 * <p>Initially, values are set according to {@link JsonXMLConfig#DEFAULT}.</p>
 * @see JsonXMLConfig
 */
public class JsonXMLConfigImpl implements JsonXMLConfig {
	private QName virtualRoot = JsonXMLConfig.DEFAULT.getVirtualRoot();
	private boolean multiplePI = JsonXMLConfig.DEFAULT.isMultiplePI();
	private boolean prettyPrint = JsonXMLConfig.DEFAULT.isPrettyPrint();
	private boolean autoArray = JsonXMLConfig.DEFAULT.isAutoArray();
	private boolean namespaceDeclarations = JsonXMLConfig.DEFAULT.isNamespaceDeclarations();
	private char namespaceSeparator = JsonXMLConfig.DEFAULT.getNamespaceSeparator();
	
	@Override
	public boolean isAutoArray() {
		return autoArray;
	}
	
	public void setAutoArray(boolean autoArray) {
		this.autoArray = autoArray;
	}
	
	/**
	 * Set autoArray property and return receiver.
	 * @param autoArray
	 * @return this
	 */
	public JsonXMLConfigImpl withAutoArray(boolean autoArray) {
		setAutoArray(autoArray);
		return this;
	}

	@Override
	public boolean isMultiplePI() {
		return multiplePI;
	}
	
	public void setMultiplePI(boolean multiplePI) {
		this.multiplePI = multiplePI;
	}
	
	/**
	 * Set multiplePI property and return receiver.
	 * @param multiplePI
	 * @return this
	 */
	public JsonXMLConfigImpl withMultiplePI(boolean multiplePI) {
		setMultiplePI(multiplePI);
		return this;
	}

	@Override
	public boolean isNamespaceDeclarations() {
		return namespaceDeclarations;
	}
	
	public void setNamespaceDeclarations(boolean namespaceDeclarations) {
		this.namespaceDeclarations = namespaceDeclarations;
	}
	
	/**
	 * Set namespaceDeclarations property and return receiver.
	 * @param namespaceDeclarations
	 * @return this
	 */
	public JsonXMLConfigImpl withNamespaceDeclarations(boolean namespaceDeclarations) {
		setNamespaceDeclarations(namespaceDeclarations);
		return this;
	}

	@Override
	public char getNamespaceSeparator() {
		return namespaceSeparator;
	}
	
	public void setNamespaceSeparator(char namespaceSeparator) {
		this.namespaceSeparator = namespaceSeparator;
	}
	
	/**
	 * Set namespaceSeparator property and return receiver.
	 * @param namespaceSeparator
	 * @return this
	 */
	public JsonXMLConfigImpl withNamespaceSeparator(char namespaceSeparator) {
		setNamespaceSeparator(namespaceSeparator);
		return this;
	}

	@Override
	public boolean isPrettyPrint() {
		return prettyPrint;
	}
	
	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}
	
	/**
	 * Set prettyPrint property and return receiver.
	 * @param prettyPrint
	 * @return this
	 */
	public JsonXMLConfigImpl withPrettyPrint(boolean prettyPrint) {
		setPrettyPrint(prettyPrint);
		return this;
	}

	@Override
	public QName getVirtualRoot() {
		return virtualRoot;
	}
	
	public void setVirtualRoot(QName virtualRoot) {
		this.virtualRoot = virtualRoot;
	}
	
	/**
	 * Set virtualRoot property and return receiver.
	 * @param virtualRoot
	 * @return this
	 */
	public JsonXMLConfigImpl withVirtualRoot(QName virtualRoot) {
		setVirtualRoot(virtualRoot);
		return this;
	}
	
	/**
	 * Set virtualRoot property and return receiver.
	 * @param virtualRoot (parsed with {@link QName#valueOf(String)})
	 * @return this
	 */
	public JsonXMLConfigImpl withVirtualRoot(String virtualRoot) {
		setVirtualRoot(QName.valueOf(virtualRoot));
		return this;
	}
}
