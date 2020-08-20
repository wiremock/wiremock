package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	public List<ServeEvent> remove(List<ServeEvent> toDelete) {

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
	protected Queue<ServeEvent> getServeQueue() {
		return serveEvents;
	}

	@Override
	public List<ServeEvent> getAll() {
		return ImmutableList.copyOf(serveEvents).reverse();
	}

}
