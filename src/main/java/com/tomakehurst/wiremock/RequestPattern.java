package com.tomakehurst.wiremock;


public class RequestPattern {

	private String uriExpression;
	private RequestMethod method;

	public RequestPattern(String uriExpression) {
		this.uriExpression = uriExpression;
	}

	public String getExpression() {
		return uriExpression;
	}
	
	public boolean isMatchedBy(String uri) {
		return (uri.equals(uriExpression));
	}
}
