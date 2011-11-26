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
package de.odysseus.staxon.json.jaxb;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>The <code>JsonXML</code> annotation is used to configure the JSON
 * serialization <em>and</em> deserialization process. It may be placed on a</p>
 * <ul>
 * <li>a model type (e.g. a JAXB-annotated class) to configure serialization and deserialization of that type,</li>
 * <li>a JAX-RS resource method to configure serialization of the result type,</li>
 * <li>a parameter of a JAX-RS resource method to configure deserialiation of the parameter type.</li>
 * </ul>
 * <p>If an annotations is present at a model type and a resource method or parameter, the latter
 * overrides the model type annotation.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER })
public @interface JsonXML {
	/**
	 * <p>JSON documents may have have multiple root properties. However,
	 * XML requires a single root element. This property whether to treat
	 * the root as a "virtual" element, which will be removed from the stream
	 * when writing and added to the stream when reading. The root element
	 * name will be determined from an <code>@XmlRootElement</code> or
	 * <code>@XmlType</code> annotation.</p>
	 * 
	 * <p>The default value is <code>false</code> (i.e. no virtual root).</p>
	 */
	boolean virtualRoot() default false;
	
	/**
	 * <p>Specify array paths. paths are absolute with at least two names,
	 * where names are separated by `'/'` and may be prefixed. The root
	 * element is <em>not</em> included in a multiple path.</p>
	 * <p>E.g. for</p>
	 * <pre>
	 * {
	 *   "alice" : {
	 *     "bob" : [ "edgar", "charlie" ],
	 *     "peter" : null
	 *   }
	 * }
	 * </pre>
	 * <p>we would specify <code>"/bob"</code> as multiple path.</p>
	 */
	String[] multiplePaths() default {};

	/**
	 * <p>Format output for better readability?</p>
	 * 
	 * <p>The default value is <code>false</code>.</p>
	 */
	boolean prettyPrint()  default false;

	/**
	 * <p>Trigger arrays automatically?</p>
	 * 
	 * <p>The default value is <code>false</code>.</p>
	 */
	boolean autoArray() default false;

	/**
	 * <p>Whether to write namespace declarations.</p>
	 * 
	 * <p>The default value is <code>true</code>.</p>
	 */
	boolean namespaceDeclarations() default true;

	/**
	 * <p>Namespace prefix separator.</p>
	 * 
	 * <p>The default value is <code>':'</code>.</p>
	 */
	char namespaceSeparator() default ':';
}