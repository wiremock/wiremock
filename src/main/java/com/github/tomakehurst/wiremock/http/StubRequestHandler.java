/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.extension.ServeEventListener.RequestPhase.*;

import com.github.tomakehurst.wiremock.common.DataTruncationSettings;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.StubServer;
import com.github.tomakehurst.wiremock.extension.*;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterV2;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.github.tomakehurst.wiremock.verification.RequestJournal;
import com.github.tomakehurst.wiremock.verification.diff.DiffEventData;
import com.github.tomakehurst.wiremock.verification.notmatched.NotMatchedRenderer;
import java.util.List;
import java.util.Map;

public class StubRequestHandler extends AbstractRequestHandler {

  private final StubServer stubServer;
  private final Admin admin;
  private final Map<String, PostServeAction> postServeActions;
  private final Map<String, ServeEventListener> serveEventListeners;
  private final RequestJournal requestJournal;
  private final boolean loggingDisabled;

  private final NotMatchedRenderer notMatchedRenderer;

  public StubRequestHandler(
      StubServer stubServer,
      ResponseRenderer responseRenderer,
      Admin admin,
      Map<String, PostServeAction> postServeActions,
      Map<String, ServeEventListener> serveEventListeners,
      RequestJournal requestJournal,
      List<RequestFilter> requestFilters,
      List<RequestFilterV2> v2RequestFilters,
      boolean loggingDisabled,
      DataTruncationSettings dataTruncationSettings,
      NotMatchedRenderer notMatchedRenderer) {
    super(responseRenderer, requestFilters, v2RequestFilters, dataTruncationSettings);
    this.stubServer = stubServer;
    this.admin = admin;
    this.postServeActions = postServeActions;
    this.serveEventListeners = serveEventListeners;
    this.requestJournal = requestJournal;
    this.loggingDisabled = loggingDisabled;
    this.notMatchedRenderer = notMatchedRenderer;
  }

  @Override
  public ServeEvent handleRequest(ServeEvent initialServeEvent) {
    triggerListeners(BEFORE_MATCH, initialServeEvent);
    final ServeEvent serveEvent = stubServer.serveStubFor(initialServeEvent);
    triggerListeners(AFTER_MATCH, serveEvent);
    return serveEvent;
  }

  @Override
  protected boolean logRequests() {
    return !loggingDisabled;
  }

  @Override
  protected void beforeResponseSent(ServeEvent serveEvent, Response response) {
    if (!response.wasConfigured()) {
      appendNonMatchSubEvent(serveEvent);
    }

    requestJournal.requestReceived(serveEvent);

    triggerListeners(BEFORE_RESPONSE_SENT, serveEvent);
  }

  private void appendNonMatchSubEvent(ServeEvent serveEvent) {
    final ResponseDefinition responseDefinition =
        notMatchedRenderer.execute(admin, serveEvent, PathParams.empty());
    final HttpHeaders headers = responseDefinition.getHeaders();
    final String contentTypeHeader =
        headers != null && headers.getHeader(ContentTypeHeader.KEY).isPresent()
            ? headers.getContentTypeHeader().firstValue()
            : null;

    serveEvent.appendSubEvent(
        SubEvent.NON_MATCH_TYPE,
        new DiffEventData(
            responseDefinition.getStatus(), contentTypeHeader, responseDefinition.getBody()));
  }

  @Override
  protected void afterResponseSent(ServeEvent serveEvent, Response response) {
    requestJournal.serveCompleted(serveEvent);

    triggerPostServeActions(serveEvent);

    triggerListeners(AFTER_COMPLETE, serveEvent);
  }

  private void triggerPostServeActions(ServeEvent serveEvent) {
    for (PostServeAction postServeAction : postServeActions.values()) {
      postServeAction.doGlobalAction(serveEvent, admin);
    }

    List<PostServeActionDefinition> postServeActionDefs = serveEvent.getPostServeActions();
    for (PostServeActionDefinition postServeActionDef : postServeActionDefs) {
      PostServeAction action = postServeActions.get(postServeActionDef.getName());
      if (action != null) {
        Parameters parameters = postServeActionDef.getParameters();
        action.doAction(serveEvent, admin, parameters);
      } else {
        notifier().error("No extension was found named \"" + postServeActionDef.getName() + "\"");
      }
    }
  }

  private void triggerListeners(
      ServeEventListener.RequestPhase requestPhase, ServeEvent serveEvent) {
    serveEventListeners.values().stream()
        .filter(ServeEventListener::applyGlobally)
        .forEach(listener -> listener.onEvent(requestPhase, serveEvent, Parameters.empty()));

    List<ServeEventListenerDefinition> serveEventListenerDefinitions =
        serveEvent.getServeEventListeners();
    for (ServeEventListenerDefinition listenerDef : serveEventListenerDefinitions) {
      ServeEventListener listener = serveEventListeners.get(listenerDef.getName());
      if (listener != null
          && !listener.applyGlobally()
          && listenerDef.shouldFireFor(requestPhase)) {
        Parameters parameters = listenerDef.getParameters();
        listener.onEvent(requestPhase, serveEvent, parameters);
      } else {
        notifier().error("No per-stub listener was found named \"" + listenerDef.getName() + "\"");
      }
    }
  }
}
