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
import com.github.tomakehurst.wiremock.extension.PersistStubMappings;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.PersistStubMappingsWrapper;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.SortedConcurrentMappingSet;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.github.tomakehurst.wiremock.matching.RequestPattern.thatMatch;
import static com.github.tomakehurst.wiremock.matching.RequestPattern.withRequstMatching;
import static com.google.common.collect.Iterables.*;

public class InMemoryRequestJournal implements RequestJournal {

	private final Queue<ServeEvent> serveEvents = new ConcurrentLinkedQueue<ServeEvent>();

	private final Optional<Integer> maxEntries;

	private boolean persistJournals=false;

	private PersistJournalRequests persistJournalRequests;

	public InMemoryRequestJournal(Optional<Integer> maxEntries, Map<String, PersistJournalRequests> persistStubMappings) {
		if (maxEntries.isPresent() && maxEntries.get() < 0) {
			throw new IllegalArgumentException("Maximum number of entries of journal must be greater than zero");
		}
		this.maxEntries = maxEntries;

		if(persistStubMappings.size()>0){
			this.persistJournals=true;
			this.persistJournalRequests=ImmutableList.copyOf(persistStubMappings.values()).get(0);
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

		if(persistJournals){
			persistJournalRequests.add(serveEvent);
		}

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

		List<ServeEvent> toDelete = FluentIterable.from(serveEvents)
				.filter(predicate)
				.toList();

		for (ServeEvent event: toDelete) {

			if(persistJournals){
				persistJournalRequests.remove(event);
			}

			serveEvents.remove(event);
		}

		return toDelete;
	}

	@Override
    public List<ServeEvent> getAllServeEvents() {

		if(persistJournals){

			List<ServeEvent> journalRequests=persistJournalRequests.getAll();

			serveEvents.clear();

			for(ServeEvent journalRequest:journalRequests){
				serveEvents.add(journalRequest);
			}
		}

        return ImmutableList.copyOf(serveEvents).reverse();
    }

	@Override
	public Optional<ServeEvent> getServeEvent(final UUID id) {
		return tryFind(serveEvents, new Predicate<ServeEvent>() {
			@Override
			public boolean apply(ServeEvent input) {
				return input.getId().equals(id);
			}
		});
	}

	@Override
	public void reset() {

		if(persistJournals){
			persistJournalRequests.clear();
		}
		serveEvents.clear();
	}


	private Iterable<LoggedRequest> getRequests() {
		List<ServeEvent> getJournalRequest;

	if(persistJournals){
		getJournalRequest=persistJournalRequests.getAll();

		for(ServeEvent serveEvent:getJournalRequest){
			serveEvents.add(serveEvent);
		}
	}

		return transform(serveEvents, new Function<ServeEvent, LoggedRequest>() {
			public LoggedRequest apply(ServeEvent input) {
				return input.getRequest();
			}
		});
	}

	private void removeOldEntries() {
		if (maxEntries.isPresent()) {
			while (serveEvents.size() > maxEntries.get()) {
				serveEvents.poll();
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
