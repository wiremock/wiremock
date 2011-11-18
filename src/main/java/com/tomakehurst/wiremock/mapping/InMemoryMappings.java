package com.tomakehurst.wiremock.mapping;

import java.util.concurrent.CopyOnWriteArrayList;


public class InMemoryMappings implements Mappings {
	
	private CopyOnWriteArrayList<RequestResponseMapping> requestResponseMappings = new CopyOnWriteArrayList<RequestResponseMapping>();
	
	@Override
	public Response getFor(Request request) {
		for (RequestResponseMapping mapping: requestResponseMappings) {
			if (mapping.getRequest().isMatchedBy(request)) {
				return mapping.getResponse();
			}
		}
		
		return Response.notConfigured();
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
