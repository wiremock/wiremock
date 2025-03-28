/*
 * Copyright (C) 2025 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.http.client.HttpClient;
import java.util.LinkedList;
import java.util.List;

public class HeaderUtil {
  public static HttpHeaders headersFrom(
      Response response, ResponseDefinition responseDefinition, boolean stubCorsEnabled) {
    List<HttpHeader> httpHeaders = new LinkedList<>();
    for (HttpHeader header : response.getHeaders().all()) {
      if (responseHeaderShouldBeTransferred(header.getKey(), stubCorsEnabled)) {
        httpHeaders.add(header);
      }
    }

    if (responseDefinition.getHeaders() != null) {
      httpHeaders.addAll(responseDefinition.getHeaders().all());
    }

    return new HttpHeaders(httpHeaders);
  }

  public static boolean responseHeaderShouldBeTransferred(String key, boolean stubCorsEnabled) {
    final String lowerCaseKey = key.toLowerCase();
    return !HttpClient.FORBIDDEN_RESPONSE_HEADERS.contains(lowerCaseKey)
        && (!stubCorsEnabled || !lowerCaseKey.startsWith("access-control"));
  }
}
