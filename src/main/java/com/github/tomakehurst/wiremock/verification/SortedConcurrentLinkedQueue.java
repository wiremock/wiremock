package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


import static com.google.common.collect.Iterables.*;

public class SortedConcurrentLinkedQueue extends PersistJournalRequestWrapper {

	private final Queue<ServeEvent> serveEvents;

	public SortedConcurrentLinkedQueue() {
		serveEvents = new ConcurrentLinkedQueue<ServeEvent>();
	}

	@Override
	public void add(ServeEvent serveEvent) {
		serveEvents.add(serveEvent);
	}

	@Override
	public List<ServeEvent> remove(Predicate<ServeEvent> predicate) {

		List<ServeEvent> toDelete = FluentIterable.from(serveEvents)
				.filter(predicate)
				.toList();

		for (ServeEvent event: toDelete) {
			serveEvents.remove(event);
		}

		return toDelete;
	}

	@Override
	public void clear() {
		serveEvents.clear();
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
	public List<ServeEvent> getAll() {
		return ImmutableList.copyOf(serveEvents).reverse();
	}

	@Override
	protected Iterable<LoggedRequest> getRequests() {

		return transform(serveEvents, new Function<ServeEvent, LoggedRequest>() {
			public LoggedRequest apply(ServeEvent input) {
				return input.getRequest();
			}
		});
	}

	@Override
	protected void removeOldEntries(Optional<Integer> maxEntries) {
		if (maxEntries.isPresent()) {
			while (serveEvents.size() > maxEntries.get()) {
				serveEvents.poll();
			}
		}
	}
}
