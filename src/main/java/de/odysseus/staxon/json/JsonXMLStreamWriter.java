package de.odysseus.staxon.json;

import java.io.IOException;

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
 * <p>The writer consumes processing instructions
 * <code>&lt;?xml-multiple element-name?&gt;</code> to properly insert JSON array tokens (<code>'['</code>
 * and <code>']'</code>). The client must provide this instruction through the
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
	private final boolean useMultiplePI = true;
	private final boolean autoEndArray = useMultiplePI;

	public JsonXMLStreamWriter(JsonStreamTarget target) {
		super(new ScopeInfo());
		this.target = target;
	}

	@Override
	protected void writeElementTagStart(XMLStreamWriterScope<ScopeInfo> newScope) throws XMLStreamException {
		ScopeInfo parentInfo = getScope().getInfo();
		String fieldName = newScope.getTagName();
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
			throw new XMLStreamException("Cannot write end element: " + getScope().getTagName(), e);
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

	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		super.writeStartDocument(encoding, version);
		try {
			target.startObject();
		} catch (IOException e) {
			throw new XMLStreamException("Cannot start document", e);
		}
		getScope().getInfo().startObjectWritten = true;
	}

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

	public void close() throws XMLStreamException {
		super.close();
		try {
			target.close();
		} catch (IOException e) {
			throw new XMLStreamException("Close failed", e);
		}
	}

	public void flush() throws XMLStreamException {
		try {
			target.flush();
		} catch (IOException e) {
			throw new XMLStreamException("Flush failed", e);
		}
	}

	@Override
	protected void writePI(String target, String data) throws XMLStreamException {
		if (useMultiplePI && JsonXMLStreamUtil.PI_MULTIPLE_TARGET.equals(target)) {
			writeStartArray(data);
		}
	}
}
