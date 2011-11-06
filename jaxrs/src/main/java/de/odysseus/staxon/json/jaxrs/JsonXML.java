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
package de.odysseus.staxon.json.jaxrs;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER })
public @interface JsonXML {
	/**
	 * <p>JSON documents may have have multiple root properties. However,
	 * XML requires a single root element. This property takes the name
	 * of a "virtual" root element, which will be removed from the stream
	 * when writing and added to the stream when reading.</p>
	 * 
	 * <p>The default value is <code>""</code> (i.e. no virtual root).</p>
	 */
	String virtualRoot() default "";
	
	/**
	 * <p>Specify array paths. paths are absolute with at least two names,
	 * where names are separated by `'/'` and may be prefixed.</p>
	 * <p>E.g. for</p>
	 * <pre>
	 * {
	 *   "alice" : {
	 *     "bob" : [ "edgar", "charlie" ],
	 *     "peter" : null
	 *   }
	 * }
	 * </pre>
	 * <p>we would specify <code>"/alice/bob"</code> as multiple path.</p>
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