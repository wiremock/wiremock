/*
 * Copyright (C) 2016-2026 Thomas Akehurst
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
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import org.wiremock.url.Query;
import org.wiremock.url.QueryParamKey;
import org.wiremock.url.QueryParamValue;

public class ServeEventQuery {

  public static final ServeEventQuery ALL = new ServeEventQuery(false, null);
  public static final ServeEventQuery ALL_UNMATCHED = new ServeEventQuery(true, null);

  public static ServeEventQuery forStubMapping(StubMapping stubMapping) {
    return new ServeEventQuery(false, stubMapping.getId());
  }

  public static ServeEventQuery forStubMapping(UUID stubMappingId) {
    return new ServeEventQuery(false, stubMappingId);
  }

  private static final QueryParamKey UNMATCHED_KEY = QueryParamKey.encode("unmatched");
  private static final QueryParamValue TRUE_PARAM_VALUE = QueryParamValue.encode("true");
  private static final QueryParamKey MATCHING_STUB_KEY = QueryParamKey.encode("matchingStub");

  public static ServeEventQuery fromRequest(Request request) {
    Query query = request.getPathAndQuery().getQueryOrEmpty();
    final List<@Nullable QueryParamValue> unmatchedParameter = query.get(UNMATCHED_KEY);
    boolean unmatched = unmatchedParameter.contains(TRUE_PARAM_VALUE);

    UUID stubMappingId = toUuid(query, MATCHING_STUB_KEY);

    return new ServeEventQuery(unmatched, stubMappingId);
  }

  private static UUID toUuid(Query query, QueryParamKey key) {
    final QueryParamValue parameter = query.getFirst(key);
    try {
      return parameter != null ? UUID.fromString(parameter.decode()) : null;
    } catch (IllegalArgumentException e) {
      throw new InvalidParameterException(
          Errors.single(
              15, "Query parameter " + key + " value '" + parameter + "' is not a valid UUID"));
    }
  }

  private final boolean onlyUnmatched;
  private final UUID stubMappingId;

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
