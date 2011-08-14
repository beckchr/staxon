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

import de.odysseus.staxon.core.AbstractXMLStreamWriter;
import de.odysseus.staxon.core.XMLStreamWriterScope;
import de.odysseus.staxon.json.io.JsonStreamTarget;

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
 */
public class JsonXMLStreamWriter extends AbstractXMLStreamWriter<JsonXMLStreamWriter.ScopeInfo> {
	static class ScopeInfo extends JsonXMLStreamScopeInfo {
		boolean startObjectWritten = false;
		boolean contentDataWritten = false;
	}

	private final JsonStreamTarget target;
	private final boolean multiplePI;
	private final boolean autoEndArray;

	/**
	 * Create writer instance.
	 * @param target stream target
	 * @param multiplePI whether to use processing instruction to trigger array start
	 */
	public JsonXMLStreamWriter(JsonStreamTarget target, boolean multiplePI) {
		super(new ScopeInfo());
		this.target = target;
		this.multiplePI = multiplePI;
		this.autoEndArray = true;
	}

	private String getFieldName(String prefix, String localName) {
		return XMLConstants.DEFAULT_NS_PREFIX.equals(prefix) ? localName : prefix + ':' + localName;
	}
	
	@Override
	protected void writeElementTagStart(XMLStreamWriterScope<ScopeInfo> newScope) throws XMLStreamException {
		ScopeInfo parentInfo = getScope().getInfo();
		String fieldName = getFieldName(newScope.getPrefix(), newScope.getLocalName());
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
	protected void writeElementTagEnd() throws XMLStreamException {
		if (getScope().isEmptyElement()) {
			writeEndElementTag();
		}
	}

	@Override
	protected void writeEndElementTag() throws XMLStreamException {
		try {
			if (autoEndArray && getScope().getInfo().getArrayName() != null) {
				writeEndArray();
			}
			if (getScope().getInfo().startObjectWritten) {
				target.endObject();
			} else if (getScope().isEmptyElement() || !getScope().getInfo().contentDataWritten) {
				target.value(null);
			}
		} catch (IOException e) {
			throw new XMLStreamException("Cannot write end element: " + getFieldName(getScope().getPrefix(), getScope().getLocalName()), e);
		}
	}

	@Override
	protected void writeProperty(String name, String value) throws XMLStreamException {
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
		try {
			switch(type) {
			case XMLStreamConstants.CHARACTERS:
			case XMLStreamConstants.CDATA:
				if (getScope().getInfo().startObjectWritten) {
					target.name("$");
				}
				target.value(data);
				getScope().getInfo().contentDataWritten = true;
				break;
			case XMLStreamConstants.COMMENT: // ignore comments
				break;
			default:
				throw new UnsupportedOperationException("Cannot write data of type " + type);
			}
		} catch (IOException e) {
			throw new XMLStreamException(e);
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
			writeStartArray(data);
		}
	}
}
