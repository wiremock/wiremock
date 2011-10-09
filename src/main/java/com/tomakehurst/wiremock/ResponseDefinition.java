package com.tomakehurst.wiremock;

public class ResponseDefinition {

	private int statusCode;
	private String bodyContent;
	
	public ResponseDefinition(int statusCode, String bodyContent) {
		this.statusCode = statusCode;
		this.bodyContent = bodyContent;
	}
	
	public static ResponseDefinition notFound() {
		return new ResponseDefinition(404, "");
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	
	public String getBodyContent() {
		return bodyContent;
	}
	
	
}
