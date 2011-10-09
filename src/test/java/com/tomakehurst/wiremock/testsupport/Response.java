package com.tomakehurst.wiremock.testsupport;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;

public class Response {
	
	private HttpMethod httpMethod;
	
	public Response(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public int getStatusCode() {
		return httpMethod.getStatusCode();
	}
	
	public String getBodyAsString() {
		try {
			return httpMethod.getResponseBodyAsString();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	public Map<String, String> getHeaders() {
		Header[] headers = httpMethod.getResponseHeaders();
		Map<String, String> headerMap = new HashMap<String, String>();
		for (Header header: headers) {
			headerMap.put(header.getName(), header.getValue());
		}
		
		return headerMap;
	}

}
