package com.tomakehurst.wiremock;

import static com.google.common.collect.Sets.newHashSet;
import static com.tomakehurst.wiremock.RequestMethod.GET;
import static com.tomakehurst.wiremock.Response.notFound;

import java.util.Set;


public class InMemoryResponses implements Responses {
	
	private Set<RequestResponseMapping> requestResponseMappings = newHashSet();
	
	public InMemoryResponses() {
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

}
