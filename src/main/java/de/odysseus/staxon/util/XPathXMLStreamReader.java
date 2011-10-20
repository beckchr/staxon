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
		private Map<QName, Integer> positions = new HashMap<QName, Integer>();

		/**
		 * Reset all properties to their initial state.
		 */
		void reset() {
			parent = null;
			name = null;
			positions.clear();
		}

		Scope getParent() {
			return parent;
		}

		int getPosition() {
			return parent == null ? 1 : parent.positions.get(name).intValue();
		}
		
		void addChild(Scope child) {
			child.parent = this;

			QName key = child.getName();
			if (positions.containsKey(key)) {				
				positions.put(key, Integer.valueOf(positions.get(key).intValue() + 1));
			} else {
				positions.put(key, Integer.valueOf(1));
			}
		}

		QName getName() {
			return name;
		}

		void setName(QName name) {
			this.name = name;
		}

		StringBuilder append(StringBuilder builder, NamespaceContext context, boolean includePositions) {
			if (parent != null) {
				parent.append(builder, context, includePositions);
			}
			builder.append('/');
			String prefix = context == null ? name.getPrefix() : context.getPrefix(name.getNamespaceURI());
			if (prefix == null) {
				throw new IllegalArgumentException("Namespace URI '" + name.getNamespaceURI() + "' has no prefix");
			}
			if (!XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
				builder.append(prefix).append(':');
			}
			builder.append(name.getLocalPart());
			if (includePositions && parent != null) {
				builder.append('[').append(getPosition()).append(']');
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
			if (current != null) {				
				current.addChild(child);
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
	 * Generate XPath expression:
	 * <ul>
	 * <li>The expression is absolute (starts with a <code>'/'</code>)</li>
	 * <li>If <code>includePositions</code> is set to <code>true</code>,
	 * all segments have a (one-based) position, except for the root segment</li>
	 * </ul>
	 * <p>
	 * More formally, the result matches
	 * </p>
	 * <pre>'/' &ltname&gt ('/' &ltname&gt '[' &ltposition&gt ']')*</pre>
	 * <p>
	 * if <code>includePositions</code> is set to <code>true</code> and
	 * </p>
	 * <pre>'/' &ltname&gt ('/' &ltname&gt)*</pre>
	 * <p>
	 * otherwise.
	 * </p>
	 * @param context namespace context used to lookup prefixes (may be <code>null</code>)
	 * @param includePositions whether to include element positions
	 * @return XPath expression
	 */
	public String getXPath(NamespaceContext context, boolean includePositions) {
		return current == null ? null : current.append(new StringBuilder(), context, includePositions).toString();
	}

	/**
	 * Same as <code>getXPath(null)</code>.
	 * @return XPath expression (with positions)
	 * @see #getXPath(NamespaceContext)
	 */
	public String getXPath() {
		return getXPath(null, true);
	}
	
	/**
	 * Gets the position for the current element (last segment of XPath expression).
	 * Child positions are counted per name. For example, in
	 * <pre>
	 * &lt;alice>
	 *   &lt;edgar/>
	 *   &lt;edgar/>
	 *   &lt;david/>
	 *   &lt;edgar/>
	 * &lt;/alice>
	 * </pre>
	 * <p>
	 * the positions of <code>alice</code>' children are 1, 2, 1 and 3.
	 * </p>
	 * @return the (one-based) position of the current element
	 */
	public int getPosition() {
		return current == null ? 0 : current.getPosition();
	}
}
