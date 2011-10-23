package com.tomakehurst.wiremock.client;

public interface AdminClient {

	void addResponse(String responseSpecJson);
	void resetMappings();
	
}
