/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import java.util.Map;

/** The type Request template model. */
public class RequestTemplateModel {

  private final String id;
  private final RequestLine requestLine;
  private final Map<String, ListOrSingle<String>> headers;
  private final Map<String, ListOrSingle<String>> cookies;

  private final boolean isMultipart;
  private final Body body;
  private final Map<String, RequestPartTemplateModel> parts;

  /**
   * Instantiates a new Request template model.
   *
   * @param id the id
   * @param requestLine the request line
   * @param headers the headers
   * @param cookies the cookies
   * @param isMultipart the is multipart
   * @param body the body
   * @param parts the parts
   */
  protected RequestTemplateModel(
      String id,
      RequestLine requestLine,
      Map<String, ListOrSingle<String>> headers,
      Map<String, ListOrSingle<String>> cookies,
      boolean isMultipart,
      Body body,
      Map<String, RequestPartTemplateModel> parts) {
    this.id = id;
    this.requestLine = requestLine;
    this.headers = headers;
    this.cookies = cookies;
    this.isMultipart = isMultipart;
    this.body = body;
    this.parts = parts;
  }

  /**
   * Gets id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Gets request line.
   *
   * @return the request line
   */
  @Deprecated
  /**
   * @deprecated Use the direct accessors
   */
  public RequestLine getRequestLine() {
    return requestLine;
  }

  /**
   * Gets method.
   *
   * @return the method
   */
  public RequestMethod getMethod() {
    return requestLine.getMethod();
  }

  /**
   * Gets path segments.
   *
   * @return the path segments
   */
  public Object getPathSegments() {
    return requestLine.getPathSegments();
  }

  /**
   * Gets path.
   *
   * @return the path
   */
  public Object getPath() {
    return requestLine.getPathSegments();
  }

  /**
   * Gets url.
   *
   * @return the url
   */
  public String getUrl() {
    return requestLine.getUrl();
  }

  /**
   * Gets query.
   *
   * @return the query
   */
  public Map<String, ListOrSingle<String>> getQuery() {
    return requestLine.getQuery();
  }

  /**
   * Gets scheme.
   *
   * @return the scheme
   */
  public String getScheme() {
    return requestLine.getScheme();
  }

  /**
   * Gets host.
   *
   * @return the host
   */
  public String getHost() {
    return requestLine.getHost();
  }

  /**
   * Gets port.
   *
   * @return the port
   */
  public int getPort() {
    return requestLine.getPort();
  }

  /**
   * Gets base url.
   *
   * @return the base url
   */
  public String getBaseUrl() {
    return requestLine.getBaseUrl();
  }

  /**
   * Gets headers.
   *
   * @return the headers
   */
  public Map<String, ListOrSingle<String>> getHeaders() {
    return headers;
  }

  /**
   * Gets cookies.
   *
   * @return the cookies
   */
  public Map<String, ListOrSingle<String>> getCookies() {
    return cookies;
  }

  /**
   * Gets body.
   *
   * @return the body
   */
  public String getBody() {
    return body.asString();
  }

  /**
   * Gets body as base 64.
   *
   * @return the body as base 64
   */
  public String getBodyAsBase64() {
    return body.asBase64();
  }

  /**
   * Is binary boolean.
   *
   * @return the boolean
   */
  public boolean isBinary() {
    return body.isBinary();
  }

  /**
   * Is multipart boolean.
   *
   * @return the boolean
   */
  public boolean isMultipart() {
    return isMultipart;
  }

  /**
   * Gets parts.
   *
   * @return the parts
   */
  public Map<String, RequestPartTemplateModel> getParts() {
    return parts;
  }

  /**
   * Gets client ip.
   *
   * @return the client ip
   */
  public String getClientIp() {
    return requestLine.getClientIp();
  }
}
