package com.tomakehurst.wiremock.mapping;

public interface RequestHandler {

	Response handle(Request request);
	void addRequestListener(RequestListener requestListener);
}