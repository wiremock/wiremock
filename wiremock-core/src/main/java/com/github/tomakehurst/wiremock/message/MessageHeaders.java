/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.message;

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static java.util.Arrays.asList;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.wiremock.annotations.PublishedAPI;

@PublishedAPI
@JsonSerialize(using = MessageHeadersJsonSerializer.class)
@JsonDeserialize(using = MessageHeadersJsonDeserializer.class)
public class MessageHeaders {

  public static final MessageHeaders NO_HEADERS = new MessageHeaders();

  private final Multimap<CaseInsensitiveKey, String> headers;

  public MessageHeaders() {
    headers = ImmutableMultimap.of();
  }

  public MessageHeaders(MessageHeader... headers) {
    this(asList(headers));
  }

  public MessageHeaders(Iterable<MessageHeader> headers) {
    ImmutableMultimap.Builder<CaseInsensitiveKey, String> builder = ImmutableMultimap.builder();
    for (MessageHeader header : getFirstNonNull(headers, Collections.<MessageHeader>emptyList())) {
      builder.putAll(header.caseInsensitiveKey(), header.values());
    }

    this.headers = builder.build();
  }

  private MessageHeaders(Multimap<CaseInsensitiveKey, String> headers) {
    this.headers = ImmutableMultimap.copyOf(headers);
  }

  public static MessageHeaders noHeaders() {
    return NO_HEADERS;
  }

  public MessageHeader getHeader(String key) {
    if (!headers.containsKey(new CaseInsensitiveKey(key))) {
      return MessageHeader.absent(key);
    }

    Collection<String> values = headers.get(new CaseInsensitiveKey(key));
    return new MessageHeader(key, values);
  }

  public @Nullable String getFirstValue(String key) {
    MessageHeader header = getHeader(key);
    return header.isPresent() ? header.firstValue() : null;
  }

  public Collection<MessageHeader> all() {
    List<MessageHeader> headerList = new ArrayList<>();
    for (CaseInsensitiveKey key : headers.keySet()) {
      headerList.add(new MessageHeader(key.value(), headers.get(key)));
    }

    return headerList;
  }

  public Set<String> keys() {
    return headers.keySet().stream().map(CaseInsensitiveKey::toString).collect(Collectors.toSet());
  }

  public int size() {
    return headers.asMap().size();
  }

  public boolean isEmpty() {
    return headers.isEmpty();
  }

  public MessageHeaders plus(MessageHeader... additionalHeaders) {
    List<MessageHeader> combined = new ArrayList<>(all());
    combined.addAll(asList(additionalHeaders));
    return new MessageHeaders(combined);
  }

  public MessageHeaders withReplaced(MessageHeader replacement) {
    List<MessageHeader> remaining = new ArrayList<>();
    for (MessageHeader header : all()) {
      if (!header.keyEquals(replacement.key())) {
        remaining.add(header);
      }
    }
    remaining.add(replacement);
    return new MessageHeaders(remaining);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MessageHeaders that = (MessageHeaders) o;

    return Objects.equals(headers, that.headers);
  }

  @Override
  public int hashCode() {
    return headers.hashCode();
  }

  @Override
  public String toString() {
    if (headers.isEmpty()) {
      return "(no headers)";
    }

    StringBuilder outString = new StringBuilder();
    for (CaseInsensitiveKey key : headers.keySet()) {
      outString.append(key).append(": ").append(headers.get(key)).append("\n");
    }

    return outString.toString();
  }
}
