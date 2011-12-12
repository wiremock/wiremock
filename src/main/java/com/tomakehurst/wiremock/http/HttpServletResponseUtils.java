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
package com.tomakehurst.wiremock.http;

import java.lang.reflect.Field;
import java.net.Socket;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;

public class HttpServletResponseUtils {

	public static Socket getUnderlyingSocketFrom(HttpServletResponse httpServletResponse) {
		HttpConnection httpConnection = getPrivateField(httpServletResponse, "_connection");
		Object channelEndPoint = httpConnection.getEndPoint();
		return getPrivateField(channelEndPoint, "_socket");
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
	
	
}
