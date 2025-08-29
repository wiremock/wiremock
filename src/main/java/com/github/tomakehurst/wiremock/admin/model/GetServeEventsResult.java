/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.admin.Paginator;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.List;

/**
 * A paginated result class for retrieving served requests (ServeEvents).
 *
 * <p>This serves as a data transfer object (DTO) for the request journal, providing a paginated
 * view of recorded requests. It also handles the case where the request journal is disabled.
 *
 * @see ServeEvent
 * @see Paginator
 * @see RequestJournalDependentResult
 */
public class GetServeEventsResult extends RequestJournalDependentResult<ServeEvent> {

  /**
   * Constructs a new GetServeEventsResult for JSON deserialization.
   *
   * @param source The full list of serve events.
   * @param meta The metadata including pagination details.
   * @param requestJournalDisabled True if the request journal is disabled.
   */
  @JsonCreator
  public GetServeEventsResult(
      @JsonProperty("requests") List<ServeEvent> source,
      @JsonProperty("meta") Meta meta,
      @JsonProperty("requestJournalDisabled") boolean requestJournalDisabled) {
    super(source, meta, requestJournalDisabled);
  }

  /**
   * Constructs a new GetServeEventsResult from a paginator.
   *
   * @param paginator The paginator for the serve events.
   * @param requestJournalDisabled True if the request journal is disabled.
   */
  public GetServeEventsResult(Paginator<ServeEvent> paginator, boolean requestJournalDisabled) {
    super(paginator, requestJournalDisabled);
  }

  /**
   * Factory method to create a result when the request journal is enabled.
   *
   * @param paginator The paginator for the serve events.
   * @return A new {@code GetServeEventsResult} instance.
   */
  public static GetServeEventsResult requestJournalEnabled(Paginator<ServeEvent> paginator) {
    return new GetServeEventsResult(paginator, false);
  }

  /**
   * Factory method to create a result when the request journal is disabled.
   *
   * @param paginator A paginator (will be ignored and an empty result returned).
   * @return A new {@code GetServeEventsResult} instance indicating the journal is disabled.
   */
  public static GetServeEventsResult requestJournalDisabled(Paginator<ServeEvent> paginator) {
    return new GetServeEventsResult(paginator, true);
  }

  public List<ServeEvent> getRequests() {
    return getServeEvents();
  }

  @JsonIgnore
  public List<ServeEvent> getServeEvents() {
    return select();
  }
}
