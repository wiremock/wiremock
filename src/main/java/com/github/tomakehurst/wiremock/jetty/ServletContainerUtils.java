/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.jetty;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.net.URI;

public class ServletContainerUtils {

	public static boolean isBrowserProxyRequest(HttpServletRequest request) {
		if (!hasField(request, "_uri")) {
			return false;
		}
		
		String uriString = getPrivateField(request, "_uri").toString();
		URI uri = URI.create(uriString);
		return uri.isAbsolute();
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getPrivateField(Object obj, String name) {
		try {
			Field field = obj.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return (T) field.get(obj);
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean hasField(Object obj, String name) {
		try {
			Field field = obj.getClass().getDeclaredField(name);
			return field != null;
		} catch (RuntimeException re) {
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
}
