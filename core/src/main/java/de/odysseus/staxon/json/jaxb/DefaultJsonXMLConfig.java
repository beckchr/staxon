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

/**
 * Default JSON XML (JAXB) configuration.
 */
public class DefaultJsonXMLConfig implements JsonXMLConfig {
	private boolean virtualRoot;
	private String[] multiplePaths;
	private boolean prettyPrint;
	private boolean autoArray;
	private boolean namespaceDeclarations;
	private char namespaceSeparator;
	
	/**
	 * Create new configuration instance.
	 * Properties are initialized as specified by the {@link JsonXML} default values.
	 */
	public DefaultJsonXMLConfig() {
		this(null);
	}
	
	/**
	 * Create new configuration instance.
	 * If the annotation is <code>null</code>, properties are initialized
	 * as specified by the {@link JsonXML} default values.
	 * @param annotation used to initialize properties
	 */
	public DefaultJsonXMLConfig(JsonXML annotation) {
		if (annotation == null) {
			@JsonXML
			class Default {};
			annotation = Default.class.getAnnotation(JsonXML.class);
		}
		autoArray = annotation.autoArray();
		multiplePaths = annotation.multiplePaths();
		namespaceDeclarations = annotation.namespaceDeclarations();
		namespaceSeparator = annotation.namespaceSeparator();
		prettyPrint = annotation.prettyPrint();
		virtualRoot = annotation.virtualRoot();		
	}
	
	@Override
	public boolean isAutoArray() {
		return autoArray;
	}
	
	public void setAutoArray(boolean autoArray) {
		this.autoArray = autoArray;
	}

	@Override
	public String[] getMultiplePaths() {
		return multiplePaths;
	}
	
	public void setMultiplePaths(String[] multiplePaths) {
		this.multiplePaths = multiplePaths;
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
	public boolean isVirtualRoot() {
		return virtualRoot;
	}
	
	public void setVirtualRoot(boolean virtualRoot) {
		this.virtualRoot = virtualRoot;
	}
}
