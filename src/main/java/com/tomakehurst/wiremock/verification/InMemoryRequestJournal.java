/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tomakehurst.wiremock.verification;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.base.Predicate;
import com.tomakehurst.wiremock.mapping.Request;
import com.tomakehurst.wiremock.mapping.RequestListener;
import com.tomakehurst.wiremock.mapping.RequestPattern;
import com.tomakehurst.wiremock.mapping.Response;

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
	public void requestReceived(Request request, Response response) {
		requests.add(LoggedRequest.createFrom(request));
	}

	@Override
	public void reset() {
		requests.clear();
	}

}
