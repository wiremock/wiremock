/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** The interface Request journal. */
public interface RequestJournal {

  /**
   * Count requests matching int.
   *
   * @param requestPattern the request pattern
   * @return the int
   */
  int countRequestsMatching(RequestPattern requestPattern);

  /**
   * Gets requests matching.
   *
   * @param requestPattern the request pattern
   * @return the requests matching
   */
  List<LoggedRequest> getRequestsMatching(RequestPattern requestPattern);

  /**
   * Gets all serve events.
   *
   * @return the all serve events
   */
  List<ServeEvent> getAllServeEvents();

  /**
   * Gets serve event.
   *
   * @param id the id
   * @return the serve event
   */
  Optional<ServeEvent> getServeEvent(UUID id);

  /** Reset. */
  void reset();

  /**
   * Request received.
   *
   * @param serveEvent the serve event
   */
  void requestReceived(ServeEvent serveEvent);

  /**
   * Serve completed.
   *
   * @param serveEvent the serve event
   */
  void serveCompleted(ServeEvent serveEvent);

  /**
   * Remove event.
   *
   * @param eventId the event id
   */
  void removeEvent(UUID eventId);

  /**
   * Remove events matching list.
   *
   * @param requestPattern the request pattern
   * @return the list
   */
  List<ServeEvent> removeEventsMatching(RequestPattern requestPattern);

  /**
   * Remove serve events for stubs matching metadata list.
   *
   * @param metadataPattern the metadata pattern
   * @return the list
   */
  List<ServeEvent> removeServeEventsForStubsMatchingMetadata(StringValuePattern metadataPattern);
}
