package com.tomakehurst.wiremock.client;

import static com.tomakehurst.wiremock.http.MimeType.JSON;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class HttpAdminClient implements AdminClient {
	
	private static final String LOCAL_WIREMOCK_NEW_RESPONSE_URL = "http://%s:%d/__admin/mappings/new";
	private static final String LOCAL_WIREMOCK_RESET_MAPPINGS_URL = "http://%s:%d/__admin/mappings/reset";
	
	private String host;
	private int port;
	
	public HttpAdminClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public void addResponse(String responseSpecJson) {
		int status = postJsonAndReturnStatus(newMappingUrl(), responseSpecJson);
		if (status != HTTP_CREATED) {
			throw new RuntimeException("Returned status code was " + status);
		}
	}
	
	@Override
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

	private String newMappingUrl() {
		return String.format(LOCAL_WIREMOCK_NEW_RESPONSE_URL, host, port);
	}
	
	private String resetMappingsUrl() {
		return String.format(LOCAL_WIREMOCK_RESET_MAPPINGS_URL, host, port);
	}

}
