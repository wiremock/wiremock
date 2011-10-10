package com.tomakehurst.wiremock.http;

public enum MimeType {
	
	JSON("application/json"),
	XML("text/xml"),
	PLAIN("text/plain");

	private String mimeString;
	
	private MimeType(String mimeString) {
		this.mimeString = mimeString;
	}
	
	public String toString() {
		return mimeString;
	}
}
