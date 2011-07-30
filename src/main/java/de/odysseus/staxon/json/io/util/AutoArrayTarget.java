package de.odysseus.staxon.json.io.util;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;

import de.odysseus.staxon.json.JsonXMLStreamWriter;
import de.odysseus.staxon.json.io.JsonStreamTarget;
import de.odysseus.staxon.json.io.JsonStreamToken;

/**
 * Experimental target filter to auto-insert array boundaries.
 * 
 * Note: this class caches all events and flushes to the
 * underlying target after receiving the last close-object
 * event, which may cause memory issues for large documents.
 * Also, auto-recognition of array boundaries never creates
 * arrays with a single element.
 * 
 * It is recommended to handle array boundaries via the
 * {@link JsonXMLStreamWriter#writeStartArray(String)} and
 * {@link JsonXMLStreamWriter#writeEndArray(String)} methods
 * or by producing <code>&lt;?xml-muliple ...?&gt;</code>
 * processing instructions.
 */
public class AutoArrayTarget implements JsonStreamTarget {
	/**
	 * Event type
	 */
	static interface Event {
		JsonStreamToken token();
		void write(JsonStreamTarget target) throws IOException;
	}
	
	static final Event START_OBJECT = new Event() {
		@Override
		public void write(JsonStreamTarget target) throws IOException {
			target.startObject();
		}
		public JsonStreamToken token() {
			return JsonStreamToken.START_OBJECT;
		}
		@Override
		public String toString() {
			return token().name();
		}
	};

	static final Event END_OBJECT = new Event() {
		@Override
		public void write(JsonStreamTarget target) throws IOException {
			target.endObject();
		}
		public JsonStreamToken token() {
			return JsonStreamToken.END_OBJECT;
		}
		@Override
		public String toString() {
			return token().name();
		}
	};

	static final Event END_ARRAY = new Event() {
		@Override
		public void write(JsonStreamTarget target) throws IOException {
			target.endArray();
		}
		public JsonStreamToken token() {
			return JsonStreamToken.END_ARRAY;
		}
		@Override
		public String toString() {
			return token().name();
		}
	};

	static final class NameEvent implements Event {
		final String name;
		boolean array;
		
		NameEvent(String name) {
			this.name = name;
		}
		@Override
		public void write(JsonStreamTarget target) throws IOException {
			target.name(name);
			if (array) {
				target.startArray();
			}
		}
		@Override
		public JsonStreamToken token() {
			return JsonStreamToken.NAME;
		}
		public String name() {
			return name;
		}
		public boolean isArray() {
			return array;
		}
		public void setArray(boolean array) {
			this.array = array;
		}
		@Override
		public String toString() {
			if (array) {
				return token().name() + " = " + name + " " + JsonStreamToken.START_ARRAY;
			} else {
				return token().name() + " = " + name;
			}
		}
	}
	
	static final class ValueEvent implements Event {
		final String value;
		
		ValueEvent(String value) {
			this.value = value;
		}
		@Override
		public void write(JsonStreamTarget target) throws IOException {
			target.value(value);
		}
		@Override
		public JsonStreamToken token() {
			return JsonStreamToken.VALUE;
		}
		@Override
		public String toString() {
			return token().name() + " = " + value;
		}
	}

	/*
	 * delegate target
	 */
	private final JsonStreamTarget delegate;
	
	/*
	 * Event queue 
	 */
	private final Deque<Event> events = new LinkedList<Event>();

	/*
	 * Field stack
	 */
	private final Stack<NameEvent> fields = new Stack<NameEvent>();
	
	public AutoArrayTarget(JsonStreamTarget delegate) {
		this.delegate = delegate;
	}

	private void pushField(String name) {
		events.add(fields.push(new NameEvent(name)));
	}

	private void popField() {
		if (fields.peek().isArray()) {
			events.add(END_ARRAY);
		}
		fields.pop();
	}
	
	@Override
	public void name(String name) throws IOException {
		if (events.peekLast().token() == JsonStreamToken.START_OBJECT) {
			pushField(name);
		} else {
			if (name.equals(fields.peek().name())) {
				fields.peek().setArray(true);
			} else {
				popField();
				pushField(name);
			}
		}
	}

	@Override
	public void value(String value) throws IOException {
		events.add(new ValueEvent(value));
	}

	@Override
	public void startObject() throws IOException {
		events.add(START_OBJECT);
	}

	@Override
	public void endObject() throws IOException {
		popField();
		events.add(END_OBJECT);
		if (fields.isEmpty()) {
			while (!events.isEmpty()) {
				events.pollFirst().write(delegate);
			}
		}
	}

	@Override
	public void startArray() throws IOException {
		throw new IllegalStateException();
	}

	@Override
	public void endArray() throws IOException {
		throw new IllegalStateException();
	}

	@Override
	public void close() throws IOException {
		while (!events.isEmpty()) {
			events.pollFirst().write(delegate);
		}
		delegate.close();
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}
}
