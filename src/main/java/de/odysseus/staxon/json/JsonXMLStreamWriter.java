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

import de.odysseus.staxon.AbstractXMLStreamWriter;
import de.odysseus.staxon.XMLStreamWriterScope;
import de.odysseus.staxon.json.stream.JsonStreamTarget;

/**
 * JSON XML stream writer.
 * 
 * <h4>Limitations</h4>
 * <ul>
 *   <li>Mixed content (e.g. <code>&lt;alice&gt;bob&lt;edgar/&gt;&lt;/alice&gt;</code>) is not supported.</li>
 *   <li><code>writeDTD(...)</code> and <code>writeEntityRef(...)</code> are not supported.</li>
 *   <li><code>writeCData(...)</code> delegates to writeCharacters(...).</li>
 *   <li><code>writeComment(...)</code> does nothing.</li>
 *   <li><code>writeProcessingInstruction(...)</code> does nothing (except for target <code>xml-multiple</code>, see below).</li>
 * </ul>
 * 
 * <p>The writer may consume processing instructions
 * (e.g. <code>&lt;?xml-multiple element-name?&gt;</code>) to properly insert JSON array tokens (<code>'['</code>
 * and <code>']'</code>). The client provides this instruction through the
 * {@link #writeProcessingInstruction(String, String)} method,
 * passing the (possibly prefixed) field name as data e.g.</p>
 * <pre>
 *   ...
 *   writer.writeProcessingInstruction("xml-multiple", "item");
 *   for (Item item : items) {
 *     writer.writeStartElement("item");
 *     ...
 *     writer.writeEndElement();
 *   }
 *   ...
 * </pre>
 * <p>The element name passed as processing instruction data is optional.
 * If omitted, the next element within the current scope will start an array. Note, that this method
 * does not allow to create empty arrays (in fact, the above code sample could create unexpected results,
 * if the name would have been omitted and collection were empty).</p>
 */
public class JsonXMLStreamWriter extends AbstractXMLStreamWriter<JsonXMLStreamWriter.ScopeInfo> {
	static class ScopeInfo extends JsonXMLStreamScopeInfo {
		private String leadText = null;
		private StringBuilder builder = null;
		boolean startObjectWritten = false;
		boolean pendingStartArray = false;

		void addText(String data) {
			if (leadText == null) { // first event?
				leadText = data;
			} else {
				if (builder == null) { // second event?
					builder = new StringBuilder(leadText);
				}
				builder.append(data);
			}
		}
		boolean hasText() {
			return leadText != null;
		}
		String getText() {
			return builder == null ? leadText : builder.toString();
		}
		void setText(String data) {
			leadText = data;
			builder = null;
		}
	}

