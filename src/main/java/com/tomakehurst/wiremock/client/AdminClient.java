package com.tomakehurst.wiremock.client;

import com.tomakehurst.wiremock.mapping.RequestPattern;

public interface AdminClient {

	void addResponse(String responseSpecJson);
	void resetMappings();
	int getRequestsMatching(RequestPattern requestPattern);
}
