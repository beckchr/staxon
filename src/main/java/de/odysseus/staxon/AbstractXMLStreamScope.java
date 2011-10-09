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
package de.odysseus.staxon;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

/**
 * Represent document/element scope. Used to store namespace declarations and
 * attributes, implements {@link NamespaceContext}.
 */
public abstract class AbstractXMLStreamScope implements NamespaceContext {
	private final NamespaceContext parent;
	private final String prefix;
	private final String localName;
	private final String namespaceURI;

	private String defaultNamespace;
	private List<Pair<String, String>> prefixes;
	private AbstractXMLStreamScope lastChild;
	private boolean startTagClosed;

	/**
	 * Create root scope.
	 * 
	 * @param defaultNamespace
	 */
	public AbstractXMLStreamScope(String defaultNamespace) {
		this.parent = null;
		this.prefix = null;
		this.localName = null;
		this.namespaceURI = null;
		this.defaultNamespace = defaultNamespace;
		this.startTagClosed = true;
	}

	/**
	 * Create root scope.
	 * 
	 * @param parent
	 *            root namespace context
	 */
	public AbstractXMLStreamScope(NamespaceContext parent) {
		this.parent = parent;
		this.prefix = null;
		this.localName = null;
		this.namespaceURI = null;
		this.defaultNamespace = parent.getNamespaceURI(XMLConstants.NULL_NS_URI);
		this.startTagClosed = true;
	}

	/**
	 * Create element scope.
	 * 
	 * @param parent
	 * @param prefix
	 * @param localName
	 */
	public AbstractXMLStreamScope(AbstractXMLStreamScope parent, String prefix, String localName, String namespaceURI) {
		this.parent = parent;
		this.prefix = prefix;
		this.localName = localName;
		this.namespaceURI = namespaceURI;
		this.startTagClosed = false;
		
		defaultNamespace = parent.getNamespaceURI(XMLConstants.NULL_NS_URI);
		parent.lastChild = this;
		parent.startTagClosed = true;
	}

	public String getPrefix() {
		return prefix == null ? getPrefix(namespaceURI) : prefix;
	}
	
	public String getLocalName() {
		return localName;
	}
	
	public String getNamespaceURI() {
		return namespaceURI == null ? getNamespaceURI(prefix) : namespaceURI;
	}

	public boolean isRoot() {
		return localName == null;
	}

	public AbstractXMLStreamScope getParent() {
		return isRoot() ? null : (AbstractXMLStreamScope)parent;
	}

	public AbstractXMLStreamScope getLastChild() {
		return lastChild;
	}

	public boolean isStartTagClosed() {
		return startTagClosed;
	}

	void setStartTagClosed(boolean startTagClosed) throws XMLStreamException {
		if (startTagClosed) {
			if (prefix == null) {
				if (!XMLConstants.NULL_NS_URI.equals(namespaceURI) && getPrefix(namespaceURI) == null) {
					throw new XMLStreamException("No prefix for namespace URI: " + namespaceURI);
				}
			} else if (namespaceURI == null) {
				if (!XMLConstants.DEFAULT_NS_PREFIX.equals(prefix) && XMLConstants.NULL_NS_URI.equals(getNamespaceURI(prefix))) {
					throw new XMLStreamException("Unbound prefix: " + prefix);
				}
			} else {
				if (!namespaceURI.equals(getNamespaceURI(prefix))) {
					if (XMLConstants.NULL_NS_URI.equals(namespaceURI)) {
						throw new XMLStreamException("Prefix '" + prefix +"' is bound to: " + getNamespaceURI(prefix));
					} else {
						throw new XMLStreamException("Prefix '" + prefix +"' is not bound to: " + namespaceURI);
					}
				}
			}
		}
		this.startTagClosed = startTagClosed;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		if (namespaceURI == null) {
			throw new IllegalArgumentException("Namespace URI must not be null");
		} else if (namespaceURI.equals(defaultNamespace)) {
			return XMLConstants.DEFAULT_NS_PREFIX;
		} else if (XMLConstants.XML_NS_URI.equals(namespaceURI)) {
			return XMLConstants.XML_NS_PREFIX;
		} else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI)) {
			return XMLConstants.XMLNS_ATTRIBUTE;
		} else {
			if (prefixes != null) {
				for (Pair<String, String> pair : prefixes) {
					if (pair.getSecond().equals(namespaceURI)) {
						return pair.getFirst();
					}
				}
			}
			return parent == null ? null : parent.getPrefix(namespaceURI);
		}
	}

	public void setPrefix(String prefix, String namespaceURI) {
		if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
			defaultNamespace = namespaceURI;
		} else if (XMLConstants.XML_NS_PREFIX.equals(namespaceURI)) {
			throw new IllegalArgumentException("Cannot bind to prefix: " + prefix);
		} else if (XMLConstants.XMLNS_ATTRIBUTE.equals(namespaceURI)) {
			throw new IllegalArgumentException("Cannot bind to prefix: " + prefix);
		} else {
			if (prefixes == null) {
				prefixes = new LinkedList<Pair<String, String>>();
			} else {
				Iterator<Pair<String, String>> iterator = prefixes.iterator();
				while (iterator.hasNext()) {
					if (iterator.next().getFirst().equals(prefix)) {
						iterator.remove();
					}
				}
			}
			prefixes.add(new Pair<String, String>(prefix, namespaceURI));
		}
	}

	@Override
	public Iterator<String> getPrefixes(final String namespaceURI) {
		if (namespaceURI == null) {
			throw new IllegalArgumentException("Namespace URI must not be null");
		} else if (XMLConstants.XML_NS_URI.equals(namespaceURI)) {
			return Arrays.asList(XMLConstants.XML_NS_PREFIX).iterator();
		} else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI)) {
			return Arrays.asList(XMLConstants.XMLNS_ATTRIBUTE).iterator();
		} else {
			return new Iterator<String>() {
				int state = 0;
				String next = null;
				Iterator<Pair<String, String>> pairs;
				Iterator<?> above;

				private String next0() {
					switch (state) {
					case 0: // check default
						if (namespaceURI.equals(defaultNamespace)) {
							state = 1;
							return XMLConstants.DEFAULT_NS_PREFIX;
						}
					case 1: // check pairs
						state = 1;
						if (prefixes != null) {
							if (pairs == null) {
								pairs = prefixes.iterator();
							}
							Pair<String, String> p;
							while (pairs.hasNext()) {
								p = pairs.next();
								if (namespaceURI.equals(p.getSecond())) {
									return p.getFirst();
								}
							}
						}
					case 2: // check above
						state = 2;
						if (parent != null) {
							if (above == null) {
								above = parent.getPrefixes(namespaceURI);
							}
							if (above.hasNext()) {
								return above.next().toString();
							}
						}
					default:
						state = -1;
						return null;
					}
				}

				@Override
				public boolean hasNext() {
					if (next == null) {
						next = next0();
					}
					return next != null;
				}

				@Override
				public String next() {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}
					String result = next;
					next = null;
					return result;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Cannot remove prefix");
				}
			};
		}
	}

	@Override
	public String getNamespaceURI(String prefix) {
		if (prefix == null) {
			throw new IllegalArgumentException("Prefix must not be null");
		} else if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
			return defaultNamespace;
		} else if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
			return XMLConstants.XML_NS_URI;
		} else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
			return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		} else {
			if (prefixes != null) {
				for (Pair<String, String> pair : prefixes) {
					if (pair.getFirst().equals(prefix)) {
						return pair.getSecond();
					}
				}
			}
			return parent == null ? XMLConstants.NULL_NS_URI : parent.getNamespaceURI(prefix);
		}
	}
}
