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
package com.github.tomakehurst.wiremock.testsupport;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.List;

import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.google.common.collect.Lists.newArrayList;

public class RequestResponseMappingBuilder {

	private String url = "/";
	private RequestMethod method = GET;
	private int responseStatus = 200;
	private String responseBody = "";
	private List<HttpHeader> headers = newArrayList();
	
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
		headers.add(new HttpHeader(key, value));
		return this;
	}
	
	public StubMapping build() {
		RequestPattern requestPattern = new RequestPattern(method, url);
		ResponseDefinition response = new ResponseDefinition(responseStatus, responseBody);
		response.setHeaders(new HttpHeaders(headers));
		StubMapping mapping = new StubMapping(requestPattern, response);
		return mapping;
	}
}
