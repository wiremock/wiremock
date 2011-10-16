package com.tomakehurst.wiremock.mapping;

import static com.google.common.collect.Sets.newHashSet;
import static com.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.tomakehurst.wiremock.mapping.Response.notFound;

import java.util.Set;


public class InMemoryMappings implements Mappings {
	
	private Set<RequestResponseMapping> requestResponseMappings = newHashSet();
	
	public InMemoryMappings() {
		requestResponseMappings.add(new RequestResponseMapping(
				new RequestPattern(GET, "/canned/resource"),
				new Response(200, "{ \"somekey\": \"My value\" }")));
	}
	
	@Override
	public Response getFor(Request request) {
		for (RequestResponseMapping mapping: requestResponseMappings) {
			if (mapping.getRequestPattern().isMatchedBy(request)) {
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
