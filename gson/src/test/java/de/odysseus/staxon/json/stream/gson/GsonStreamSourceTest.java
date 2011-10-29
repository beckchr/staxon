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
package de.odysseus.staxon.json.stream.gson;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.Assert;

import org.junit.Test;

import com.google.gson.stream.JsonReader;

import de.odysseus.staxon.json.stream.JsonStreamToken;

public class GsonStreamSourceTest {
	@Test
	public void testObjectValue() throws IOException {
		StringReader reader = new StringReader("{\"alice\":\"bob\"}");
		GsonStreamSource source = new GsonStreamSource(new JsonReader(reader));
		
		Assert.assertEquals(JsonStreamToken.START_OBJECT, source.peek());
		source.startObject();

		Assert.assertEquals(JsonStreamToken.NAME, source.peek());
		Assert.assertEquals("alice", source.name());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("bob", source.value());

		Assert.assertEquals(JsonStreamToken.END_OBJECT, source.peek());
		source.endObject();
		
		Assert.assertEquals(JsonStreamToken.NONE, source.peek());
		source.close();
	}

	@Test
	public void testArrayValue() throws IOException {
		StringReader reader = new StringReader("[\"bob\"]");
		GsonStreamSource source = new GsonStreamSource(new JsonReader(reader));

		Assert.assertEquals(JsonStreamToken.START_ARRAY, source.peek());
		source.startArray();
		
		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("bob", source.value());

		Assert.assertEquals(JsonStreamToken.END_ARRAY, source.peek());
		source.endArray();
		
		Assert.assertEquals(JsonStreamToken.NONE, source.peek());
		source.close();
	}

	@Test
	public void testArray1() throws IOException {
		StringReader reader = new StringReader("{\"alice\":[\"bob\"]}");
		GsonStreamSource source = new GsonStreamSource(new JsonReader(reader));

		Assert.assertEquals(JsonStreamToken.START_OBJECT, source.peek());
		source.startObject();

		Assert.assertEquals(JsonStreamToken.NAME, source.peek());
		Assert.assertEquals("alice", source.name());

		Assert.assertEquals(JsonStreamToken.START_ARRAY, source.peek());
		source.startArray();
		
		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("bob", source.value());

		Assert.assertEquals(JsonStreamToken.END_ARRAY, source.peek());
		source.endArray();
		
		Assert.assertEquals(JsonStreamToken.END_OBJECT, source.peek());
		source.endObject();
		
		Assert.assertEquals(JsonStreamToken.NONE, source.peek());
		source.close();
	}

	@Test
	public void testArray2() throws IOException {
		StringReader reader = new StringReader("{\"alice\":{\"bob\":[\"edgar\",\"charlie\"]}}");
		GsonStreamSource source = new GsonStreamSource(new JsonReader(reader));

		Assert.assertEquals(JsonStreamToken.START_OBJECT, source.peek());
		source.startObject();

		Assert.assertEquals(JsonStreamToken.NAME, source.peek());
		Assert.assertEquals("alice", source.name());

		Assert.assertEquals(JsonStreamToken.START_OBJECT, source.peek());
		source.startObject();

		Assert.assertEquals(JsonStreamToken.NAME, source.peek());
		Assert.assertEquals("bob", source.name());

		Assert.assertEquals(JsonStreamToken.START_ARRAY, source.peek());
		source.startArray();
		
		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("edgar", source.value());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("charlie", source.value());

		Assert.assertEquals(JsonStreamToken.END_ARRAY, source.peek());
		source.endArray();
		
		Assert.assertEquals(JsonStreamToken.END_OBJECT, source.peek());
		source.endObject();
		
		Assert.assertEquals(JsonStreamToken.END_OBJECT, source.peek());
		source.endObject();
		
		Assert.assertEquals(JsonStreamToken.NONE, source.peek());
		source.close();
	}

