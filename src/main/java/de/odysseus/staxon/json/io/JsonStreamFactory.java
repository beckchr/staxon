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
package de.odysseus.staxon.json.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import javax.xml.stream.FactoryConfigurationError;

public abstract class JsonStreamFactory {
	/**
	 * <p>Create a new instance of a JsonStreamFactory.</p>
	 * <p>Determines the class to instantiate as follows:
	 * <ol>
	 * <li>Use the Services API (as detailed in the JAR specification). If a resource with the name
	 * of META-INF/services/de.odysseus.staxon.json.io.JsonStreamFactory exists, then its first line,
	 * if present, is used as the UTF-8 encoded name of the implementation class.</li>
	 * <li>Use the properties file "lib/staxon.properties" in the JRE directory. If this file exists
	 * and  is readable by the java.util.Properties.load(InputStream) method, and it contains an entry
	 * whose key is "de.odysseus.staxon.json.io.JsonStreamFactory", then the value of that entry is
	 * used as the name of the implementation class.</li>
	 * <li>Use the de.odysseus.staxon.json.io.JsonStreamFactory system property. If a system property
	 * with this name is defined, then its value is used as the name of the implementation class.</li>
	 * <li>Use platform default: try "de.odysseus.staxon.json.io.jackson.JacksonStreamFactory" first.
	 * If this class is not found, try "de.odysseus.staxon.json.io.gson.GsonStreamFactory"</li>
	 * </ol>
	 * </p>
	 * @return An instance of JsonStreamFactory.
	 * @throws FactoryConfigurationError
	 *             if a factory class cannot be found or instantiation fails.
	 */
	public static JsonStreamFactory newFactory() throws FactoryConfigurationError {
		ClassLoader classLoader;
		try {
			classLoader = Thread.currentThread().getContextClassLoader();
		} catch (SecurityException e) {
			classLoader = JsonStreamFactory.class.getClassLoader();
		}

		String className = null;

		String serviceId = "META-INF/services/" + JsonStreamFactory.class.getName();
		InputStream input = classLoader.getResourceAsStream(serviceId);
		try {
			if (input != null) {
				className = new BufferedReader(new InputStreamReader(input, "UTF-8")).readLine();
			}
		} catch (IOException e) {
			// do nothing
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception io) {
					// do nothing
				} finally {
					input = null;
				}
			}
		}

		if (className == null || className.trim().length() == 0) {
			try {
				String home = System.getProperty("java.home");
				if (home != null) {
					String path = home + File.separator + "lib" + File.separator + "staxon.properties";
					File file = new File(path);
					if (file.exists()) {
						input = new FileInputStream(file);
						Properties props = new Properties();
						props.load(input);
						className = props.getProperty(JsonStreamFactory.class.getName());
					}
				}
			} catch (IOException e) {
				// do nothing
			} catch (SecurityException e) {
				// do nothing
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException io) {
						// do nothing
					} finally {
						input = null;
					}
				}
			}
		}

		if (className == null || className.trim().length() == 0) {
			try {
				className = System.getProperty(JsonStreamFactory.class.getName());
			} catch (Exception se) {
				// do nothing
			}
		}
		
		Class<?> clazz = null;

		if (className == null || className.trim().length() == 0) {
			className = "de.odysseus.staxon.json.io.jackson.JacksonStreamFactory";
			try {
				clazz = classLoader.loadClass(className);
			} catch (ClassNotFoundException e) {
				className = "de.odysseus.staxon.json.io.gson.GsonStreamFactory";
				try {
					clazz = classLoader.loadClass(className);
				} catch (ClassNotFoundException e2) {
					throw new FactoryConfigurationError("Could not load default stream factory class");
				}
			}
		} else {
			try {
				clazz = classLoader.loadClass(className.trim());
			} catch (ClassNotFoundException e) {
				throw new FactoryConfigurationError("Could not load stream factory class " + className, e);
			}
		}

		try {
			return (JsonStreamFactory) clazz.newInstance();
		} catch (Exception e) {
			throw new FactoryConfigurationError("Could not create stream factory instance", e);
		} catch (Error e) {
			throw new FactoryConfigurationError("Error creating stream factory instance: " + e);
		}
	}

	public abstract JsonStreamSource createJsonStreamSource(InputStream input) throws IOException;
	public abstract JsonStreamSource createJsonStreamSource(Reader reader) throws IOException;
		
	public abstract JsonStreamTarget createJsonStreamTarget(OutputStream output, boolean pretty) throws IOException;
	public abstract JsonStreamTarget createJsonStreamTarget(Writer writer, boolean pretty) throws IOException;
}
