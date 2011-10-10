package com.tomakehurst.wiremock.mapping;

import com.tomakehurst.wiremock.http.RequestMethod;


public class RequestPattern {

	private final String uriExpression;
	private final RequestMethod method;
	
	
	public RequestPattern(RequestMethod method, String uriExpression) {
		this.uriExpression = uriExpression;
		this.method = method;
	}

	public boolean isMatchedBy(Request request) {
		return (request.getMethod() == method && request.getUri().equals(uriExpression));
	}
}