	@Test
	public void testArray3() throws IOException {
		StringReader reader = new StringReader("{\"alice\":{\"edgar\":[\"bob\"],\"charlie\":[\"bob\"]}}");
		GsonStreamSource source = new GsonStreamSource(new JsonReader(reader));

		Assert.assertEquals(JsonStreamToken.START_OBJECT, source.peek());
		source.startObject();

		Assert.assertEquals(JsonStreamToken.NAME, source.peek());
		Assert.assertEquals("alice", source.name());

		Assert.assertEquals(JsonStreamToken.START_OBJECT, source.peek());
		source.startObject();

		Assert.assertEquals(JsonStreamToken.NAME, source.peek());
		Assert.assertEquals("edgar", source.name());

		Assert.assertEquals(JsonStreamToken.START_ARRAY, source.peek());
		source.startArray();
		
		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("bob", source.value());

		Assert.assertEquals(JsonStreamToken.END_ARRAY, source.peek());
		source.endArray();
		
		Assert.assertEquals(JsonStreamToken.NAME, source.peek());
		Assert.assertEquals("charlie", source.name());

		Assert.assertEquals(JsonStreamToken.START_ARRAY, source.peek());
		source.startArray();
		
		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("bob", source.value());

		Assert.assertEquals(JsonStreamToken.END_ARRAY, source.peek());
		source.endArray();
		
		Assert.assertEquals(JsonStreamToken.END_OBJECT, source.peek());
		source.endObject();
		
		Assert.assertEquals(JsonStreamToken.END_OBJECT, source.peek());
		source.endObject();
		
		Assert.assertEquals(JsonStreamToken.NONE, source.peek());
		source.close();
	}

	@Test
	public void testString() throws IOException {
		StringReader reader = new StringReader("[\"\",\"abc\",\"\\b\\f\\n\\r\\t\",\"\\\"\",\"\\\\\",\"\\u001F\"]");
		GsonStreamSource source = new GsonStreamSource(new JsonReader(reader));

		Assert.assertEquals(JsonStreamToken.START_ARRAY, source.peek());
		source.startArray();
		
		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("", source.value());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("abc", source.value());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("\b\f\n\r\t", source.value());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("\"", source.value());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("\\", source.value());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("\u001F", source.value());

		Assert.assertEquals(JsonStreamToken.END_ARRAY, source.peek());
		source.endArray();
		
		Assert.assertEquals(JsonStreamToken.NONE, source.peek());
		source.close();
	}

	@Test
	public void testLiteralValues() throws IOException {
		StringReader reader = new StringReader("[true,false,null]");
		GsonStreamSource source = new GsonStreamSource(new JsonReader(reader));

		Assert.assertEquals(JsonStreamToken.START_ARRAY, source.peek());
		source.startArray();
		
		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("true", source.value());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("false", source.value());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertNull(source.value());

		Assert.assertEquals(JsonStreamToken.END_ARRAY, source.peek());
		source.endArray();
		
		Assert.assertEquals(JsonStreamToken.NONE, source.peek());
		source.close();
	}

	@Test
	public void testNumberValues() throws IOException {
		StringReader reader = new StringReader("[123,12e3,12E3,12.3,1.2e3,1.2E3]");
		GsonStreamSource source = new GsonStreamSource(new JsonReader(reader));

		Assert.assertEquals(JsonStreamToken.START_ARRAY, source.peek());
		source.startArray();
		
		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("123", source.value());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("12e3", source.value());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("12E3", source.value());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("12.3", source.value());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("1.2e3", source.value());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("1.2E3", source.value());

		Assert.assertEquals(JsonStreamToken.END_ARRAY, source.peek());
		source.endArray();
		
		Assert.assertEquals(JsonStreamToken.NONE, source.peek());
		source.close();
	}	

	@Test(expected = IOException.class)
	public void testUnexpected() throws IOException {
		StringReader reader = new StringReader("\"alice\":\"bob\""); // missing document start/end
		GsonStreamSource source = new GsonStreamSource(new JsonReader(reader));

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		source.value();
		source.peek();
	}

	@Test
	public void testWhitespace() throws IOException {
		StringReader reader = new StringReader("{\r  \"alice\" : \"bob\"\r\n}");
		GsonStreamSource source = new GsonStreamSource(new JsonReader(reader));
		
		Assert.assertEquals(JsonStreamToken.START_OBJECT, source.peek());
		source.startObject();

		Assert.assertEquals(JsonStreamToken.NAME, source.peek());
		Assert.assertEquals("alice", source.name());

		Assert.assertEquals(JsonStreamToken.VALUE, source.peek());
		Assert.assertEquals("bob", source.value());

		Assert.assertEquals(JsonStreamToken.END_OBJECT, source.peek());
		source.endObject();
		
		Assert.assertEquals(JsonStreamToken.NONE, source.peek());
		source.close();
	}
}
