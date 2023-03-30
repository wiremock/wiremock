/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.DataTruncationSettings;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.Timing;
import com.github.tomakehurst.wiremock.extension.PostServeActionDefinition;
import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

public class ServeEvent {

  private static final InheritableThreadLocal<ServeEvent> currentEvent =
      new InheritableThreadLocal<>();

  private final UUID id;
  private final LoggedRequest request;
  private final StubMapping stubMapping;
  private final ResponseDefinition responseDefinition;
  private final LoggedResponse response;
  private final AtomicReference<Timing> timing;

  @JsonCreator
  public ServeEvent(
      @JsonProperty("id") UUID id,
      @JsonProperty("request") LoggedRequest request,
      @JsonProperty("mapping") StubMapping stubMapping,
      @JsonProperty("responseDefinition") ResponseDefinition responseDefinition,
      @JsonProperty("response") LoggedResponse response,
      @JsonProperty("wasMatched") boolean ignoredReadOnly,
      @JsonProperty("timing") Timing timing) {
    this.id = id;
    this.request = request;
    this.responseDefinition = responseDefinition;
    this.stubMapping = stubMapping;
    this.response = response;
    this.timing = new AtomicReference<>(timing);
  }

  protected ServeEvent(
      LoggedRequest request, StubMapping stubMapping, ResponseDefinition responseDefinition) {
    this(UUID.randomUUID(), request, stubMapping, responseDefinition, null, false, null);
  }

  public static ServeEvent forUnmatchedRequest(LoggedRequest request) {
    return new ServeEvent(request, null, ResponseDefinition.notConfigured());
  }

  public static ServeEvent forBadRequest(LoggedRequest request, Errors errors) {
    return new ServeEvent(request, null, ResponseDefinition.badRequest(errors));
  }

  public static ServeEvent forBadRequestEntity(LoggedRequest request, Errors errors) {
    return new ServeEvent(request, null, ResponseDefinition.badRequestEntity(errors));
  }

  public static ServeEvent forNotAllowedRequest(LoggedRequest request, Errors errors) {
    return new ServeEvent(request, null, ResponseDefinition.notPermitted(errors));
  }

  public static ServeEvent of(Request request) {
    return new ServeEvent(LoggedRequest.createFrom(request), null, null);
  }

  public static ServeEvent of(Request request, StubMapping stubMapping) {
    return new ServeEvent(LoggedRequest.createFrom(request), stubMapping, null);
  }

  public static ServeEvent of(LoggedRequest request, ResponseDefinition responseDefinition) {
    return new ServeEvent(request, null, responseDefinition);
  }

  public static ServeEvent of(
      LoggedRequest request, ResponseDefinition responseDefinition, StubMapping stubMapping) {
    return new ServeEvent(request, stubMapping, responseDefinition);
  }

  public static ServeEvent getCurrent() {
    return currentEvent.get();
  }

  public static void setCurrent(ServeEvent serveEvent) {
    currentEvent.set(serveEvent);
  }

  public static void clearCurrent() {
    currentEvent.remove();
  }

  public ServeEvent withResponseDefinition(ResponseDefinition responseDefinition) {
    return new ServeEvent(
        id, request, stubMapping, responseDefinition, response, false, getTiming());
  }

  public ServeEvent complete(
      Response response, int processTimeMillis, DataTruncationSettings dataTruncationSettings) {
    return new ServeEvent(
        id,
        request,
        stubMapping,
        responseDefinition,
        LoggedResponse.from(response, dataTruncationSettings.getMaxResponseBodySize()),
        false,
        new Timing((int) response.getInitialDelay(), processTimeMillis));
  }

  public void afterSend(int responseSendTimeMillis) {
    timing.set(timing.get().withResponseSendTime(responseSendTimeMillis));
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
    return timing.get();
  }

  @JsonIgnore
  public List<PostServeActionDefinition> getPostServeActions() {
    return stubMapping != null && stubMapping.getPostServeActions() != null
        ? getStubMapping().getPostServeActions()
        : Collections.emptyList();
  }

  public static final Function<ServeEvent, LoggedRequest> TO_LOGGED_REQUEST =
      ServeEvent::getRequest;

  public static final Predicate<ServeEvent> NOT_MATCHED = ServeEvent::isNoExactMatch;
}
