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
package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import org.wiremock.annotations.Beta;

/**
 * Journal for recording and querying message events. Similar to RequestJournal but for message
 * channel events.
 */
@Beta(justification = "Message Journal API")
public interface MessageJournal {

  /**
   * Counts the number of message events matching the given predicate.
   *
   * @param predicate the predicate to match events against
   * @return the count of matching events
   */
  int countEventsMatching(Predicate<MessageServeEvent> predicate);

  /**
   * Gets all message events matching the given predicate.
   *
   * @param predicate the predicate to match events against
   * @return list of matching events, most recent first
   */
  List<MessageServeEvent> getEventsMatching(Predicate<MessageServeEvent> predicate);

  /**
   * Gets all message serve events.
   *
   * @return list of all events, most recent first
   */
  List<MessageServeEvent> getAllMessageServeEvents();

  /**
   * Gets a specific message serve event by ID.
   *
   * @param id the event ID
   * @return the event if found
   */
  Optional<MessageServeEvent> getMessageServeEvent(UUID id);

  /** Resets the journal, removing all events. */
  void reset();

  /**
   * Records a message event in the journal.
   *
   * @param event the event to record
   */
  void messageReceived(MessageServeEvent event);

  /**
   * Removes a specific event from the journal.
   *
   * @param eventId the ID of the event to remove
   */
  void removeEvent(UUID eventId);

  /**
   * Removes all events matching the given predicate.
   *
   * @param predicate the predicate to match events against
   * @return list of removed events
   */
  List<MessageServeEvent> removeEventsMatching(Predicate<MessageServeEvent> predicate);

  /**
   * Removes all events for stubs matching the given metadata pattern.
   *
   * @param metadataPattern the pattern to match stub metadata against
   * @return list of removed events
   */
  List<MessageServeEvent> removeEventsForStubsMatchingMetadata(StringValuePattern metadataPattern);

  /**
   * Waits for a message event matching the given predicate to appear in the journal.
   *
   * @param predicate the predicate to match events against
   * @param maxWait the maximum duration to wait
   * @return the matching event if found within the timeout, empty otherwise
   */
  Optional<MessageServeEvent> waitForEvent(
      Predicate<MessageServeEvent> predicate, Duration maxWait);

  /**
   * Waits for a specific number of message events matching the given predicate.
   *
   * @param predicate the predicate to match events against
   * @param count the number of events to wait for
   * @param maxWait the maximum duration to wait
   * @return list of matching events if the count is reached within the timeout
   */
  List<MessageServeEvent> waitForEvents(
      Predicate<MessageServeEvent> predicate, int count, Duration maxWait);
}
