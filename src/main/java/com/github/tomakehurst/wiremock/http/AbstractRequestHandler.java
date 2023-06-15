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

import com.github.tomakehurst.wiremock.common.DataTruncationSettings;
import com.github.tomakehurst.wiremock.extension.requestfilter.ContinueAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.StopAction;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

import java.util.List;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.extension.requestfilter.FilterProcessor.processFilters;
import static com.google.common.collect.Lists.newArrayList;

public abstract class AbstractRequestHandler implements RequestHandler, RequestEventSource {

  protected List<RequestListener> listeners = newArrayList();
  protected final ResponseRenderer responseRenderer;
  protected final List<RequestFilter> requestFilters;

  private final DataTruncationSettings dataTruncationSettings;

  public AbstractRequestHandler(
      ResponseRenderer responseRenderer,
      List<RequestFilter> requestFilters,
      DataTruncationSettings dataTruncationSettings) {
    this.responseRenderer = responseRenderer;
    this.requestFilters = requestFilters;
    this.dataTruncationSettings = dataTruncationSettings;
  }

  @Override
  public void addRequestListener(RequestListener requestListener) {
    listeners.add(requestListener);
  }

  protected void beforeResponseSent(ServeEvent serveEvent, Response response) {}

  protected void afterResponseSent(ServeEvent serveEvent, Response response) {}

  @Override
  public void handle(Request request, HttpResponder httpResponder) {
    ServeEvent serveEvent = ServeEvent.of(request);
    Request processedRequest = request;

    if (!requestFilters.isEmpty()) {
      RequestFilterAction requestFilterAction =
          processFilters(request, requestFilters, RequestFilterAction.continueWith(request));
      if (requestFilterAction instanceof ContinueAction) {
        processedRequest = ((ContinueAction) requestFilterAction).getRequest();
        serveEvent = handleRequest(serveEvent.replaceRequest(processedRequest));
      } else {
        serveEvent =
            serveEvent.withResponseDefinition(((StopAction) requestFilterAction).getResponseDefinition());
      }
    } else {
      serveEvent = handleRequest(serveEvent);
    }

    ResponseDefinition responseDefinition = serveEvent.getResponseDefinition();
    responseDefinition.setOriginalRequest(processedRequest);
    Response response = responseRenderer.render(serveEvent);
    response = Response.Builder.like(response).protocol(request.getProtocol()).build();
    ServeEvent completedServeEvent = serveEvent.complete(response, dataTruncationSettings);

    if (logRequests()) {
      notifier()
          .info(
              "Request received:\n"
                  + formatRequest(processedRequest)
                  + "\n\nMatched response definition:\n"
                  + responseDefinition
                  + "\n\nResponse:\n"
                  + response);
    }

    for (RequestListener listener : listeners) {
      listener.requestReceived(processedRequest, response);
    }

    beforeResponseSent(completedServeEvent, response);

    serveEvent.beforeSend();
    httpResponder.respond(processedRequest, response);
    serveEvent.afterSend();
    afterResponseSent(completedServeEvent, response);
  }

  protected String formatRequest(Request request) {
    StringBuilder sb = new StringBuilder();
    sb.append(request.getClientIp())
        .append(" - ")
        .append(request.getMethod())
        .append(" ")
        .append(request.getUrl());

    if (request.isBrowserProxyRequest()) {
      sb.append(" (via browser proxy request)");
    }

    sb.append("\n\n");
    sb.append(request.getHeaders());

    if (request.getBody() != null) {
      sb.append(request.getBodyAsString()).append("\n");
    }

    return sb.toString();
  }

  protected boolean logRequests() {
    return false;
  }

  protected abstract ServeEvent handleRequest(ServeEvent request);
}
