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
import static com.github.tomakehurst.wiremock.stubbing.ServeEvent.ORIGINAL_SERVE_EVENT_KEY;

import com.github.tomakehurst.wiremock.common.DataTruncationSettings;
import com.github.tomakehurst.wiremock.extension.requestfilter.*;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractRequestHandler implements RequestHandler, RequestEventSource {

  protected List<RequestListener> listeners = new ArrayList<>();
  protected final ResponseRenderer responseRenderer;
  protected final FilterProcessor filterProcessor;

  private final DataTruncationSettings dataTruncationSettings;

  public AbstractRequestHandler(
      ResponseRenderer responseRenderer,
      List<RequestFilter> requestFilters,
      List<RequestFilterV2> v2RequestFilters,
      DataTruncationSettings dataTruncationSettings) {
    this.responseRenderer = responseRenderer;
    this.filterProcessor = new FilterProcessor(requestFilters, v2RequestFilters);
    this.dataTruncationSettings = dataTruncationSettings;
  }

  @Override
  public void addRequestListener(RequestListener requestListener) {
    listeners.add(requestListener);
  }

  protected void beforeResponseSent(ServeEvent serveEvent, Response response) {}

  protected void afterResponseSent(ServeEvent serveEvent, Response response) {}

  @Override
  public void handle(Request request, HttpResponder httpResponder, ServeEvent originalServeEvent) {
    ServeEvent serveEvent = ServeEvent.of(request);
    Request processedRequest = request;

    if (filterProcessor.hasAnyFilters()) {
      RequestFilterAction requestFilterAction = filterProcessor.processFilters(request, serveEvent);

      if (requestFilterAction instanceof ContinueAction) {
        processedRequest = ((ContinueAction) requestFilterAction).getRequest();
        serveEvent = handleRequest(serveEvent.replaceRequest(processedRequest));
      } else {
        serveEvent =
            serveEvent.withResponseDefinition(
                ((StopAction) requestFilterAction).getResponseDefinition());
      }
    } else {
      serveEvent = handleRequest(serveEvent);
    }

    ResponseDefinition responseDefinition = serveEvent.getResponseDefinition();
    responseDefinition.setOriginalRequest(processedRequest);
    Response response = responseRenderer.render(serveEvent);
    response = Response.Builder.like(response).protocol(request.getProtocol()).build();
    serveEvent = serveEvent.complete(response, dataTruncationSettings);

    if (logRequests()) {
      notifier()
          .info(
              "Request received:\n"
                  + formatRequest(request)
                  + "\n\nMatched response definition:\n"
                  + responseDefinition
                  + "\n\nResponse:\n"
                  + response);
    }

    for (RequestListener listener : listeners) {
      listener.requestReceived(request, response);
    }

    beforeResponseSent(serveEvent, response);

    serveEvent.beforeSend();

    Map<String, Object> attributes = Map.of(ORIGINAL_SERVE_EVENT_KEY, serveEvent);
    httpResponder.respond(request, response, attributes);

    serveEvent.afterSend();
    afterResponseSent(serveEvent, response);
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
