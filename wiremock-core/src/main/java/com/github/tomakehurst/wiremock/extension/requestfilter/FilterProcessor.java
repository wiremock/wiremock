/*
 * Copyright (C) 2020-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.requestfilter;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.List;

public class FilterProcessor {

  private final List<? extends RequestFilter> v1RequestFilters;
  private final List<? extends RequestFilterV2> v2RequestFilters;

  public FilterProcessor(
      List<? extends RequestFilter> v1RequestFilters,
      List<? extends RequestFilterV2> v2RequestFilters) {
    this.v1RequestFilters = v1RequestFilters;
    this.v2RequestFilters = v2RequestFilters;
  }

  public RequestFilterAction processFilters(Request request, ServeEvent serveEvent) {
    RequestFilterAction requestFilterAction =
        processV1Filters(request, v1RequestFilters, RequestFilterAction.continueWith(request));
    if (requestFilterAction instanceof ContinueAction) {
      return processV2Filters(request, serveEvent, v2RequestFilters, requestFilterAction);
    } else {
      return requestFilterAction;
    }
  }

  private RequestFilterAction processV1Filters(
      Request request,
      List<? extends RequestFilter> requestFilters,
      RequestFilterAction lastAction) {

    if (requestFilters.isEmpty()) {
      return lastAction;
    }

    RequestFilterAction action = requestFilters.get(0).filter(request);

    if (action instanceof ContinueAction) {
      Request newRequest = ((ContinueAction) action).getRequest();
      return processV1Filters(newRequest, requestFilters.subList(1, requestFilters.size()), action);
    }

    return action;
  }

  private RequestFilterAction processV2Filters(
      Request request,
      ServeEvent serveEvent,
      List<? extends RequestFilterV2> v2RequestFilters,
      RequestFilterAction lastAction) {

    if (v2RequestFilters.isEmpty()) {
      return lastAction;
    }

    RequestFilterAction action = v2RequestFilters.get(0).filter(request, serveEvent);

    if (action instanceof ContinueAction) {
      Request newRequest = ((ContinueAction) action).getRequest();
      return processV2Filters(
          newRequest, serveEvent, v2RequestFilters.subList(1, v2RequestFilters.size()), action);
    }

    return action;
  }

  public boolean hasAnyFilters() {
    return !v1RequestFilters.isEmpty() || !v2RequestFilters.isEmpty();
  }
}
