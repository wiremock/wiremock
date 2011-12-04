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
package com.tomakehurst.wiremock.testsupport;

import static com.tomakehurst.wiremock.http.RequestMethod.GET;

import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.RequestPattern;
import com.tomakehurst.wiremock.mapping.RequestResponseMapping;
import com.tomakehurst.wiremock.mapping.ResponseDefinition;

public class RequestResponseMappingBuilder {

	private String url = "/";
	private RequestMethod method = GET;
	private int responseStatus = 200;
	private String responseBody = "";
	private HttpHeaders headers = new HttpHeaders();
	
	public static RequestResponseMappingBuilder aMapping() {
		return new RequestResponseMappingBuilder();
	}

	public RequestResponseMappingBuilder withUrl(String url) {
		this.url = url;
		return this;
	}

	public RequestResponseMappingBuilder withMethod(RequestMethod method) {
		this.method = method;
		return this;
	}

	public RequestResponseMappingBuilder withResponseStatus(int responseStatus) {
		this.responseStatus = responseStatus;
		return this;
	}

	public RequestResponseMappingBuilder withResponseBody(String responseBody) {
		this.responseBody = responseBody;
		return this;
	}
	
	public RequestResponseMappingBuilder withHeader(String key, String value) {
		headers.put(key, value);
		return this;
	}
	
	public RequestResponseMapping build() {
		RequestPattern requestPattern = new RequestPattern(method, url);
		ResponseDefinition response = new ResponseDefinition(responseStatus, responseBody);
		response.setHeaders(headers);
		RequestResponseMapping mapping = new RequestResponseMapping(requestPattern, response);
		return mapping;
	}
}
