/*
 * Copyright (C) 2024 Thomas Akehurst
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class LoggedRequests extends ArrayList<LoggedRequest> {

  public LoggedRequests(final List<LoggedRequest> requests) {
    this.addAll(requests);
  }

  public String requestSummary() {
    Map<String, Integer> sortedRequests = new TreeMap<>();
    Map<String, String> readableRequests = new HashMap<>();

    for (LoggedRequest loggedRequest : this) {
      String requestKey = loggedRequest.getUrl() + " " + loggedRequest.getMethod().getName();
      String readableRequestString =
          loggedRequest.getMethod().getName() + " " + loggedRequest.getUrl();
      if (sortedRequests.containsKey(requestKey)) {
        sortedRequests.put(requestKey, sortedRequests.get(requestKey) + 1);
      } else {
        sortedRequests.put(requestKey, 1);
      }
      readableRequests.put(requestKey, readableRequestString);
    }
    return sortedRequests.entrySet().stream()
        .map((e) -> e.getValue() + "\t| " + readableRequests.get(e.getKey()))
        .collect(Collectors.joining("\n"));
  }
}
