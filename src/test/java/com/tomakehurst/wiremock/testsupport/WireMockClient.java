package com.tomakehurst.wiremock.testsupport;

import static com.tomakehurst.wiremock.http.MimeType.JSON;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class WireMockClient {

	private static final String LOCAL_WIREMOCK_ROOT = "http://localhost:%d%s";
	private static final String LOCAL_WIREMOCK_NEW_RESPONSE_URL = "http://localhost:%d/__admin/mappings/new";
	private static final String LOCAL_WIREMOCK_RESET_MAPPINGS_URL = "http://localhost:%d/__admin/mappings/reset";
	
	private int port;
	
	public WireMockClient(int port) {
		this.port = port;
	}
	
	public WireMockClient() {
		this(8080);
	}
	
	private String mockServiceUrlFor(String path) {
		return String.format(LOCAL_WIREMOCK_ROOT, port, path);
	}
	
	private String newMappingUrl() {
		return String.format(LOCAL_WIREMOCK_NEW_RESPONSE_URL, port);
	}
	
	private String resetMappingsUrl() {
		return String.format(LOCAL_WIREMOCK_RESET_MAPPINGS_URL, port);
	}

	public WireMockResponse get(String url, HttpHeader... headers) {
		HttpMethod httpMethod = new GetMethod(mockServiceUrlFor(url));
		return executeMethodAndCovertExceptions(httpMethod, headers);
	}
	
	public WireMockResponse put(String url, HttpHeader... headers) {
		HttpMethod httpMethod = new PutMethod(mockServiceUrlFor(url));
		return executeMethodAndCovertExceptions(httpMethod, headers);
	}

	public void addResponse(String responseSpecJson) {
		int status = postJsonAndReturnStatus(newMappingUrl(), responseSpecJson);
		if (status != HTTP_CREATED) {
			throw new RuntimeException("Returned status code was " + status);
		}
	}
	
	public void resetMappings() {
		int status = postEmptyBodyAndReturnStatus(resetMappingsUrl());
		if (status != HTTP_OK) {
			throw new RuntimeException("Returned status code was " + status);
		}
	}

	private int postJsonAndReturnStatus(String url, String json) {
		PostMethod post = new PostMethod(url);
		try {
			if (json != null) {
				post.setRequestEntity(new StringRequestEntity(json, JSON.toString(), "utf-8"));
			}
			new HttpClient().executeMethod(post);
			return post.getStatusCode();
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private int postEmptyBodyAndReturnStatus(String url) {
		return postJsonAndReturnStatus(url, null);
	}

	private WireMockResponse executeMethodAndCovertExceptions(HttpMethod httpMethod, HttpHeader... headers) {
		HttpClient client = new HttpClient();
		try {
			for (HttpHeader header: headers) {
				httpMethod.addRequestHeader(header.getName(), header.getValue());
			}
			client.executeMethod(httpMethod);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

		return new WireMockResponse(httpMethod);
	}

}
