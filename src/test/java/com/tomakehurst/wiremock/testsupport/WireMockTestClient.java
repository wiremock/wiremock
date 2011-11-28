package com.tomakehurst.wiremock.testsupport;

import static com.tomakehurst.wiremock.http.MimeType.JSON;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class WireMockTestClient {

	private static final String LOCAL_WIREMOCK_ROOT = "http://localhost:%d%s";
	private static final String LOCAL_WIREMOCK_NEW_RESPONSE_URL = "http://localhost:%d/__admin/mappings/new";
	private static final String LOCAL_WIREMOCK_RESET_URL = "http://localhost:%d/__admin/reset";
	
	private int port;
	
	public WireMockTestClient(int port) {
		this.port = port;
	}
	
	public WireMockTestClient() {
		this(8080);
	}
	
	private String mockServiceUrlFor(String path) {
		return String.format(LOCAL_WIREMOCK_ROOT, port, path);
	}
	
	private String newMappingUrl() {
		return String.format(LOCAL_WIREMOCK_NEW_RESPONSE_URL, port);
	}
	
	private String resetUrl() {
		return String.format(LOCAL_WIREMOCK_RESET_URL, port);
	}

	public WireMockResponse get(String url, HttpHeader... headers) {
		HttpUriRequest httpRequest = new HttpGet(mockServiceUrlFor(url));
		return executeMethodAndCovertExceptions(httpRequest, headers);
	}
	
	public WireMockResponse put(String url, HttpHeader... headers) {
		HttpUriRequest httpRequest = new HttpPut(mockServiceUrlFor(url));
		return executeMethodAndCovertExceptions(httpRequest, headers);
	}
	
	public WireMockResponse putWithBody(String url, String body, String contentType, HttpHeader... headers) {
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

	private WireMockResponse executeMethodAndCovertExceptions(HttpUriRequest httpRequest, HttpHeader... headers) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpParams params = new BasicHttpParams();
		params.setParameter("http.protocol.handle-redirects", false);
		client.setParams(params);
		try {
			for (HttpHeader header: headers) {
				httpRequest.addHeader(header.getName(), header.getValue());
			}
			HttpResponse httpResponse = client.execute(httpRequest);
			return new WireMockResponse(httpResponse);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

}
