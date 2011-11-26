package com.tomakehurst.wiremock.verification;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.base.Predicate;
import com.tomakehurst.wiremock.mapping.Request;
import com.tomakehurst.wiremock.mapping.RequestListener;
import com.tomakehurst.wiremock.mapping.RequestPattern;

public class InMemoryRequestJournal implements RequestListener, RequestJournal {
	
	private ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<Request>();

	@Override
	public int countRequestsMatching(RequestPattern requestPattern) {
		return size(filter(requests, matchedBy(requestPattern))); 
	}
	
	private Predicate<Request> matchedBy(final RequestPattern requestPattern) {
		return new Predicate<Request>() {
			public boolean apply(Request input) {
				return requestPattern.isMatchedBy(input);
			}
		};
	}

	@Override
	public void requestReceived(Request request) {
		requests.add(LoggedRequest.createFrom(request));
	}

	@Override
	public void reset() {
		requests.clear();
	}

}
