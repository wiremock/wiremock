/*
 * Copyright (C) 2011-2024 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface Request {

  // This is populated by the serve event.
  @JsonIgnore
  default UUID getId() {
    return null;
  }

  interface Part {
    String getName();

    String getFileName();

    HttpHeader getHeader(String name);

    HttpHeaders getHeaders();

    Body getBody();
  }

  String getUrl();

  String getAbsoluteUrl();

  RequestMethod getMethod();

  String getScheme();

  String getHost();

  int getPort();

  String getClientIp();

  String getHeader(String key);

  HttpHeader header(String key);

  ContentTypeHeader contentTypeHeader();

  HttpHeaders getHeaders();

  boolean containsHeader(String key);

  Set<String> getAllHeaderKeys();

  // These are calculated from other fields so should not be serialised
  @JsonIgnore
  default PathParams getPathParameters() {
    return PathParams.empty();
  }

  QueryParameter queryParameter(String key);

  FormParameter formParameter(String key);

  Map<String, FormParameter> formParameters();

  Map<String, Cookie> getCookies();

  /**
   * Returns the body of the request.
   * <p>
   * It will transparently decode the body if it is encoded with gzip.
   * @return the body of the request
   */
  byte[] getBody();

  /**
   * Returns the raw body of the request.
   * <p>
   * It will not decode the body if it is encoded with gzip.
   * @return the raw body of the request
   */
  byte[] getRawBody();

  String getBodyAsString();

  String getBodyAsBase64();

  boolean isMultipart();

  Collection<Part> getParts();

  Part getPart(String name);

  boolean isBrowserProxyRequest();

  Optional<Request> getOriginalRequest();

  String getProtocol();
}
