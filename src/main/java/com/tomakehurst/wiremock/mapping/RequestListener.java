package com.tomakehurst.wiremock.mapping;

public interface RequestListener {

	void requestReceived(Request request);
}
