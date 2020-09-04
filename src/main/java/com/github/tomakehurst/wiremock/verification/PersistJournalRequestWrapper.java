package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.util.List;
import java.util.Queue;
import java.util.UUID;

public abstract class PersistJournalRequestWrapper {

		public abstract  void add(ServeEvent mapping);

		public abstract List<ServeEvent> remove(List<com.github.tomakehurst.wiremock.stubbing.ServeEvent> toDelete);

		public abstract void clear();

		public abstract List<ServeEvent> getAll();

		protected abstract Queue<ServeEvent> getServeQueue();
}
