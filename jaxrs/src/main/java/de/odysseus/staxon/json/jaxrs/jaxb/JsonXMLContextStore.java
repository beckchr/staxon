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
package de.odysseus.staxon.json.jaxrs.jaxb;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class JsonXMLContextStore {
	static class CacheKey {
		private final int hashCode;
		private final Class<?>[] classes;

		CacheKey(Class<?>... classes) {
			int classesHashCode = 0;
			for (Class<?> clazz : classes) {
				classesHashCode ^= clazz.hashCode();
			}
			this.hashCode = classesHashCode;
			this.classes = classes;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass() || hashCode() != obj.hashCode()) {
				return false;
			}
			CacheKey other = (CacheKey) obj;
			if (!Arrays.equals(classes, other.classes)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}

	private final ConcurrentHashMap<CacheKey, JAXBContext> cache = new ConcurrentHashMap<JsonXMLContextStore.CacheKey, JAXBContext>();
	private final Providers providers;
	
	public JsonXMLContextStore(Providers providers) {
		this.providers = providers;
	}
	
	protected JAXBContext createContext(Class<?>... types) throws JAXBException {
		return JAXBContext.newInstance(types);
	}

	public JAXBContext getContext(Class<?> type, MediaType mediaType) throws JAXBException {
		CacheKey key = new CacheKey(type);
		JAXBContext result = cache.get(key);
		if (result != null) {
			return result;
		}
		if (providers != null) {
			ContextResolver<JAXBContext> resolver = providers.getContextResolver(JAXBContext.class, mediaType);
			if (resolver != null) {
				result = resolver.getContext(type);
				if (result != null) {
					return cache.putIfAbsent(key, result) == null ? result : cache.get(key);
				}
			}
		}
		result = createContext(type);
		if (result != null) {
			return cache.putIfAbsent(key, result) == null ? result : cache.get(key);
		}
		return null;
	}

//	public JAXBContext getContext(Class<?>... types) throws JAXBException {
//		CacheKey key = new CacheKey(types);
//		JAXBContext result = cache.get(key);
//		if (result != null) {
//			return result;
//		}
//		result = createContext(types);
//		if (result != null) {
//			return cache.putIfAbsent(key, result) == null ? result : cache.get(key);
//		}
//		return null;
//	}
}
