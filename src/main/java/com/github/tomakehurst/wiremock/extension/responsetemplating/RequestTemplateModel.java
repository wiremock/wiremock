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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.github.tomakehurst.wiremock.common.ListOrSingle;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import java.util.Map;

public class RequestTemplateModel {

  private final String id;
  private final RequestLine requestLine;
  private final Map<String, ListOrSingle<String>> headers;
  private final Map<String, ListOrSingle<String>> cookies;
  private final String body;

  protected RequestTemplateModel(
      String id,
      RequestLine requestLine,
      Map<String, ListOrSingle<String>> headers,
      Map<String, ListOrSingle<String>> cookies,
      String body) {
    this.id = id;
    this.requestLine = requestLine;
    this.headers = headers;
    this.cookies = cookies;
    this.body = body;
  }

  public String getId() {
    return id;
  }

  @Deprecated
  /**
   * @deprecated Use the direct accessors
   */
  public RequestLine getRequestLine() {
    return requestLine;
  }

  public RequestMethod getMethod() {
    return requestLine.getMethod();
  }

  public Object getPathSegments() {
    return requestLine.getPathSegments();
  }

  public Object getPath() {
    return requestLine.getPathSegments();
  }

  public String getUrl() {
    return requestLine.getUrl();
  }

  public Map<String, ListOrSingle<String>> getQuery() {
    return requestLine.getQuery();
  }

  public String getScheme() {
    return requestLine.getScheme();
  }

  public String getHost() {
    return requestLine.getHost();
  }

  public int getPort() {
    return requestLine.getPort();
  }

  public String getBaseUrl() {
    return requestLine.getBaseUrl();
  }

  public Map<String, ListOrSingle<String>> getHeaders() {
    return headers;
  }

  public Map<String, ListOrSingle<String>> getCookies() {
    return cookies;
  }

  public String getBody() {
    return body;
  }

  public String getClientIp() {
    return requestLine.getClientIp();
  }
}
