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

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import de.odysseus.staxon.AbstractXMLStreamReader;
import de.odysseus.staxon.XMLStreamReaderScope;
import de.odysseus.staxon.json.stream.JsonStreamSource;
import de.odysseus.staxon.json.stream.JsonStreamToken;

/**
 * JSON XML stream reader.
 * 
 * <h4>Limitations</h4>
 * <ul>
 *   <li>Mixed content (e.g. <code>&lt;alice&gt;bob&lt;edgar/&gt;&lt;/alice&gt;</code>) is not supported.</li>
 * </ul>
 * 
 * <p>The reader may produce processing instructions
 * <code>&lt;?xml-multiple element-name?&gt;</code>
 * to indicate array starts (<code>'['</code>).</p>
 */
public class JsonXMLStreamReader extends AbstractXMLStreamReader<JsonXMLStreamReader.ScopeInfo> {
	static class ScopeInfo extends JsonXMLStreamScopeInfo {
		private String currentTagName;
	}
	
	private final JsonStreamSource source;
	private final boolean multiplePI;
	private final char prefixSeparator;

	/**
	 * Create reader instance.
	 * @param source stream source
	 * @param multiplePI whether to use processing instruction to trigger array start
	 * @throws XMLStreamException
	 */
	public JsonXMLStreamReader(JsonStreamSource source, boolean multiplePI, char prefixSeparator) throws XMLStreamException {
		super(new ScopeInfo());
		this.source = source;
		this.multiplePI = multiplePI;
		this.prefixSeparator = prefixSeparator;
		initialize();
	}

	private void readStartElementTag(String name) throws XMLStreamException {
		int separator = name.indexOf(prefixSeparator);
		if (separator < 0) {
			readStartElementTag(XMLConstants.DEFAULT_NS_PREFIX, name, new ScopeInfo());
		} else {
			readStartElementTag(name.substring(0, separator), name.substring(separator+1), new ScopeInfo());
		}
	}
	
	private void readAttr(String name, String value) throws XMLStreamException {
		int separator = name.indexOf(prefixSeparator);
		if (separator < 0) {
			readAttr(XMLConstants.DEFAULT_NS_PREFIX, name, value);
		} else {
			readAttr(name.substring(0, separator), name.substring(separator+1), value);
		}
	}

	private void consumeName(ScopeInfo info) throws XMLStreamException, IOException {
		String fieldName = source.name();
		if (fieldName.startsWith("@")) {
			fieldName = fieldName.substring(1);
			if (source.peek() == JsonStreamToken.VALUE) {
				readAttr(fieldName, source.value());
			} else if (XMLConstants.XMLNS_ATTRIBUTE.equals(fieldName)) { // badgerfish
				source.startObject();
				while (source.peek() == JsonStreamToken.NAME) {
					String prefix = source.name();
					if ("$".equals(prefix)) {
						readAttr(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.XMLNS_ATTRIBUTE, source.value());
					} else {
						readAttr(XMLConstants.XMLNS_ATTRIBUTE, prefix, source.value());
					}
				}
				source.endObject();
			} else {
				throw new IllegalStateException("Expected attribute value");
			}
		} else if ("$".equals(fieldName)) {
			readData(source.value(), XMLStreamConstants.CHARACTERS);
		} else {
			info.currentTagName = fieldName;
		}
	}
	
	@Override
	protected boolean consume() throws XMLStreamException, IOException {
		XMLStreamReaderScope<ScopeInfo> scope = getScope();
		switch (source.peek()) {
		case NAME:
			consumeName(scope.getInfo());
			return consume();
		case START_ARRAY:
			source.startArray();
			if (scope.getInfo().getArrayName() != null) {
				throw new IOException("Array start inside array");
			}
			if (scope.getInfo().currentTagName == null) {
				throw new IOException("Array name missing");
			}
			scope.getInfo().startArray(scope.getInfo().currentTagName);
			if (multiplePI) {
				readPI(JsonXMLStreamConstants.MULTIPLE_PI_TARGET, scope.getInfo().currentTagName);
			}
			return consume();
		case START_OBJECT:
			source.startObject();
			if (scope.isRoot() && scope.getInfo().currentTagName == null) {
				readStartDocument(null, null, null);
			} else {
				if (scope.getInfo().getArrayName() != null) {
					scope.getInfo().incArraySize();
				}
				readStartElementTag(scope.getInfo().currentTagName);
			}
			return consume();
		case END_OBJECT:
			source.endObject();
			if (scope.isRoot()) {
				readEndDocument();
				return false;
			} else {
				readEndElementTag();
				return true;
			}
		case VALUE:
			String text = source.value();
			String name = scope.getInfo().currentTagName;
			if (scope.getInfo().getArrayName() != null) {
				scope.getInfo().incArraySize();
				name = scope.getInfo().getArrayName();
			}
			readStartElementTag(name);
			if (text != null) {
				readData(text, XMLStreamConstants.CHARACTERS);
			}
			readEndElementTag();
			return true;
		case END_ARRAY:
			source.endArray();
			if (scope.getInfo().getArrayName() == null) {
				throw new IllegalStateException("Array end without matching start");
			}
			scope.getInfo().endArray();
			return true;
		case NONE:
			return false;
		default:
			throw new IOException("Unexpected token: " + source.peek());
		}
	}

	@Override
	public void close() throws XMLStreamException {
		super.close();
		try {
			source.close();
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}
}
