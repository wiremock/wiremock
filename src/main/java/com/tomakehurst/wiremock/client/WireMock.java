package com.tomakehurst.wiremock.client;

import static com.tomakehurst.wiremock.http.MimeType.JSON;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.JsonMappingBinder;
import com.tomakehurst.wiremock.mapping.RequestResponseMapping;

public class WireMock {
	
	private static final int DEFAULT_PORT = 8080;
	private static final String DEFAULT_HOST = "localhost";
	private static final String LOCAL_WIREMOCK_NEW_RESPONSE_URL = "http://%s:%d/__admin/mappings/new";
	private static final String LOCAL_WIREMOCK_RESET_MAPPINGS_URL = "http://%s:%d/__admin/mappings/reset";

	private static String staticHost = DEFAULT_HOST;
	private static int staticPort = DEFAULT_PORT;
	
	private String host = DEFAULT_HOST;
	private int port = DEFAULT_PORT;
	
	private static WireMock defaultInstance;
	
	public WireMock(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public WireMock() {
	}
	
	public static void givenThat(MappingBuilder mappingBuilder) {
		initDefaultInstanceIfNull();
		defaultInstance.register(mappingBuilder);
	}
	
	private static void initDefaultInstanceIfNull() {
		if (defaultInstance == null) {
			defaultInstance = new WireMock(staticHost, staticPort);
		}
	}

	public void register(MappingBuilder mappingBuilder) {
		RequestResponseMapping mapping = mappingBuilder.build();
		String json = JsonMappingBinder.buildJsonStringFor(mapping);
		addResponse(json);
	}
	
	public static UrlMatchingStrategy urlEqualTo(String url) {
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrl(url);
		return urlStrategy;
	}
	
	public static MappingBuilder get(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.GET, urlMatchingStrategy);
	}
	
	public static ResponseDefinitionBuilder aResponse() {
		return new ResponseDefinitionBuilder();
	}
	
	private void addResponse(String responseSpecJson) {
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

	private String newMappingUrl() {
		return String.format(LOCAL_WIREMOCK_NEW_RESPONSE_URL, host, port);
	}
	
	private String resetMappingsUrl() {
		return String.format(LOCAL_WIREMOCK_RESET_MAPPINGS_URL, host, port);
	}

	public static void setStaticHost(String staticHost) {
		WireMock.staticHost = staticHost;
	}

	public static void setStaticPort(int staticPort) {
		WireMock.staticPort = staticPort;
	}
}
