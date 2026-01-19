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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = HttpHeadersJsonSerializer.class)
@JsonDeserialize(using = HttpHeadersJsonDeserializer.class)
public class HttpHeaders {

  private final Multimap<CaseInsensitiveKey, String> headers;

  public HttpHeaders() {
    headers = ImmutableMultimap.of();
  }

  public HttpHeaders(HttpHeader... headers) {
    this(Arrays.asList(headers));
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

  private HttpHeaders(Multimap<CaseInsensitiveKey, String> headers) {
    this.headers = ImmutableMultimap.copyOf(headers);
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

  public HttpHeaders transform(Consumer<Builder> transformer) {
    final Builder builder = new Builder(this);
    transformer.accept(builder);
    return builder.build();
  }

  public int size() {
    return headers.asMap().size();
  }

  public HttpHeaders plus(HttpHeader... additionalHeaders) {
    List<HttpHeader> httpHeaders = new ArrayList<>(all());
    httpHeaders.addAll(asList(additionalHeaders));
    return new HttpHeaders(httpHeaders);
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

  @SuppressWarnings("UnusedReturnValue")
  public static class Builder {
    private final ListMultimap<CaseInsensitiveKey, String> headers =
        MultimapBuilder.linkedHashKeys().arrayListValues().build();

    public Builder() {}

    public Builder(HttpHeaders httpHeaders) {
      headers.putAll(httpHeaders.headers);
    }

    public Builder setAll(HttpHeader... headers) {
      return setAll(List.of(headers));
    }

    public Builder setAll(Iterable<HttpHeader> headers) {
      removeAll();
      for (HttpHeader header : headers) {
        add(header.caseInsensitiveKey(), header.values());
      }
      return this;
    }

    public Builder addAll(HttpHeader... headers) {
      return addAll(List.of(headers));
    }

    public Builder addAll(Iterable<HttpHeader> headers) {
      for (HttpHeader header : headers) {
        add(header.caseInsensitiveKey(), header.values());
      }
      return this;
    }

    public Builder set(String key, String... values) {
      return set(new CaseInsensitiveKey(key), values);
    }

    public Builder set(CaseInsensitiveKey key, String... values) {
      set(key, List.of(values));
      return this;
    }

    public Builder set(String key, Iterable<String> values) {
      return set(new CaseInsensitiveKey(key), values);
    }

    public Builder set(CaseInsensitiveKey key, Iterable<String> values) {
      headers.replaceValues(key, values);
      return this;
    }

    public Builder add(String key, String... values) {
      add(new CaseInsensitiveKey(key), values);
      return this;
    }

    public Builder add(CaseInsensitiveKey key, String... values) {
      add(key, List.of(values));
      return this;
    }

    public Builder add(String key, Iterable<String> values) {
      add(new CaseInsensitiveKey(key), values);
      return this;
    }

    public Builder add(CaseInsensitiveKey key, Iterable<String> values) {
      headers.putAll(key, values);
      return this;
    }

    public Builder remove(String key) {
      return remove(new CaseInsensitiveKey(key));
    }

    public Builder remove(CaseInsensitiveKey key) {
      headers.removeAll(key);
      return this;
    }

    public Builder removeAll() {
      headers.clear();
      return this;
    }

    public List<String> get(String key) {
      return get(new CaseInsensitiveKey(key));
    }

    public List<String> get(CaseInsensitiveKey key) {
      return headers.get(key);
    }

    public Set<CaseInsensitiveKey> keys() {
      return headers.keySet();
    }

    public HttpHeaders build() {
      return new HttpHeaders(headers);
    }
  }
}
