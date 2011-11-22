package com.tomakehurst.wiremock.mapping;

import static com.google.common.collect.Iterables.find;

import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.base.Predicate;


public class InMemoryMappings implements Mappings {
	
	private CopyOnWriteArrayList<RequestResponseMapping> requestResponseMappings = new CopyOnWriteArrayList<RequestResponseMapping>();
	
	@Override
	public Response getFor(Request request) {
		RequestResponseMapping matchingMapping = find(requestResponseMappings, mappingMatching(request), RequestResponseMapping.notConfigured());
		return matchingMapping.getResponse();
	}
	
	private Predicate<RequestResponseMapping> mappingMatching(final Request request) {
		return new Predicate<RequestResponseMapping>() {
			public boolean apply(RequestResponseMapping input) {
				return input.getRequest().isMatchedBy(request);
			}
		};
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
