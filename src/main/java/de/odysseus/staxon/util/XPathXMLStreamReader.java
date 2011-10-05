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
package de.odysseus.staxon.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * A {@link StreamReaderDelegate} providing XPath expressions.
 */
public class XPathXMLStreamReader extends StreamReaderDelegate {
	static class Scope {
		private Scope parent;
		private QName name;
		private int position;
		private Map<QName, Integer> children = new HashMap<QName, Integer>();

		/**
		 * Reset all properties to their initial state.
		 */
		void reset() {
			parent = null;
			name = null;
			position = 0;
			children.clear();
		}

		Scope getParent() {
			return parent;
		}

		void setParent(Scope parent) {
			this.parent = parent;
		}

		int getChildCount(QName name) {
			return children.containsKey(name) ? children.get(name) : 0;
		}

		void setChildCount(QName name, int value) {
			children.put(name, Integer.valueOf(value));
		}

		QName getName() {
			return name;
		}

		void setName(QName name) {
			this.name = name;
		}

		int getPosition() {
			return position;
		}

		void setPosition(int position) {
			this.position = position;
		}

		StringBuilder append(StringBuilder builder, NamespaceContext context) {
			if (parent != null) {
				parent.append(builder, context);
			}
			builder.append('/');
			String prefix = context == null ? getName().getPrefix() : context.getPrefix(name.getNamespaceURI());
			if (prefix == null) {
				throw new IllegalArgumentException("Namespace URI '" + name.getNamespaceURI() + "' has no prefix");
			}
			if (!XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
				builder.append(name.getPrefix()).append(':');
			}
			builder.append(name.getLocalPart());
			if (parent != null) {
				builder.append('[').append(position).append(']');
			}
			return builder;
		}
	}

	private final Stack<Scope> recycle = new Stack<Scope>();

	private Scope current;

	public XPathXMLStreamReader(XMLStreamReader reader) {
		super(reader);
		enter();
	}
	
	@Override
	public void setParent(XMLStreamReader reader) {
		throw new UnsupportedOperationException();
	}

	/*
	 * Called before moving cursor.
	 */
	private void leave() {
		if (getEventType() == XMLStreamConstants.END_ELEMENT) {
			/*
			 * Pop old scope, making its parent the new current scope.
			 * Does not change the child count of the parent.
			 */
			Scope parent = current.getParent();
			current.reset();
			recycle.push(current);
			current = parent;
		}
	}

	/*
	 * Called after moving cursor.
	 */
	private void enter() {
		if (getEventType() == XMLStreamConstants.START_ELEMENT) {
			/*
			 * Push new scope, making it the new current scope.
			 * Increases the child count of the parent by one.
			 */
			Scope child = recycle.isEmpty() ? new Scope() : recycle.pop();
			child.setName(getName());
			if (current == null) {
				child.setPosition(1);
				child.setParent(null);
			} else {
				child.setPosition(current.getChildCount(child.getName()) + 1);
				current.setChildCount(child.getName(), child.getPosition());
				child.setParent(current);
			}
			current = child;
		}
	}

	@Override
	public int next() throws XMLStreamException {
		leave();
		super.next();
		enter();
		return getEventType();
	}

	@Override
	public int nextTag() throws XMLStreamException {
		leave();
		super.nextTag();
		enter();
		return getEventType();
	}

	@Override
	public void close() throws XMLStreamException {
		try {
			super.close();
		} finally {
			recycle.clear();
			current = null;
		}
	}
	
	@Override
	public String toString() {
		return new StringBuilder(getClass().getSimpleName())
			.append('@').append(getXPath())
			.append('(').append(super.toString()).append(')')
			.toString();
	}
	
	/**
	 * Generate the "canonical" XPath string:
	 * <ul>
	 * <li>The expression is absolute (starts with a <code>'/'</code>)</li>
	 * <li>All segments have a (one-based) position, except for the root segment</li>
	 * </ul>
	 * <p>
	 * More formally, the canonical form is
	 * <code>'/' &ltname&gt ('/' &ltname&gt '[' &ltposition&gt ']')*</code>
	 * </p>
	 * 
	 * @param context namespace context used to lookup prefixes (may be <code>null</code>)
	 * @return "canonical" XPath expression.
	 */
	public String getXPath(NamespaceContext context) {
		return current == null ? null : current.append(new StringBuilder(), context).toString();
	}

	/**
	 * Same as <code>getXPath(null)</code>.
	 * @return "canonical" XPath expression.
	 * @see #getXPath(NamespaceContext)
	 */
	public String getXPath() {
		return getXPath(null);
	}
}
