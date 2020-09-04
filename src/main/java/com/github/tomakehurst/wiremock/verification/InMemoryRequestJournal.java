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

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.PersistJournalRequests;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.util.*;

import static com.github.tomakehurst.wiremock.matching.RequestPattern.thatMatch;
import static com.github.tomakehurst.wiremock.matching.RequestPattern.withRequstMatching;
import static com.google.common.collect.Iterables.*;

public class InMemoryRequestJournal implements RequestJournal {

	private  PersistJournalRequestWrapper serveEvents;

	private final Optional<Integer> maxEntries;

	private boolean persistJournals=false;

	private PersistJournalRequests persistJournalRequests;

	public InMemoryRequestJournal(Optional<Integer> maxEntries, Map<String, PersistJournalRequests> persistStubMappings) {

		if (maxEntries.isPresent() && maxEntries.get() < 0) {
			throw new IllegalArgumentException("Maximum number of entries of journal must be greater than zero");
		}

		this.maxEntries = maxEntries;

		if (persistStubMappings.isEmpty()) {

			serveEvents = new SortedConcurrentLinkedQueue();

		}else if (persistStubMappings.size() == 1) {

				this.serveEvents=ImmutableList.copyOf(persistStubMappings.values()).get(0);

		}else {

			throw new IllegalArgumentException("Multiple PersistJournalRequests extensions present,Only one configuration allowed");
		}
	}

	@Override
	public int countRequestsMatching(RequestPattern requestPattern) {
		return size(filter(getRequests(), thatMatch(requestPattern)));
	}

	@Override
	public List<LoggedRequest> getRequestsMatching(RequestPattern requestPattern) {
		return ImmutableList.copyOf(filter(getRequests(), thatMatch(requestPattern)));
	}

	@Override
	public void requestReceived(ServeEvent serveEvent) {

		serveEvents.add(serveEvent);
        removeOldEntries();
	}

	@Override
	public void removeEvent(final UUID eventId) {
		removeServeEvents(new Predicate<ServeEvent>() {
			@Override
			public boolean apply(ServeEvent input) {
				return input.getId().equals(eventId);
			}
		});
	}

	@Override
	public List<ServeEvent> removeEventsMatching(RequestPattern requestPattern) {
		return removeServeEvents(withRequstMatching(requestPattern));
	}

	@Override
	public List<ServeEvent> removeServeEventsForStubsMatchingMetadata(StringValuePattern metadataPattern) {
		return removeServeEvents(withStubMetadataMatching(metadataPattern));
	}

	private List<ServeEvent> removeServeEvents(Predicate<ServeEvent> predicate) {

		Queue<ServeEvent> getserveEvents = serveEvents.getServeQueue();

		List<ServeEvent> toDelete = FluentIterable.from(getserveEvents)
				.filter(predicate)
				.toList();

		return serveEvents.remove(toDelete);
	}

	@Override
    public List<ServeEvent> getAllServeEvents() {

        return serveEvents.getAll();
    }

	@Override
	public Optional<ServeEvent> getServeEvent(final UUID id) {

		Queue<ServeEvent> getserveEvents = serveEvents.getServeQueue();

		return tryFind(getserveEvents, new Predicate<ServeEvent>() {
				@Override
				public boolean apply(ServeEvent input) {
					return input.getId().equals(id);
				}
			});

	}

	@Override
	public void reset() {

		serveEvents.clear();
	}

	private Iterable<LoggedRequest> getRequests() {

		Queue<ServeEvent> getserveEvents = serveEvents.getServeQueue();

		return transform(getserveEvents, new Function<ServeEvent, LoggedRequest>() {
			public LoggedRequest apply(ServeEvent input) {
				return input.getRequest();
			}
		});

	}

	private void removeOldEntries() {

		Queue<ServeEvent> getserveEvents = serveEvents.getServeQueue();

		if (maxEntries.isPresent()) {
			while (getserveEvents.size() > maxEntries.get()) {
				getserveEvents.poll();
			}
		}

	}

	private static Predicate<ServeEvent> withStubMetadataMatching(final StringValuePattern metadataPattern) {
		return new Predicate<ServeEvent>() {
			@Override
			public boolean apply(ServeEvent serveEvent) {
				StubMapping stub = serveEvent.getStubMapping();
				if (stub != null) {
					String metadataJson = Json.write(stub.getMetadata());
					return metadataPattern.match(metadataJson).isExactMatch();
				}

				return false;
			}
		};
	}

}
