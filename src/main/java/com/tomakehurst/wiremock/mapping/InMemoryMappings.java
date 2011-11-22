package com.tomakehurst.wiremock.mapping;

import static com.google.common.collect.Iterables.find;

import java.util.concurrent.CopyOnWriteArrayList;


public class InMemoryMappings implements Mappings {
	
	private CopyOnWriteArrayList<RequestResponseMapping> requestResponseMappings = new CopyOnWriteArrayList<RequestResponseMapping>();
	
	@Override
	public Response getFor(Request request) {
		RequestResponseMapping matchingMapping = find(requestResponseMappings,
				RequestResponseMapping.Matcher.forRequest(request),
				RequestResponseMapping.notConfigured());
		return matchingMapping.getResponse();
	}
	
	@Override
	public void addMapping(RequestResponseMapping mapping) {
		requestResponseMappings.add(0, mapping);
	}

	@Override
	public void reset() {
		requestResponseMappings.clear();
	}

}
