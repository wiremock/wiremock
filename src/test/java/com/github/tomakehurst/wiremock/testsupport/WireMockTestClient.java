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

import com.github.tomakehurst.wiremock.http.HttpClientFactory;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import static com.github.tomakehurst.wiremock.http.MimeType.JSON;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

public class WireMockTestClient {

	private static final String LOCAL_WIREMOCK_ROOT = "http://%s:%d%s";
	private static final String LOCAL_WIREMOCK_NEW_RESPONSE_URL = "http://%s:%d/__admin/mappings/new";
	private static final String LOCAL_WIREMOCK_RESET_URL = "http://%s:%d/__admin/reset";
	private static final String LOCAL_WIREMOCK_RESET_DEFAULT_MAPPINS_URL = "http://%s:%d/__admin/mappings/reset";

	private int port;
    private String address;
	
	public WireMockTestClient(int port, String address) {
        this.port = port;
        this.address = address;
    }
	
	public WireMockTestClient(int port) {
		this(port, "localhost");
	}
	
	public WireMockTestClient() {
		this(8080);
	}
	
	private String mockServiceUrlFor(String path) {
		return String.format(LOCAL_WIREMOCK_ROOT, address, port, path);
	}
	
	private String newMappingUrl() {
		return String.format(LOCAL_WIREMOCK_NEW_RESPONSE_URL, address, port);
	}
	
	private String resetUrl() {
		return String.format(LOCAL_WIREMOCK_RESET_URL, address, port);
	}

    private String resetDefaultMappingsUrl() {
        return String.format(LOCAL_WIREMOCK_RESET_DEFAULT_MAPPINS_URL, address, port);
    }

	public WireMockResponse get(String url, TestHttpHeader... headers) {
		HttpUriRequest httpRequest = new HttpGet(mockServiceUrlFor(url));
		return executeMethodAndCovertExceptions(httpRequest, headers);
	}
	
	public WireMockResponse getViaProxy(String url) {
		URI targetUri = URI.create(url);
		
		HttpHost proxy = new HttpHost(address, 8080, "http");

        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

            HttpHost target = new HttpHost(targetUri.getHost(), targetUri.getPort(), targetUri.getScheme());
            HttpGet req = new HttpGet(targetUri.getPath());

            System.out.println("executing request to " + target + " via " + proxy);
            HttpResponse httpResponse = httpclient.execute(target, req);
            return new WireMockResponse(httpResponse);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} 
	}
	
	public WireMockResponse put(String url, TestHttpHeader... headers) {
		HttpUriRequest httpRequest = new HttpPut(mockServiceUrlFor(url));
		return executeMethodAndCovertExceptions(httpRequest, headers);
	}
	
	public WireMockResponse putWithBody(String url, String body, String contentType, TestHttpHeader... headers) {
		HttpPut httpPut = new HttpPut(mockServiceUrlFor(url));
		try {
			httpPut.setEntity(new StringEntity(body, contentType, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		return executeMethodAndCovertExceptions(httpPut, headers);
	}
	
	public WireMockResponse postWithBody(String url, String body, String bodyMimeType, String bodyEncoding) {
		HttpPost httpPost = new HttpPost(mockServiceUrlFor(url));
		try {
			httpPost.setEntity(new StringEntity(body, bodyMimeType, bodyEncoding));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		return executeMethodAndCovertExceptions(httpPost);
	}

	public void addResponse(String responseSpecJson) {
		int status = postJsonAndReturnStatus(newMappingUrl(), responseSpecJson);
		if (status != HTTP_CREATED) {
			throw new RuntimeException("Returned status code was " + status);
		}
	}
	
	public void resetMappings() {
		int status = postEmptyBodyAndReturnStatus(resetUrl());
		if (status != HTTP_OK) {
			throw new RuntimeException("Returned status code was " + status);
		}
	}

    public void resetDefaultMappings() {
        int status = postEmptyBodyAndReturnStatus(resetDefaultMappingsUrl());
        if (status != HTTP_OK) {
            throw new RuntimeException("Returned status code was " + status);
        }
    }

    private int postJsonAndReturnStatus(String url, String json) {
		HttpPost post = new HttpPost(url);
		try {
			if (json != null) {
				post.setEntity(new StringEntity(json, JSON.toString(), "utf-8"));
			}
			HttpResponse httpResponse = new DefaultHttpClient().execute(post);
			return httpResponse.getStatusLine().getStatusCode();
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private int postEmptyBodyAndReturnStatus(String url) {
		return postJsonAndReturnStatus(url, null);
	}

	private WireMockResponse executeMethodAndCovertExceptions(HttpUriRequest httpRequest, TestHttpHeader... headers) {
		HttpClient client = HttpClientFactory.createClient();
		HttpParams params = client.getParams();
		params.setParameter("http.protocol.handle-redirects", false);
		
		try {
			for (TestHttpHeader header: headers) {
				httpRequest.addHeader(header.getName(), header.getValue());
			}
			HttpResponse httpResponse = client.execute(httpRequest);
			return new WireMockResponse(httpResponse);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

}
