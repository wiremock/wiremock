/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.ListOrSingle;
import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.common.url.PathTemplate;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Deprecated
/** @deprecated Use the accessors on {@link RequestTemplateModel} */
public class RequestLine {
  private final RequestMethod method;
  private final String scheme;
  private final String host;
  private final int port;
  private final Map<String, ListOrSingle<String>> query;
  private final String url;
  private final String clientIp;

  private final PathTemplate pathTemplate;

  private RequestLine(
      RequestMethod method,
      String scheme,
      String host,
      int port,
      String url,
      String clientIp,
      Map<String, ListOrSingle<String>> query,
      PathTemplate pathTemplate) {
    this.method = method;
    this.scheme = scheme;
    this.host = host;
    this.port = port;
    this.url = url;
    this.clientIp = clientIp;
    this.query = query;
    this.pathTemplate = pathTemplate;
  }

  public static RequestLine fromRequest(final Request request, final PathTemplate pathTemplate) {
    URI url = URI.create(request.getUrl());
    Map<String, QueryParameter> rawQuery = Urls.splitQuery(url);
    Map<String, ListOrSingle<String>> adaptedQuery =
        rawQuery.entrySet().stream()
            .map(entry -> Map.entry(entry.getKey(), ListOrSingle.of(entry.getValue().values())))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    return new RequestLine(
        request.getMethod(),
        request.getScheme(),
        request.getHost(),
        request.getPort(),
        request.getUrl(),
        request.getClientIp(),
        adaptedQuery,
        pathTemplate);
  }

  public RequestMethod getMethod() {
    return method;
  }

  public Object getPathSegments() {
    return pathTemplate == null ? new UrlPath(url) : new TemplatedUrlPath(url, pathTemplate);
  }

  public String getPath() {
    return getPathSegments().toString();
  }

  public String getUrl() {
    return url;
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
