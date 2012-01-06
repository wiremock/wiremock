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
package com.github.tomakehurst.wiremock.servlet;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.common.io.CharStreams;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.mapping.Request;

public class HttpServletRequestAdapter implements Request {
	
	private HttpServletRequest request;
	private String cachedBody;
	
	public HttpServletRequestAdapter(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String getUrl() {
		String url = request.getRequestURI();
		
		if (!isNullOrEmpty(request.getContextPath())) {
			url = url.replace(request.getContextPath(), "");
		}
		
		if (!isNullOrEmpty(request.getQueryString())) {
			url = url + "?" + request.getQueryString();
		}
		
		return url;
	}

	@Override
	public RequestMethod getMethod() {
		return RequestMethod.valueOf(request.getMethod().toUpperCase());
	}

	@Override
	public String getBodyAsString() {
		if (cachedBody == null) {
			try {
				cachedBody = CharStreams.toString(request.getReader());
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		
		return cachedBody;
	}

	@Override
	public String getHeader(String key) {
		return request.getHeader(key);
	}

	@Override
	public boolean containsHeader(String key) {
		return request.getHeader(key) != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getAllHeaderKeys() {
		LinkedHashSet<String> headerKeys = new LinkedHashSet<String>();
		for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
			headerKeys.add(headerNames.nextElement());
		}
		
		return headerKeys;
	}

}
