package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.util.List;
import java.util.UUID;

public abstract class PersistJournalRequestWrapper {

		public abstract  void add(ServeEvent mapping);

		public abstract List<ServeEvent> remove(Predicate<ServeEvent> predicate);

		public abstract void clear();

		public abstract List<ServeEvent> getAll();

		public abstract Optional<ServeEvent> getServeEvent(final UUID id);

		protected abstract Iterable<LoggedRequest> getRequests();

		protected abstract void removeOldEntries(Optional<Integer> maxEntries);

}
