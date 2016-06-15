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
package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServedStub;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.github.tomakehurst.wiremock.matching.RequestPattern.thatMatch;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.transform;

public class InMemoryRequestJournal implements RequestListener, RequestJournal {

	private final Queue<ServedStub> servedStubs = new ConcurrentLinkedQueue<ServedStub>();

	private final Optional<Integer> maxEntries;

	public InMemoryRequestJournal(Optional<Integer> maxEntries) {
		if (maxEntries.isPresent() && maxEntries.get() < 0) {
			throw new IllegalArgumentException("Maximum number of entries of journal must be greater than zero");
		}
		this.maxEntries = maxEntries;
	}

	@Override
	public int countRequestsMatching(RequestPattern requestPattern) {
		return size(filter(getRequests(), thatMatch(requestPattern)));
	}

	@Override
	public List<LoggedRequest> getRequestsMatching(RequestPattern requestPattern) {
		return ImmutableList.copyOf(filter(getRequests(), thatMatch(requestPattern)));
	}

    private Predicate<Request> matchedBy(final RequestPattern requestPattern) {
		return new Predicate<Request>() {
			public boolean apply(Request input) {
				return requestPattern.isMatchedBy(input, Collections.<String, RequestMatcherExtension>emptyMap());
			}
		};
	}

	@Override
	public void requestReceived(Request request, Response response) {
		servedStubs.add(ServedStub.exactMatch(LoggedRequest.createFrom(request), null));
		removeOldEntries();
	}

	@Override
	public void requestReceived(ServedStub servedStub) {
		servedStubs.add(servedStub);
        removeOldEntries();
	}

    @Override
    public List<ServedStub> getAllServedStubs() {
        return ImmutableList.copyOf(servedStubs);
    }

	@Override
	public void reset() {
		servedStubs.clear();
	}

	private Iterable<LoggedRequest> getRequests() {
		return transform(servedStubs, new Function<ServedStub, LoggedRequest>() {
			public LoggedRequest apply(ServedStub input) {
				return input.getRequest();
			}
		});
	}

	private void removeOldEntries() {
		if (maxEntries.isPresent()) {
			while (servedStubs.size() > maxEntries.get()) {
				servedStubs.poll();
			}
		}
	}

}
