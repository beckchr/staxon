package de.odysseus.staxon.sample.service;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.JerseyTest;

public class CustomerResourceTest extends JerseyTest {
	public CustomerResourceTest() {
		super("de.odysseus.staxon.sample.service");
	}

	@Test
	public void testCustomer() {
		/*
		 * get customer
		 */
		ClientResponse response1 =
				resource().
				path("/customer/get").
				get(ClientResponse.class);
		Assert.assertEquals(200, response1.getStatus());
		String json = response1.getEntity(String.class);
		/*
		 * post customer
		 */
		ClientResponse response2 =
				resource().
				path("/customer/post").
				type(MediaType.APPLICATION_JSON_TYPE).
				post(ClientResponse.class, json);
		Assert.assertEquals(201, response2.getStatus());
		String text = response2.getEntity(String.class);
		Assert.assertEquals("Received: David Lynch", text);
	}

	@Test
	public void testCustomerArray() {
		/*
		 * get customer array
		 */
		ClientResponse response1 =
				resource().
				path("/customer/get/array").
				get(ClientResponse.class);
		Assert.assertEquals(200, response1.getStatus());
		String json = response1.getEntity(String.class);
		/*
		 * post customer array
		 */
		ClientResponse response2 =
				resource().
				path("/customer/post/array").
				type(MediaType.APPLICATION_JSON_TYPE).
				post(ClientResponse.class, json);
		Assert.assertEquals(201, response2.getStatus());
		String text = response2.getEntity(String.class);
		Assert.assertEquals("Received: 2 customers", text);
	}
}
