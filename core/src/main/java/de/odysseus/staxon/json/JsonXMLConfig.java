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
 * Json XML factory configuration interface.
 */
public interface JsonXMLConfig {
	/**
	 * Default configuration
	 */
	public static final JsonXMLConfig DEFAULT = new JsonXMLConfig() {
		@Override
		public boolean isAutoArray() {
			return false;
		}
		@Override
		public boolean isMultiplePI() {
			return true;
		}
		@Override
		public boolean isNamespaceDeclarations() {
			return true;
		}
		@Override
		public char getNamespaceSeparator() {
			return ':';
		}
		@Override
		public boolean isPrettyPrint() {
			return false;
		}
		@Override
		public QName getVirtualRoot() {
			return null;
		}
	};

	/**
	 * <p>Trigger arrays automatically?</p>
	 * 
	 * <p>The default value is <code>false</code>.</p>
	 */
	public boolean isAutoArray();

	/**
	 * <p>Whether to use the {@link JsonXMLStreamConstants#MULTIPLE_PI_TARGET}
	 * processing instruction to indicate an array start.
	 * If <code>true</code>, a PI is used to inform the writer to begin an array,
	 * passing the name of following multiple elements as data.
	 * The writer will close arrays automatically.</p>
	 * If <code>true</code>, this reader will insert a PI with the field
	 * name as PI data. 
	 *  
	 * <p>Note that the element given in the PI may occur zero times,
	 * indicating an "empty array".</p>
	 * 
	 * <p>The default value is <code>true</code>.</p>
	 * @return multiple PI flag
	 */
	public boolean isMultiplePI();

	/**
	 * <p>Whether to write namespace declarations.</p>
	 * 
	 * <p>The default value is <code>true</code>.</p>
	 * @return namespace declarations flag
	 */
	public boolean isNamespaceDeclarations();

	/**
	 * <p>Namespace prefix separator.</p>
	 * 
	 * <p>The default value is <code>':'</code>.</p>
	 * @return namespace separator
	 */
	public char getNamespaceSeparator();

	/**
	 * <p>Format output for better readability?</p>
	 * 
	 * <p>The default value is <code>false</code>.</p>
	 * @return pretty print flag
	 */
	public boolean isPrettyPrint();

	/**
	 * <p>JSON documents may have have multiple root properties. However,
	 * XML requires a single root element. This property specifies
	 * the root as a "virtual" element, which will be removed from the stream
	 * when writing and added to the stream when reading.
	 * 
	 * <p>The default value is <code>null</code> (i.e. no virtual root).</p>
	 * @return virtual root
	 */
	public QName getVirtualRoot();
}