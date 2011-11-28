package com.tomakehurst.wiremock.mapping;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.find;
import static com.tomakehurst.wiremock.mapping.Priority.LOW;

import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.base.Predicate;


public class InMemoryMappings implements Mappings {
	
	private CopyOnWriteArrayList<RequestResponseMapping> normalPriorityMappings = new CopyOnWriteArrayList<RequestResponseMapping>();
	private CopyOnWriteArrayList<RequestResponseMapping> lowPriorityMappings = new CopyOnWriteArrayList<RequestResponseMapping>();
	
	@Override
	public Response getFor(Request request) {
		RequestResponseMapping matchingMapping = find(
				concat(normalPriorityMappings, lowPriorityMappings),
				mappingMatching(request),
				RequestResponseMapping.notConfigured());
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
		if (mapping.priorityIs(LOW)) {
			lowPriorityMappings.add(0, mapping);
		} else {
			normalPriorityMappings.add(0, mapping);
		}
	}

	@Override
	public void reset() {
		normalPriorityMappings.clear();
	}

}
