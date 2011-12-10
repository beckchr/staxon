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
 * Simple JSON XML configuration.
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

	@Override
	public boolean isMultiplePI() {
		return multiplePI;
	}
	
	public void setMultiplePI(boolean multiplePI) {
		this.multiplePI = multiplePI;
	}
	
	@Override
	public boolean isNamespaceDeclarations() {
		return namespaceDeclarations;
	}
	
	public void setNamespaceDeclarations(boolean namespaceDeclarations) {
		this.namespaceDeclarations = namespaceDeclarations;
	}
	
	@Override
	public char getNamespaceSeparator() {
		return namespaceSeparator;
	}
	
	public void setNamespaceSeparator(char namespaceSeparator) {
		this.namespaceSeparator = namespaceSeparator;
	}
	
	@Override
	public boolean isPrettyPrint() {
		return prettyPrint;
	}
	
	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}
	
	@Override
	public QName getVirtualRoot() {
		return virtualRoot;
	}
	
	public void setVirtualRoot(QName virtualRoot) {
		this.virtualRoot = virtualRoot;
	}
	
	public void setVirtualRoot(String virtualRoot) {
		this.virtualRoot = QName.valueOf(virtualRoot);
	}
}
