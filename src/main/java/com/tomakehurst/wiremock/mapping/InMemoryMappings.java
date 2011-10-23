package com.tomakehurst.wiremock.mapping;

import static com.tomakehurst.wiremock.mapping.Response.notFound;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


public class InMemoryMappings implements Mappings {
	
	private Set<RequestResponseMapping> requestResponseMappings = new CopyOnWriteArraySet<RequestResponseMapping>();
	
	@Override
	public Response getFor(Request request) {
		for (RequestResponseMapping mapping: requestResponseMappings) {
			if (mapping.getRequest().isMatchedBy(request)) {
				return mapping.getResponse();
			}
		}
		
		return notFound();
	}

	@Override
	public void addMapping(RequestResponseMapping mapping) {
		requestResponseMappings.add(mapping);
	}

	@Override
	public void reset() {
		requestResponseMappings.clear();
	}

}
