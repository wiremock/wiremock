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
package com.tomakehurst.wiremock.verification;

import java.util.Set;

import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.Request;

public class LoggedRequest implements Request {
	
	private String url;
	private RequestMethod method;
	private HttpHeaders headers = new HttpHeaders();
	private String body;
	
	public static LoggedRequest createFrom(Request request) {
		LoggedRequest loggedRequest = new LoggedRequest();
		loggedRequest.url = request.getUrl();
		loggedRequest.method = request.getMethod();
		loggedRequest.body = request.getBodyAsString();
		for (String key: request.getAllHeaderKeys()) {
			loggedRequest.headers.put(key, request.getHeader(key));
		}
		
		return loggedRequest;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public RequestMethod getMethod() {
		return method;
	}

	@Override
	public String getHeader(String key) {
		return headers.get(key);
	}

	@Override
	public boolean containsHeader(String key) {
		return headers.containsKey(key);
	}

	@Override
	public String getBodyAsString() {
		return body;
	}

	@Override
	public Set<String> getAllHeaderKeys() {
		return headers.keySet();
	}

}
