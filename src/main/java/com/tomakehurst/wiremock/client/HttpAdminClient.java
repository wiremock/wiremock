package com.tomakehurst.wiremock.client;

import static com.tomakehurst.wiremock.http.MimeType.JSON;
import static com.tomakehurst.wiremock.mapping.JsonMappingBinder.buildVerificationResultFrom;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.tomakehurst.wiremock.global.GlobalSettings;
import com.tomakehurst.wiremock.mapping.JsonMappingBinder;
import com.tomakehurst.wiremock.mapping.RequestPattern;
import com.tomakehurst.wiremock.verification.VerificationResult;

public class HttpAdminClient implements AdminClient {
	
	private static final String ADMIN_URL_PREFIX = "http://%s:%d/__admin";
	private static final String LOCAL_WIREMOCK_NEW_RESPONSE_URL = ADMIN_URL_PREFIX + "/mappings/new";
	private static final String LOCAL_WIREMOCK_RESET_URL = ADMIN_URL_PREFIX + "/reset";
	private static final String LOCAL_WIREMOCK_COUNT_REQUESTS_URL = ADMIN_URL_PREFIX + "/requests/count";
	private static final String WIREMOCK_GLOBAL_SETTINGS_URL = ADMIN_URL_PREFIX + "/settings";
	
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
		int status = postEmptyBodyAndReturnStatus(resetUrl());
		assertStatusOk(status);
	}

	private void assertStatusOk(int status) {
		if (status != HTTP_OK) {
			throw new RuntimeException("Returned status code was " + status);
		}
	}
	
	@Override
	public int getRequestsMatching(RequestPattern requestPattern) {
		String json = JsonMappingBinder.write(requestPattern);
		String body = postJsonAssertOkAndReturnBody(requestsCountUrl(), json, HTTP_OK);
		VerificationResult verificationResult = buildVerificationResultFrom(body);
		return verificationResult.getCount();
	}
	
	@Override
	public void updateGlobalSettings(GlobalSettings settings) {
		String json = JsonMappingBinder.write(settings);
		postJsonAssertOkAndReturnBody(globalSettingsUrl(), json, HTTP_OK);
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
	
	private String postJsonAssertOkAndReturnBody(String url, String json, int expectedStatus) {
		PostMethod post = new PostMethod(url);
		try {
			if (json != null) {
				post.setRequestEntity(new StringRequestEntity(json, JSON.toString(), "utf-8"));
			}
			new HttpClient().executeMethod(post);
			return post.getResponseBodyAsString();
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
	
	private String resetUrl() {
		return String.format(LOCAL_WIREMOCK_RESET_URL, host, port);
	}
	
	private String requestsCountUrl() {
		return String.format(LOCAL_WIREMOCK_COUNT_REQUESTS_URL, host, port);
	}

	private String globalSettingsUrl() {
		return String.format(WIREMOCK_GLOBAL_SETTINGS_URL, host, port);
	}
}
