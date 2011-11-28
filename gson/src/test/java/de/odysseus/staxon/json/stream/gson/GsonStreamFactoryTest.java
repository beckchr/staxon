package de.odysseus.staxon.json.stream.gson;

import junit.framework.Assert;

import org.junit.Test;

import de.odysseus.staxon.json.stream.JsonStreamFactory;

public class GsonStreamFactoryTest {
	@Test
	public void test() {
		Assert.assertTrue(JsonStreamFactory.newFactory() instanceof GsonStreamFactory);
	}
}
