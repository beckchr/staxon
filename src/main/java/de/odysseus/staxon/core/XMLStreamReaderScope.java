package de.odysseus.staxon.core;

import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;


public class XMLStreamReaderScope<T> extends AbstractXMLStreamScope {
	private List<Pair<String, String>> declarations;
	private List<Pair<QName, String>> attributes;
	private T info;

	public XMLStreamReaderScope(String defaultNamespace, T info) {
		super(defaultNamespace);
		this.info = info;
	}

	public XMLStreamReaderScope(NamespaceContext parent, T info) {
		super(parent);
		this.info = info;
	}

	public XMLStreamReaderScope(XMLStreamReaderScope<T> parent, String prefix, String localName) {
		super(parent, prefix, localName);
	}

	public T getInfo() {
		return info;
	}
	
	public void setInfo(T info) {
		this.info = info;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public XMLStreamReaderScope<T> getParent() {
		return (XMLStreamReaderScope<T>)super.getParent();
	}

	public int getNamespaceCount() {
		return declarations == null ? 0 : declarations.size();
	}

	public String getNamespacePrefix(int index) {
		return declarations == null ? null : declarations.get(index).getFirst();
	}

	public String getNamespaceURI(int index) {
		return declarations == null ? null : declarations.get(index).getSecond();
	}

	void addNamespaceURI(String prefix, String namespaceURI) {
		if (declarations == null) {
			declarations = new LinkedList<Pair<String, String>>();
		}
		declarations.add(new Pair<String, String>(prefix, namespaceURI));
		setPrefix(prefix, namespaceURI);
	}

	void addAttribute(QName name, String value) {
		if (attributes == null) {
			attributes = new LinkedList<Pair<QName, String>>();
		}
		attributes.add(new Pair<QName, String>(name, value));
	}

	public int getAttributeCount() {
		return attributes == null ? 0 : attributes.size();
	}

	public QName getAttributeName(int index) {
		return attributes.get(index).getFirst();
	}

	public String getAttributeValue(int index) {
		return attributes.get(index).getSecond();
	}

	public String getAttributeValue(String namespaceURI, String localName) {
		if (attributes != null) {
			if (namespaceURI == null) {
				namespaceURI = XMLConstants.NULL_NS_URI;
			}
			for (Pair<QName, String> attribute : attributes) {
				if (localName.equals(attribute.getFirst().getLocalPart())) {
					if (namespaceURI.equals(attribute.getFirst().getNamespaceURI())) {
						return attribute.getSecond();
					}
				}
			}
		}
		return null;
	}
}
