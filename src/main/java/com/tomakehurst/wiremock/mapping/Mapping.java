package com.tomakehurst.wiremock.mapping;

import com.tomakehurst.wiremock.http.RequestMethod;

public class Mapping {

	private RequestMethod method;
	private String uriPattern;
	private Response response;
	
	public RequestMethod getMethod() {
		return method;
	}
	
	public void setMethod(RequestMethod method) {
		this.method = method;
	}
	
	public String getUriPattern() {
		return uriPattern;
	}
	
	public void setUriPattern(String uriPattern) {
		this.uriPattern = uriPattern;
	}
	
	public Response getResponse() {
		return response;
	}
	
	public void setResponse(Response response) {
		this.response = response;
	}

	
}
