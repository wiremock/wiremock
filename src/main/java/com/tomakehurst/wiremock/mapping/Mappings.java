package com.tomakehurst.wiremock.mapping;


public interface Mappings {

	Response getFor(Request request);
	void addMapping(RequestResponseMapping mapping);
	
}
