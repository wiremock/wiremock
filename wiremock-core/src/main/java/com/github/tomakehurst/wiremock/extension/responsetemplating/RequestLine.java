/*
 * Copyright (C) 2017-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import static com.github.tomakehurst.wiremock.common.Urls.toStrings;

import com.github.tomakehurst.wiremock.common.ListOrSingle;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.wiremock.url.PathAndQuery;

public class RequestLine {
  private final RequestMethod method;
  private final String scheme;
  private final String host;
  private final int port;
  private final Map<String, ListOrSingle<String>> query;
  private final PathAndQuery url;
  private final String clientIp;

  private final PathParams pathParams;

  private RequestLine(
      RequestMethod method,
      String scheme,
      String host,
      int port,
      PathAndQuery url,
      String clientIp,
      Map<String, ListOrSingle<String>> query,
      PathParams pathParams) {
    this.method = method;
    this.scheme = scheme;
    this.host = host;
    this.port = port;
    this.url = url;
    this.clientIp = clientIp;
    this.query = query;
    this.pathParams = pathParams;
  }

  public static RequestLine fromRequest(final Request request) {
    var query = request.getTypedAbsoluteUrl().getQueryOrEmpty();
    Map<String, ListOrSingle<String>> adaptedQuery =
        query.asMap().entrySet().stream()
            .map(
                e -> {
                  var key = e.getKey().decode();
                  var values = toStrings(e.getValue());
                  return Map.entry(key, ListOrSingle.of(values));
                })
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    return new RequestLine(
        request.getMethod(),
        request.getScheme(),
        request.getHost(),
        request.getPort(),
        request.getPathAndQuery(),
        request.getClientIp(),
        adaptedQuery,
        request.getPathParameters());
  }

  public RequestMethod getMethod() {
    return method;
  }

  public Object getPathSegments() {
    return pathParams.isEmpty() ? new UrlPath(url) : new TemplatedUrlPath(url, pathParams);
  }

  public String getPath() {
    return getPathSegments().toString();
  }

  public String getUrl() {
    return url.toString();
  }

  public Map<String, ListOrSingle<String>> getQuery() {
    return query;
  }

  public String getScheme() {
    return scheme;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getBaseUrl() {
    String portPart = isStandardPort(scheme, port) ? "" : ":" + port;

    return scheme + "://" + host + portPart;
  }

  public String getClientIp() {
    return this.clientIp;
  }

  private boolean isStandardPort(String scheme, int port) {
    return (scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443);
  }
}