	static boolean isWhitespace(String text) {
		for (int i = 0; i < text.length(); i++) {
			if (!Character.isWhitespace(text.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private final JsonStreamTarget target;
	private final boolean multiplePI;
	private final boolean autoEndArray;
	private final boolean skipSpace;
	private final char prefixSeparator;

	/**
	 * Create writer instance.
	 * @param target stream target
	 * @param multiplePI whether to use processing instruction to trigger array start
	 */
	public JsonXMLStreamWriter(JsonStreamTarget target, boolean multiplePI, char prefixSeparator) {
		super(new ScopeInfo());
		this.target = target;
		this.multiplePI = multiplePI;
		this.prefixSeparator = prefixSeparator;
		this.autoEndArray = true;
		this.skipSpace = true;
	}

	private String getFieldName(String prefix, String localName) {
		return XMLConstants.DEFAULT_NS_PREFIX.equals(prefix) ? localName : prefix + prefixSeparator + localName;
	}
	
	@Override
	protected void writeStartElementTag(XMLStreamWriterScope<ScopeInfo> newScope) throws XMLStreamException {
		ScopeInfo parentInfo = getScope().getInfo();
		if (parentInfo.hasText()) {
			if (!skipSpace || !isWhitespace(parentInfo.getText())) {
				throw new XMLStreamException("Mixed content is not supported: '" + parentInfo.getText() + "'");
			}
			parentInfo.setText(null);
		}
		String fieldName = getFieldName(newScope.getPrefix(), newScope.getLocalName());
		if (parentInfo.pendingStartArray) {
			writeStartArray(fieldName);
		}
		try {
			if (parentInfo.getArrayName() == null) {
				if (!parentInfo.startObjectWritten) {
					target.startObject();
					parentInfo.startObjectWritten = true;
				}
			} else if (autoEndArray && !fieldName.equals(parentInfo.getArrayName())) {
				writeEndArray();
			}
			if (parentInfo.getArrayName() == null) {
				target.name(fieldName);
			} else {
				parentInfo.incArraySize();
			}
		} catch (IOException e) {
			throw new XMLStreamException("Cannot write start element: " + fieldName, e);
		}
		newScope.setInfo(new ScopeInfo());
	}
	
	@Override
	protected void writeStartElementTagEnd() throws XMLStreamException {
		if (getScope().isEmptyElement()) {
			writeEndElementTag();
		}
	}

	@Override
	protected void writeEndElementTag() throws XMLStreamException {
		try {
			if (getScope().getInfo().hasText()) {
				if (getScope().getInfo().startObjectWritten) {
					target.name("$");
				}
				target.value(getScope().getInfo().getText());
			}
			if (autoEndArray && getScope().getInfo().getArrayName() != null) {
				writeEndArray();
			}
			if (getScope().getInfo().startObjectWritten) {
				target.endObject();
			} else if (!getScope().getInfo().hasText()) {
				target.value(null);
			}
		} catch (IOException e) {
			throw new XMLStreamException("Cannot write end element: " + getFieldName(getScope().getPrefix(), getScope().getLocalName()), e);
		}
	}

	@Override
	protected void writeAttr(String prefix, String localName, String value) throws XMLStreamException {
		String name = XMLConstants.DEFAULT_NS_PREFIX.equals(prefix) ? localName : prefix + prefixSeparator + localName;
		try {
			if (!getScope().getInfo().startObjectWritten) {
				target.startObject();
				getScope().getInfo().startObjectWritten = true;
			}
			target.name('@' + name);
			target.value(value);
		} catch (IOException e) {
			throw new XMLStreamException("Cannot write attribute: " + name, e);
		}
	}
	
	@Override
	protected void writeData(String data, int type) throws XMLStreamException {
		switch(type) {
		case XMLStreamConstants.CHARACTERS:
		case XMLStreamConstants.CDATA:
			if (getScope().getLastChild() != null) {
				if (!skipSpace || !isWhitespace(data)) {
					throw new XMLStreamException("Mixed content is not supported: '" + getScope().getInfo().getText() + "'");
				}
			} else {
				getScope().getInfo().addText(data);
			}
			break;
		case XMLStreamConstants.COMMENT: // ignore comments
			break;
		default:
			throw new UnsupportedOperationException("Cannot write data of type " + type);
		}
	}

	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		super.writeStartDocument(encoding, version);
		try {
			target.startObject();
		} catch (IOException e) {
			throw new XMLStreamException("Cannot start document", e);
		}
		getScope().getInfo().startObjectWritten = true;
	}

	
	@Override
	public void writeEndDocument() throws XMLStreamException {
		super.writeEndDocument();
		try {
			target.endObject();
		} catch (IOException e) {
			throw new XMLStreamException("Cannot end document", e);
		}
		getScope().getInfo().startObjectWritten = false;
	}

	public void writeStartArray(String fieldName) throws XMLStreamException {
		if (autoEndArray && getScope().getInfo().getArrayName() != null) {
			writeEndArray();
		}
		getScope().getInfo().startArray(fieldName);
		getScope().getInfo().pendingStartArray = false;
		try {
			if (!getScope().getInfo().startObjectWritten) {
				target.startObject();
				getScope().getInfo().startObjectWritten = true;
			}
			target.name(fieldName);
			target.startArray();
		} catch (IOException e) {
			throw new XMLStreamException("Cannot start array: " + fieldName, e);
		}
	}

	public void writeEndArray() throws XMLStreamException {
		getScope().getInfo().endArray();
		try {
			target.endArray();
		} catch (IOException e) {
			throw new XMLStreamException("Cannot end array: " + getScope().getInfo().getArrayName(), e);
		}
	}

	@Override
	public void close() throws XMLStreamException {
		super.close();
		try {
			target.close();
		} catch (IOException e) {
			throw new XMLStreamException("Close failed", e);
		}
	}

	@Override
	public void flush() throws XMLStreamException {
		try {
			target.flush();
		} catch (IOException e) {
			throw new XMLStreamException("Flush failed", e);
		}
	}

	@Override
	protected void writePI(String target, String data) throws XMLStreamException {
		if (multiplePI && JsonXMLStreamConstants.MULTIPLE_PI_TARGET.equals(target)) {
			if (data == null || data.trim().isEmpty()) {
				getScope().getInfo().pendingStartArray = true;
			} else {
				writeStartArray(data.trim());
			}
		}
	}
}
