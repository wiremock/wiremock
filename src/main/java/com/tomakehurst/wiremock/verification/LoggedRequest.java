package com.tomakehurst.wiremock.verification;

import java.util.Set;

import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.Request;

public class LoggedRequest implements Request {
	
	private String url;
	private RequestMethod method;
	private HttpHeaders headers = new HttpHeaders();
	private String body;
	
	public static LoggedRequest createFrom(Request request) {
		LoggedRequest loggedRequest = new LoggedRequest();
		loggedRequest.url = request.getUrl();
		loggedRequest.method = request.getMethod();
		loggedRequest.body = request.getBodyAsString();
		for (String key: request.getAllHeaderKeys()) {
			loggedRequest.headers.put(key, request.getHeader(key));
		}
		
		return loggedRequest;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public RequestMethod getMethod() {
		return method;
	}

	@Override
	public String getHeader(String key) {
		return headers.get(key);
	}

	@Override
	public boolean containsHeader(String key) {
		return headers.containsKey(key);
	}

	@Override
	public String getBodyAsString() {
		return body;
	}

	@Override
	public Set<String> getAllHeaderKeys() {
		return headers.keySet();
	}

}
