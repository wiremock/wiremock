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

import static com.github.tomakehurst.wiremock.client.HttpClientUtils.getEntityAsByteArrayAndCloseStream;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.mapping.Request;
import com.github.tomakehurst.wiremock.mapping.Response;
import com.github.tomakehurst.wiremock.mapping.ResponseDefinition;
import com.google.common.base.Optional;

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
			Response response = new Response(httpResponse.getStatusLine().getStatusCode());
			response.setFromProxy(true);
			for (Header header: httpResponse.getAllHeaders()) {
				response.addHeader(header.getName(), header.getValue());
			}
			
			response.setBody(getEntityAsByteArrayAndCloseStream(httpResponse));
			return response;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
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
			if (!key.equalsIgnoreCase("Content-Length")) {
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
