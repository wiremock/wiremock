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

import static com.github.tomakehurst.wiremock.client.HttpClientUtils.getEntityAsStringAndCloseStream;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.find;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;

import com.google.common.base.Predicate;

public class WireMockResponse {
	
	private HttpResponse httpResponse;
	private String content;
	
	public WireMockResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
		content = getEntityAsStringAndCloseStream(httpResponse);
	}

	public int statusCode() {
		return httpResponse.getStatusLine().getStatusCode();
	}
	
	public String content() {
		return content;
	}
	
	public String header(final String key) {
		return find(
				copyOf(httpResponse.getAllHeaders()),
				caseInsensitiveKeyMatching(key),
				defaultToHeaderWithNullValue(key))
				.getValue();
	}

	private BasicHeader defaultToHeaderWithNullValue(final String key) {
		return new BasicHeader(key, null);
	}

	private Predicate<Header> caseInsensitiveKeyMatching(final String key) {
		return new Predicate<Header>() {
			public boolean apply(Header header) {
				return header.getName().equalsIgnoreCase(key);
			}
		};
	}
	
	public Map<String, String> headers() {
		Header[] headers = httpResponse.getAllHeaders();
		Map<String, String> headerMap = new HashMap<String, String>();
		for (Header header: headers) {
			headerMap.put(header.getName(), header.getValue());
		}
		
		return headerMap;
	}

}
