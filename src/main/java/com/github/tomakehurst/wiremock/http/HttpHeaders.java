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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static java.util.Arrays.asList;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonSerialize(using = HttpHeadersJsonSerializer.class)
@JsonDeserialize(using = HttpHeadersJsonDeserializer.class)
public class HttpHeaders {

  private final Multimap<CaseInsensitiveKey, String> headers;

  public HttpHeaders() {
    headers = ImmutableMultimap.of();
  }

  public HttpHeaders(HttpHeader... headers) {
    this(ImmutableList.copyOf(headers));
  }

  public HttpHeaders(Iterable<HttpHeader> headers) {
    ImmutableMultimap.Builder<CaseInsensitiveKey, String> builder = ImmutableMultimap.builder();
    for (HttpHeader header : getFirstNonNull(headers, Collections.<HttpHeader>emptyList())) {
      builder.putAll(caseInsensitive(header.key()), header.values());
    }

    this.headers = builder.build();
  }

  public HttpHeaders(HttpHeaders headers) {
    this(headers.all());
  }

  public static HttpHeaders noHeaders() {
    return new HttpHeaders();
  }

  public HttpHeader getHeader(String key) {
    if (!headers.containsKey(caseInsensitive(key))) {
      return HttpHeader.absent(key);
    }

    Collection<String> values = headers.get(caseInsensitive(key));
    return new HttpHeader(key, values);
  }

  public ContentTypeHeader getContentTypeHeader() {
    HttpHeader header = getHeader(ContentTypeHeader.KEY);
    if (header.isPresent()) {
      return new ContentTypeHeader(header.firstValue());
    }

    return ContentTypeHeader.absent();
  }

  public Collection<HttpHeader> all() {
    List<HttpHeader> httpHeaderList = new ArrayList<>();
    for (CaseInsensitiveKey key : headers.keySet()) {
      httpHeaderList.add(new HttpHeader(key.value(), headers.get(key)));
    }

    return httpHeaderList;
  }

  public Set<String> keys() {
    return headers.keySet().stream().map(CaseInsensitiveKey::toString).collect(Collectors.toSet());
  }

  public static HttpHeaders copyOf(HttpHeaders source) {
    return new HttpHeaders(source);
  }

  public int size() {
    return headers.asMap().size();
  }

  public HttpHeaders plus(HttpHeader... additionalHeaders) {
    return new HttpHeaders(
        ImmutableList.<HttpHeader>builder()
            .addAll(all())
            .addAll(asList(additionalHeaders))
            .build());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HttpHeaders that = (HttpHeaders) o;

    return headers != null ? headers.equals(that.headers) : that.headers == null;
  }

  @Override
  public int hashCode() {
    return headers != null ? headers.hashCode() : 0;
  }

  @Override
  public String toString() {
    if (headers.isEmpty()) {
      return "(no headers)\n";
    }

    StringBuilder outString = new StringBuilder();
    for (CaseInsensitiveKey key : headers.keySet()) {
      outString.append(key.toString()).append(": ").append(headers.get(key)).append("\n");
    }

    return outString.toString();
  }

  private CaseInsensitiveKey caseInsensitive(String key) {
    return new CaseInsensitiveKey(key);
  }
}
