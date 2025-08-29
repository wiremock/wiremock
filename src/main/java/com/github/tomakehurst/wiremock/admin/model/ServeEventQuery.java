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

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.InvalidParameterException;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A query object for filtering collections of {@link ServeEvent}s.
 *
 * <p>Encapsulates filter criteria for serve events, such as whether to include only unmatched
 * requests or requests served by a specific stub mapping.
 *
 * @see ServeEvent
 * @see StubMapping
 */
public class ServeEventQuery {

  public static final ServeEventQuery ALL = new ServeEventQuery(false, null);
  public static final ServeEventQuery ALL_UNMATCHED = new ServeEventQuery(true, null);

  /**
   * Creates a query for events that were served by a specific stub mapping.
   *
   * @param stubMapping The stub mapping to filter by.
   * @return A new {@code ServeEventQuery} instance.
   */
  public static ServeEventQuery forStubMapping(StubMapping stubMapping) {
    return new ServeEventQuery(false, stubMapping.getId());
  }

  /**
   * Creates a query by parsing query parameters from an HTTP request.
   *
   * <p>It looks for "unmatched" and "matchingStub" query parameters.
   *
   * @param request The incoming HTTP request.
   * @return A new {@code ServeEventQuery} instance based on the request parameters.
   */
  public static ServeEventQuery fromRequest(Request request) {
    final QueryParameter unmatchedParameter = request.queryParameter("unmatched");
    boolean unmatched = unmatchedParameter.isPresent() && unmatchedParameter.containsValue("true");

    final QueryParameter stubParameter = request.queryParameter("matchingStub");
    UUID stubMappingId = toUuid(stubParameter);

    return new ServeEventQuery(unmatched, stubMappingId);
  }

  private static UUID toUuid(QueryParameter parameter) {
    try {
      return parameter.isPresent() ? UUID.fromString(parameter.firstValue()) : null;
    } catch (IllegalArgumentException e) {
      throw new InvalidParameterException(
          Errors.single(
              15,
              "Query parameter "
                  + parameter.key()
                  + " value '"
                  + parameter.firstValue()
                  + "' is not a valid UUID"));
    }
  }

  private final boolean onlyUnmatched;
  private final UUID stubMappingId;

  /**
   * Constructs a new ServeEventQuery.
   *
   * @param onlyUnmatched if true, only unmatched requests will be included.
   * @param stubMappingId if not null, only requests served by this stub ID will be included.
   */
  public ServeEventQuery(
      @JsonProperty("onlyUnmatched") boolean onlyUnmatched,
      @JsonProperty("stubMappingId") UUID stubMappingId) {
    this.onlyUnmatched = onlyUnmatched;
    this.stubMappingId = stubMappingId;
  }

  public boolean isOnlyUnmatched() {
    return onlyUnmatched;
  }

  public UUID getStubMappingId() {
    return stubMappingId;
  }

  /**
   * Applies the query's filter criteria to a list of serve events.
   *
   * @param events The list of {@link ServeEvent}s to filter.
   * @return A new list containing only the events that match the query criteria.
   */
  public List<ServeEvent> filter(List<ServeEvent> events) {
    if (!onlyUnmatched && stubMappingId == null) {
      return events;
    }

    final Predicate<ServeEvent> matchPredicate =
        onlyUnmatched ? serveEvent -> !serveEvent.getWasMatched() : serveEvent -> true;

    final Predicate<ServeEvent> stubPredicate =
        stubMappingId != null
            ? serveEvent ->
                serveEvent.getWasMatched()
                    && serveEvent.getStubMapping().getId().equals(stubMappingId)
            : serveEvent -> true;

    return events.stream().filter(matchPredicate).filter(stubPredicate).collect(toList());
  }
}
