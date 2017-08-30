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

import com.github.tomakehurst.wiremock.common.KeyStoreSettings;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.google.common.collect.ImmutableList;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.ByteArrayEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import static com.github.tomakehurst.wiremock.common.HttpClientUtils.getEntityAsByteArrayAndCloseStream;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PATCH;
import static com.github.tomakehurst.wiremock.http.Response.response;

public class ProxyResponseRenderer implements ResponseRenderer {

    private static final int MINUTES = 1000 * 60;
    private static final String TRANSFER_ENCODING = "transfer-encoding";
    private static final String CONTENT_ENCODING = "content-encoding";
    private static final String CONTENT_LENGTH = "content-length";
    private static final String HOST_HEADER = "host";

    private final HttpClient client;
    private final boolean preserveHostHeader;
    private final String hostHeaderValue;
	
	public ProxyResponseRenderer(ProxySettings proxySettings, KeyStoreSettings trustStoreSettings, boolean preserveHostHeader, String hostHeaderValue) {
		client = HttpClientFactory.createClient(1000, 5 * MINUTES, proxySettings, trustStoreSettings);

        this.preserveHostHeader = preserveHostHeader;
        this.hostHeaderValue = hostHeaderValue;
	}

    public ProxyResponseRenderer() {
        this(ProxySettings.NO_PROXY, KeyStoreSettings.NO_STORE, false, null);
    }

	@Override
	public Response render(ResponseDefinition responseDefinition) {
		HttpUriRequest httpRequest = getHttpRequestFor(responseDefinition);
        addRequestHeaders(httpRequest, responseDefinition);

		try {
			addBodyIfPostPutOrPatch(httpRequest, responseDefinition);
			HttpResponse httpResponse = client.execute(httpRequest);

            return response()
                    .status(httpResponse.getStatusLine().getStatusCode())
                    .headers(headersFrom(httpResponse, responseDefinition))
                    .body(getEntityAsByteArrayAndCloseStream(httpResponse))
                    .fromProxy(true)
                    .build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

    private HttpHeaders headersFrom(HttpResponse httpResponse, ResponseDefinition responseDefinition) {
	    List<HttpHeader> httpHeaders = new LinkedList<HttpHeader>();
	    for (Header header : httpResponse.getAllHeaders()) {
		    httpHeaders.add(new HttpHeader(header.getName(), header.getValue()));
	    }

        if (responseDefinition.getHeaders() != null) {
            httpHeaders.addAll(responseDefinition.getHeaders().all());
        }
	    
	    return new HttpHeaders(httpHeaders);
    }

    public static HttpUriRequest getHttpRequestFor(ResponseDefinition response) {
		final RequestMethod method = response.getOriginalRequest().getMethod();
		String url = response.getProxyUrl();
		if (url.endsWith("/")) {
		   url = url.substring(0, url.length() - 1);
		}
		return HttpClientFactory.getHttpRequestFor(method, url);
	}
	
	private void addRequestHeaders(HttpRequest httpRequest, ResponseDefinition response) {
		Request originalRequest = response.getOriginalRequest(); 
		for (String key: originalRequest.getAllHeaderKeys()) {
			if (headerShouldBeTransferred(key)) {
                if (!HOST_HEADER.equalsIgnoreCase(key) || preserveHostHeader) {
					List<String> values = originalRequest.header(key).values();
					for (String value: values) {
						httpRequest.addHeader(key, value);
					}
                } else {
                    if (hostHeaderValue != null) {
                        httpRequest.addHeader(key, hostHeaderValue);
                    } else if (response.getProxyBaseUrl() != null) {
                        httpRequest.addHeader(key, URI.create(response.getProxyBaseUrl()).getAuthority());
                    }
                }
			}
		}
				
		if (response.getAdditionalProxyRequestHeaders() != null) {
			for (String key: response.getAdditionalProxyRequestHeaders().keys()) {
				httpRequest.setHeader(key, response.getAdditionalProxyRequestHeaders().getHeader(key).firstValue());
			}			
		}
	}

    private static boolean headerShouldBeTransferred(String key) {
        return !ImmutableList.of(CONTENT_LENGTH, TRANSFER_ENCODING, "connection").contains(key.toLowerCase());
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
            return applyGzipWrapperIfRequired(
                originalRequest,
                new InputStreamEntity(new ByteArrayInputStream(originalRequest.getBody()), -1, contentType)
            );
        }

        return applyGzipWrapperIfRequired(
            originalRequest,
            new ByteArrayEntity(originalRequest.getBody())
        );
    }

    private static HttpEntity applyGzipWrapperIfRequired(Request originalRequest, HttpEntity content) {
        if (originalRequest.containsHeader(CONTENT_ENCODING) &&
            originalRequest.header(CONTENT_ENCODING).firstValue().contains("gzip")) {
            return new GzipCompressingEntity(content);
        }

        return content;
    }

}
