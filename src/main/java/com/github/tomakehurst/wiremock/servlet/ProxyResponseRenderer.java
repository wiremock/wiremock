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

import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.mapping.Request;
import com.github.tomakehurst.wiremock.mapping.Response;
import com.github.tomakehurst.wiremock.mapping.ResponseDefinition;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static com.github.tomakehurst.wiremock.client.HttpClientUtils.getEntityAsByteArrayAndCloseStream;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static com.github.tomakehurst.wiremock.mapping.Response.response;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

public class ProxyResponseRenderer implements ResponseRenderer {
	
	private static final int MINUTES = 1000 * 60;
	
	private final HttpClient client;
	
	public ProxyResponseRenderer() {
		client = HttpClientFactory.createClient(1000, 5 * MINUTES);
	}

	@Override
	public Response render(ResponseDefinition responseDefinition) {
		HttpUriRequest httpRequest = getHttpRequestFor(responseDefinition);
		addRequestHeaders(httpRequest, responseDefinition);
		httpRequest.removeHeaders("Host");
		
		try {
			addBodyIfPostOrPut(httpRequest, responseDefinition);
			HttpResponse httpResponse = client.execute(httpRequest);

            return response()
                    .status(httpResponse.getStatusLine().getStatusCode())
                    .headers(headersFrom(httpResponse))
                    .body(getEntityAsByteArrayAndCloseStream(httpResponse))
                    .fromProxy(true)
                    .build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

    private HttpHeaders headersFrom(HttpResponse httpResponse) {
        return new HttpHeaders(transform(asList(httpResponse.getAllHeaders()), new Function<Header, HttpHeader>() {
            public HttpHeader apply(Header header) {
                return new HttpHeader(header.getName(), header.getValue());
            }
        }));
    }

    private static HttpUriRequest getHttpRequestFor(ResponseDefinition response) {
		RequestMethod method = response.getOriginalRequest().getMethod();
		String url = response.getProxyUrl();
		notifier().info("Proxying: " + method + " " + url);
		
		switch (method) {
		case GET:
			return new HttpGet(url);
		case POST:
			return new HttpPost(url);
		case PUT:
			return new HttpPut(url);
		case DELETE:
			return new HttpDelete(url);
		case HEAD:
			return new HttpHead(url);
		case OPTIONS:
			return new HttpOptions(url);
		case TRACE:
			return new HttpTrace(url);
		default:
			throw new RuntimeException("Cannot create HttpMethod for " + method);
		}
	}
	
	private static void addRequestHeaders(HttpRequest httpRequest, ResponseDefinition response) {
		Request originalRequest = response.getOriginalRequest(); 
		for (String key: originalRequest.getAllHeaderKeys()) {
			if (!key.equals("Content-Length")) {
				String value = originalRequest.getHeader(key);
				httpRequest.addHeader(key, value);
			}
		}
	}
	
	private static void addBodyIfPostOrPut(HttpRequest httpRequest, ResponseDefinition response) throws UnsupportedEncodingException {
		Request originalRequest = response.getOriginalRequest();
		if (originalRequest.getMethod() == POST || originalRequest.getMethod() == PUT) {
			HttpEntityEnclosingRequest requestWithEntity = (HttpEntityEnclosingRequest) httpRequest;
			Optional<ContentTypeHeader> optionalContentType = ContentTypeHeader.getFrom(originalRequest);
			String body = originalRequest.getBodyAsString();
			
			if (optionalContentType.isPresent()) {
				ContentTypeHeader header = optionalContentType.get();
				requestWithEntity.setEntity(new StringEntity(body,
						header.mimeTypePart(),
						header.encodingPart().isPresent() ? header.encodingPart().get() : "utf-8"));
			} else {
				requestWithEntity.setEntity(new StringEntity(body,
						"text/plain",
						"utf-8"));
			}
		}
	}

}
