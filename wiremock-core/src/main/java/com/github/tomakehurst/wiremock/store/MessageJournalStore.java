/*
 * Copyright (C) 2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.store;

import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.wiremock.annotations.Beta;

/**
 * Store interface for message journal events. Similar to RequestJournalStore but for
 * MessageServeEvents.
 */
@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public interface MessageJournalStore extends Store<UUID, MessageServeEvent> {

  /** Returns all message serve events in the store. */
  Stream<MessageServeEvent> getAll();

  /** Adds a new message serve event to the store. */
  void add(MessageServeEvent event);

  /** Removes the oldest event from the store. */
  void removeLast();

  /**
   * Registers a listener to be notified when events are added to the store. This is used to support
   * waiting for events matching specific criteria.
   *
   * @param listener the listener to register
   */
  void registerEventListener(Consumer<? super StoreEvent<UUID, MessageServeEvent>> listener);
}
