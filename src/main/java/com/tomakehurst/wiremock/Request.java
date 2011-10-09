package com.tomakehurst.wiremock;


public class Request {

	private String uri;
	private RequestMethod method;
	
	public Request(String uri, RequestMethod method) {
		this.uri = uri;
		this.method = method;
	}
	
	
}
