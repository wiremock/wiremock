package com.tomakehurst.wiremock.verification;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.tomakehurst.wiremock.mapping.Request;
import com.tomakehurst.wiremock.mapping.RequestListener;
import com.tomakehurst.wiremock.mapping.RequestPattern;

public class InMemoryRequestJournal implements RequestListener, RequestJournal {
	
	private ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<Request>();

	@Override
	public int countRequestsMatching(RequestPattern requestPattern) {
		int count = 0;
		for (Request request: requests) {
			if (requestPattern.isMatchedBy(request)) {
				count++;
			}
		}
		
		return count;
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
