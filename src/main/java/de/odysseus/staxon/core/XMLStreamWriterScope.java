package de.odysseus.staxon.core;

import javax.xml.namespace.NamespaceContext;

public class XMLStreamWriterScope<T> extends AbstractXMLStreamScope {
	private final boolean emptyElement;

	private T info;

	public XMLStreamWriterScope(String defaultNamespace, T info) {
		super(defaultNamespace);
		this.info = info;
		this.emptyElement = false;
	}

	public XMLStreamWriterScope(NamespaceContext parent, T info) {
		super(parent);
		this.info = info;
		this.emptyElement = false;
	}

	public XMLStreamWriterScope(XMLStreamWriterScope<T> parent, String prefix, String localName, boolean emptyElement) {
		super(parent, prefix, localName);
		this.emptyElement = emptyElement;
	}

	public T getInfo() {
		return info;
	}
	
	public void setInfo(T info) {
		this.info = info;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public XMLStreamWriterScope<T> getParent() {
		return (XMLStreamWriterScope<T>)super.getParent();
	}
	
	public boolean isEmptyElement() {
		return emptyElement;
	}
}
