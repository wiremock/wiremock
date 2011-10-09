package com.tomakehurst.wiremock;


public interface ResponseDefinitions {

	ResponseDefinition get(RequestMethod method, String uri);
	
}
