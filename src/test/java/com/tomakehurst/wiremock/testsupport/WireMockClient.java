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

	private static final String LOCAL_WIREMOCK_ROOT = "http://localhost:8080";
	private static final String LOCAL_WIREMOCK_NEW_RESPONSE_URL = "http://localhost:8080/__admin/mappings/new";
	private static final String LOCAL_WIREMOCK_RESET_MAPPINGS_URL = "http://localhost:8080/__admin/mappings/reset";

	public WireMockResponse get(String uri, HttpHeader... headers) {
		HttpMethod httpMethod = new GetMethod(LOCAL_WIREMOCK_ROOT + uri);
		return executeMethodAndCovertExceptions(httpMethod, headers);
	}
	
	public WireMockResponse put(String uri, HttpHeader... headers) {
		HttpMethod httpMethod = new PutMethod(LOCAL_WIREMOCK_ROOT + uri);
		return executeMethodAndCovertExceptions(httpMethod, headers);
	}

	public void addResponse(String responseSpecJson) {
		int status = postJsonAndReturnStatus(LOCAL_WIREMOCK_NEW_RESPONSE_URL, responseSpecJson);
		if (status != HTTP_CREATED) {
			throw new RuntimeException("Returned status code was " + status);
		}
	}
	
	public void resetMappings() {
		int status = postEmptyBodyAndReturnStatus(LOCAL_WIREMOCK_RESET_MAPPINGS_URL);
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
