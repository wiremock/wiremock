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
package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static com.github.tomakehurst.wiremock.common.HttpClientUtils.getEntityAsByteArrayAndCloseStream;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PATCH;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

public class ProxyResponseRenderer implements ResponseRenderer {

    private static final int MINUTES = 1000 * 60;
    private static final String TRANSFER_ENCODING = "transfer-encoding";
    private static final String CONTENT_LENGTH = "content-length";

    private final HttpClient client;
	
	public ProxyResponseRenderer(ProxySettings proxySettings) {
        if (proxySettings != null) {
            client = HttpClientFactory.createClient(1000, 5 * MINUTES, proxySettings);
        } else {
            client = HttpClientFactory.createClient(1000, 5 * MINUTES);
        }
	}

    public ProxyResponseRenderer() {
        this(ProxySettings.NO_PROXY);
    }

	@Override
	public Response render(ResponseDefinition responseDefinition) {
		HttpUriRequest httpRequest = getHttpRequestFor(responseDefinition);
        httpRequest.removeHeaders("Host");
        addRequestHeaders(httpRequest, responseDefinition);

		try {
			addBodyIfPostPutOrPatch(httpRequest, responseDefinition);
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
		case PATCH:
			return new HttpPatch(url);
		default:
			throw new RuntimeException("Cannot create HttpMethod for " + method);
		}
	}
	
	private static void addRequestHeaders(HttpRequest httpRequest, ResponseDefinition response) {
		Request originalRequest = response.getOriginalRequest(); 
		for (String key: originalRequest.getAllHeaderKeys()) {
			if (headerShouldBeTransferred(key)) {
				String value = originalRequest.getHeader(key);
				httpRequest.addHeader(key, value);
			}
		}
	}

    private static boolean headerShouldBeTransferred(String key) {
        return !ImmutableList.of(CONTENT_LENGTH, TRANSFER_ENCODING).contains(key.toLowerCase());
    }

    private static void addBodyIfPostPutOrPatch(HttpRequest httpRequest, ResponseDefinition response) throws UnsupportedEncodingException {
		Request originalRequest = response.getOriginalRequest();
		if (originalRequest.getMethod().isOneOf(PUT, POST, PATCH)) {
			HttpEntityEnclosingRequest requestWithEntity = (HttpEntityEnclosingRequest) httpRequest;
            requestWithEntity.setEntity(buildEntityFrom(originalRequest));
		}
	}

    private static HttpEntity buildEntityFrom(Request originalRequest) {
        ContentTypeHeader contentTypeHeader = originalRequest.contentTypeHeader().or("text/plain");
        ContentType contentType = ContentType.create(contentTypeHeader.mimeTypePart(), contentTypeHeader.encodingPart().or("utf-8"));

        if (originalRequest.containsHeader(TRANSFER_ENCODING) &&
                originalRequest.header(TRANSFER_ENCODING).firstValue().equals("chunked")) {
            return new InputStreamEntity(new ByteArrayInputStream(originalRequest.getBodyAsString().getBytes()), -1, contentType);
        }

        return new StringEntity(originalRequest.getBodyAsString(), contentType);
    }

}
