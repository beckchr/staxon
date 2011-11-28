package de.odysseus.staxon.sample.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import de.odysseus.staxon.json.jaxrs.jaxb.JsonXMLArrayProvider;
import de.odysseus.staxon.json.jaxrs.jaxb.JsonXMLObjectProvider;

public class CustomerApplication extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		/*
		 * Providers
		 */
		classes.add(JsonXMLObjectProvider.class);
		classes.add(JsonXMLArrayProvider.class);
		/*
		 * Resources
		 */
		classes.add(CustomerResource.class);
		return classes;
	}
}
