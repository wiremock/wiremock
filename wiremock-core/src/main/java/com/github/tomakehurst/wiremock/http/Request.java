/*
 * Copyright (C) 2011-2026 Thomas Akehurst
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.wiremock.url.AbsoluteUrl;
import org.wiremock.url.PathAndQuery;
import org.wiremock.url.Query;

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

  @Deprecated // use getPathAndQueryWithoutPrefix()
  @NonNull String getUrl();

  @JsonIgnore
  default @NonNull PathAndQuery getPathAndQueryWithoutPrefix() {
    return PathAndQuery.parse(getUrl());
  }

  @Deprecated // use getTypedAbsoluteUrl()
  @Nullable String getAbsoluteUrl();

  @JsonIgnore
  default @Nullable AbsoluteUrl getTypedAbsoluteUrl() {
    String absoluteUrl = getAbsoluteUrl();
    return absoluteUrl != null ? AbsoluteUrl.parse(absoluteUrl) : null;
  }

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

  @Deprecated // use getPathAndQueryWithoutPrefix().getQueryOrEmpty().get(key)
  default @Nullable QueryParameter queryParameter(String key) {
    PathAndQuery pathAndQuery = getPathAndQueryWithoutPrefix();
    Query query = pathAndQuery.getQueryOrEmpty();
    return new QueryParameter(key, query.getDecoded(key));
  }

  FormParameter formParameter(String key);

  Map<String, FormParameter> formParameters();

  Map<String, Cookie> getCookies();

  byte[] getBody();

  String getBodyAsString();

  String getBodyAsBase64();

  boolean isMultipart();

  Collection<Part> getParts();

  Part getPart(String name);

  boolean isBrowserProxyRequest();

  Optional<Request> getOriginalRequest();

  String getProtocol();
}
