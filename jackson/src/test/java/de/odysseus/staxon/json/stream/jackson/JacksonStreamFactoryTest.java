package de.odysseus.staxon.json.stream.jackson;

import junit.framework.Assert;

import org.junit.Test;

import de.odysseus.staxon.json.stream.JsonStreamFactory;

public class JacksonStreamFactoryTest {
	@Test
	public void test() {
		Assert.assertTrue(JsonStreamFactory.newFactory() instanceof JacksonStreamFactory);
	}
}
