package com.tomakehurst.wiremock.mapping;


public interface Mappings {

	ResponseDefinition getFor(Request request);
	void addMapping(RequestResponseMapping mapping);
	void reset();
}
