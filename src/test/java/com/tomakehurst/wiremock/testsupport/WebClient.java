package com.tomakehurst.wiremock.testsupport;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

public class WebClient {

	public Response get(String url, HttpHeader... headers) {
		HttpMethod httpMethod = new GetMethod(url);
		return executeMethodAndCovertExceptions(httpMethod);
	}
	
	private Response executeMethodAndCovertExceptions(HttpMethod httpMethod) {
		HttpClient client = new HttpClient();
		try {
			client.executeMethod(httpMethod);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		
		return new Response(httpMethod);
	}
}
