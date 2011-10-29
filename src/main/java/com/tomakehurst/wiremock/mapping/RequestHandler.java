package com.tomakehurst.wiremock.mapping;

public interface RequestHandler {
	
	public static final String CONTEXT_KEY = "RequestHandler";

	Response handle(Request request);
	void addRequestListener(RequestListener requestListener);
}