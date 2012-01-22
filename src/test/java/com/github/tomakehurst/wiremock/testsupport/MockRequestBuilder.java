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

import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.mapping.Request;

public class MockRequestBuilder {

	private Mockery context;
	private String url = "/";
	private RequestMethod method = GET;
	private HttpHeaders headers = new HttpHeaders();
	private String body = "";
	
	public MockRequestBuilder(Mockery context) {
		this.context = context;
	}
	
	public static MockRequestBuilder aRequest(Mockery context) {
		return new MockRequestBuilder(context);
	}

	public MockRequestBuilder withUrl(String url) {
		this.url = url;
		return this;
	}

	public MockRequestBuilder withMethod(RequestMethod method) {
		this.method = method;
		return this;
	}

	public MockRequestBuilder withHeader(String key, String value) {
		headers.put(key, value);
		return this;
	}
	
	public MockRequestBuilder withBody(String body) {
		this.body = body;
		return this;
	}
	
	public Request build() {
		final Request request = context.mock(Request.class);
		context.checking(new Expectations() {{
			allowing(request).getUrl(); will(returnValue(url));
			allowing(request).getMethod(); will(returnValue(method));
			for (Map.Entry<String, String> header: headers.entrySet()) {
				allowing(request).containsHeader(header.getKey()); will(returnValue(true));
				allowing(request).getHeader(header.getKey()); will(returnValue(header.getValue()));
			}
			allowing(request).getAllHeaderKeys(); will(returnValue(newLinkedHashSet(headers.keySet())));
			allowing(request).containsHeader(with(any(String.class))); will(returnValue(false));
			allowing(request).getBodyAsString(); will(returnValue(body));
		}});
		
		return request;
	}
}
