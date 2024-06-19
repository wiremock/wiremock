/*
 * Copyright (C) 2016-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.stubbing;

import static com.github.tomakehurst.wiremock.stubbing.SubEvent.NON_MATCH_TYPE;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.DataTruncationSettings;
import com.github.tomakehurst.wiremock.common.Timing;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeActionDefinition;
import com.github.tomakehurst.wiremock.extension.ServeEventListenerDefinition;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.base.Stopwatch;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServeEvent {

  public static final String ORIGINAL_SERVE_EVENT_KEY = "wiremock.ORIGINAL_SERVE_EVENT";

  private final UUID id;
  private final LoggedRequest request;
  private final StubMapping stubMapping;
  private final ResponseDefinition responseDefinition;
  private final LoggedResponse response;
  private final Timing timing;

  private final ConcurrentLinkedQueue<SubEvent> subEvents;

  private final Stopwatch stopwatch;

  protected ServeEvent(
      UUID id,
      LoggedRequest request,
      StubMapping stubMapping,
      ResponseDefinition responseDefinition,
      LoggedResponse response,
      Timing timing,
      ConcurrentLinkedQueue<SubEvent> subEvents,
      Stopwatch stopwatch) {
    this.id = id;
    this.request = request;
    this.stubMapping = stubMapping;
    this.responseDefinition = responseDefinition;
    this.response = response;
    this.timing = timing;
    this.subEvents = subEvents;
    this.stopwatch = stopwatch;
  }

  @JsonCreator
  public ServeEvent(
      @JsonProperty("id") UUID id,
      @JsonProperty("request") LoggedRequest request,
      @JsonProperty("mapping") StubMapping stubMapping,
      @JsonProperty("responseDefinition") ResponseDefinition responseDefinition,
      @JsonProperty("response") LoggedResponse response,
      @JsonProperty("wasMatched") boolean ignoredReadOnly,
      @JsonProperty("timing") Timing timing,
      @JsonProperty("subEvents") Queue<SubEvent> subEvents) {
    this(
        id,
        request,
        stubMapping,
        responseDefinition,
        response,
        timing != null ? timing : Timing.create(),
        subEvents != null ? new ConcurrentLinkedQueue<>(subEvents) : new ConcurrentLinkedQueue<>(),
        Stopwatch.createStarted());
  }

  protected ServeEvent(
      LoggedRequest request, StubMapping stubMapping, ResponseDefinition responseDefinition) {
    this(UUID.randomUUID(), request, stubMapping, responseDefinition, null, false, null, null);
  }

  public static ServeEvent of(Request request) {
    return new ServeEvent(LoggedRequest.createFrom(request), null, null);
  }

  public static ServeEvent ofUnmatched(
      LoggedRequest request, ResponseDefinition responseDefinition) {
    return new ServeEvent(request, null, responseDefinition);
  }

  public ServeEvent replaceRequest(Request request) {
    return new ServeEvent(
        id,
        LoggedRequest.createFrom(request),
        stubMapping,
        responseDefinition,
        response,
        timing,
        subEvents,
        stopwatch);
  }

  public ServeEvent withStubMapping(StubMapping stubMapping) {
    return new ServeEvent(
        id, request, stubMapping, responseDefinition, response, false, timing, subEvents);
  }

  public ServeEvent withResponseDefinition(ResponseDefinition responseDefinition) {
    return new ServeEvent(
        id, request, stubMapping, responseDefinition, response, false, timing, subEvents);
  }

  public ServeEvent withPathParamDecoratedRequest() {
    final LoggedRequest newLoggedRequest =
        LoggedRequest.createFrom(
            RequestPathParamsDecorator.decorate(request, stubMapping.getRequest()));
    return new ServeEvent(
        id, newLoggedRequest, stubMapping, responseDefinition, response, false, timing, subEvents);
  }

  public ServeEvent complete(Response response, DataTruncationSettings dataTruncationSettings) {
    timing.logProcessTime(stopwatch);
    timing.setAddedTime((int) response.getInitialDelay());

    return new ServeEvent(
        id,
        request,
        stubMapping,
        responseDefinition,
        LoggedResponse.from(response, dataTruncationSettings.getMaxResponseBodySize()),
        false,
        timing,
        subEvents);
  }

  public void beforeSend() {
    stopwatch.reset();
  }

  public void afterSend() {
    timing.logResponseSendTime(stopwatch);
  }

  @JsonIgnore
  public boolean isNoExactMatch() {
    return responseDefinition == null || !responseDefinition.wasConfigured();
  }

  public UUID getId() {
    return id;
  }

  public LoggedRequest getRequest() {
    return request;
  }

  public ResponseDefinition getResponseDefinition() {
    return responseDefinition;
  }

  public boolean getWasMatched() {
    return responseDefinition.wasConfigured();
  }

  public StubMapping getStubMapping() {
    return stubMapping;
  }

  public LoggedResponse getResponse() {
    return response;
  }

  public Timing getTiming() {
    return timing;
  }

  public Queue<? extends SubEvent> getSubEvents() {
    return subEvents;
  }

  public void appendSubEvent(String type, Object data) {
    final long elapsedNanos = stopwatch.elapsed(NANOSECONDS);
    appendSubEvent(new SubEvent(type, elapsedNanos, data));
  }

  public void appendSubEvent(SubEvent subEvent) {
    if (hasNotAlreadyBeenAppended(subEvent)) {
      subEvents.add(subEvent);
    }
  }

  private boolean hasNotAlreadyBeenAppended(SubEvent subEvent) {
    if (!subEvent.isStandardType()) {
      return true;
    }

    return subEvents.stream().noneMatch(subEvent::isEquivalentStandardTypedEventTo);
  }

  @JsonIgnore
  public Optional<SubEvent> getDiffSubEvent() {
    return subEvents.stream()
        .filter(subEvent -> subEvent.getType().equals(NON_MATCH_TYPE))
        .findFirst();
  }

  @JsonIgnore
  public List<PostServeActionDefinition> getPostServeActions() {
    return stubMapping != null && stubMapping.getPostServeActions() != null
        ? getStubMapping().getPostServeActions()
        : Collections.emptyList();
  }

  @JsonIgnore
  public List<ServeEventListenerDefinition> getServeEventListeners() {
    return stubMapping != null && stubMapping.getServeEventListeners() != null
        ? getStubMapping().getServeEventListeners()
        : Collections.emptyList();
  }

  @JsonIgnore
  public Parameters getTransformerParameters() {
    return stubMapping != null
            && stubMapping.getResponse() != null
            && stubMapping.getResponse().getTransformerParameters() != null
        ? stubMapping.getResponse().getTransformerParameters()
        : Parameters.empty();
  }
}
