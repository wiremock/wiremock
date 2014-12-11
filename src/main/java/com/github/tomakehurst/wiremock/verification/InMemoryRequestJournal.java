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
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;

public class InMemoryRequestJournal implements RequestListener, RotatingRequestJournal {
	
	private Queue<LoggedRequest> requests = new ConcurrentLinkedQueue<LoggedRequest>();

    private Optional<Integer> maxEntries;

    private Optional<Integer> initialMaxEntries;

    /**
     * @param initialMaxEntries Initial value for the size of the request journal. When the journal is reset the
     *                          maximum number of entries is reset to this value.
     */
    public InMemoryRequestJournal(Optional<Integer> initialMaxEntries) {
        this.initialMaxEntries = initialMaxEntries;
        setMaxEntries(initialMaxEntries);
    }

    @Override
	public int countRequestsMatching(RequestPattern requestPattern) {
		return size(filter(requests, matchedBy(requestPattern))); 
	}

    @Override
    public List<LoggedRequest> getRequestsMatching(RequestPattern requestPattern) {
        return ImmutableList.copyOf(filter(requests, matchedBy(requestPattern)));
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
        removeOldEntries();
	}

    @Override
    public void requestReceived(Request request) {
        requestReceived(request, null);
    }

	@Override
	public void reset() {
		requests.clear();
        setMaxEntries(initialMaxEntries);
	}

    private void removeOldEntries() {
        if(maxEntries.isPresent()) {
            while (requests.size() > maxEntries.get()) {
                // Remove entries from the journal until we reach the desired number. In general this will only be
                // maximum one entry if we add one entry in requestReceived.
                requests.poll();
            }
        }
    }

    @Override
    public void setMaxEntries(Optional<Integer> maxEntries) {
        if(maxEntries.isPresent() && maxEntries.get() <0) {
            throw new IllegalArgumentException("Maximum number of entries of journal must be greater than zero");
        }
        this.maxEntries = maxEntries;
        removeOldEntries();
    }

    @Override
    public Optional<Integer> getMaxEntries() {
        return this.maxEntries;
    }
}
