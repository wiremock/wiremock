/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.Request;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MultipartValuePattern implements ValueMatcher<Request.Part> {

  public enum MatchingType {
    ALL,
    ANY
  }

  private final String name;
  private final String filename;
  private final Map<String, MultiValuePattern> headers;
  private final List<ContentPattern<?>> bodyPatterns;
  private final MatchingType matchingType;

  @JsonCreator
  public MultipartValuePattern(
      @JsonProperty("name") String name,
      @JsonProperty("fileName") String filename,
      @JsonProperty("matchingType") MatchingType type,
      @JsonProperty("headers") Map<String, MultiValuePattern> headers,
      @JsonProperty("bodyPatterns") List<ContentPattern<?>> body) {
    this.name = name;
    this.filename = filename;
    this.matchingType = type;
    this.headers = headers;
    this.bodyPatterns = body;
  }

  @JsonIgnore
  public boolean isMatchAny() {
    return matchingType == MatchingType.ANY;
  }

  @JsonIgnore
  public boolean isMatchAll() {
    return matchingType == MatchingType.ALL;
  }

  @Override
  public MatchResult match(final Request.Part value) {
    if (headers != null || bodyPatterns != null) {
      return MatchResult.aggregate(
          headers != null ? matchHeaderPatterns(value) : MatchResult.exactMatch(),
          bodyPatterns != null ? matchBodyPatterns(value) : MatchResult.exactMatch(),
          filename != null ? matchFileName(value) : MatchResult.exactMatch());
    }

    return MatchResult.exactMatch();
  }

  public MatchResult match(final Request request) {
    return isMatchAll() ? matchAllMultiparts(request) : matchAnyMultipart(request);
  }

  private MatchResult matchAllMultiparts(final Request request) {
    return request.getParts().stream()
            .allMatch(input -> MultipartValuePattern.this.match(input).isExactMatch())
        ? MatchResult.exactMatch()
        : MatchResult.noMatch();
  }

  private MatchResult matchAnyMultipart(final Request request) {
    Collection<Request.Part> parts = request.getParts();
    if (parts == null || parts.isEmpty()) {
      return MatchResult.noMatch();
    }

    return parts.stream().anyMatch(input -> MultipartValuePattern.this.match(input).isExactMatch())
        ? MatchResult.exactMatch()
        : MatchResult.noMatch();
  }

  public String getName() {
    return name;
  }

  public String getFileName() {
    return filename;
  }

  public Map<String, MultiValuePattern> getHeaders() {
    return headers;
  }

  public MatchingType getMatchingType() {
    return matchingType;
  }

  public List<ContentPattern<?>> getBodyPatterns() {
    return bodyPatterns;
  }

  private MatchResult matchFileName(final Request.Part part) {
    if (filename != null && !filename.isEmpty()) {
      return MatchResult.of(filename.equals(part.getFileName()));
    }
    return MatchResult.exactMatch();
  }

  private MatchResult matchHeaderPatterns(final Request.Part part) {
    if (headers != null && !headers.isEmpty()) {
      return MatchResult.aggregate(
          headers.entrySet().stream()
              .map(
                  headerPattern ->
                      headerPattern.getValue().match(part.getHeader(headerPattern.getKey())))
              .collect(Collectors.toList()));
    }

    return MatchResult.exactMatch();
  }

  private MatchResult matchBodyPatterns(final Request.Part value) {
    return MatchResult.aggregate(
        bodyPatterns.stream()
            .map(bodyPattern -> matchBody(value, bodyPattern))
            .collect(Collectors.toList()));
  }

  private static MatchResult matchBody(Request.Part part, ContentPattern<?> bodyPattern) {
    Body body = part.getBody();
    if (body == null) {
      return MatchResult.noMatch();
    }

    if (BinaryEqualToPattern.class.isAssignableFrom(bodyPattern.getClass())) {
      return ((BinaryEqualToPattern) bodyPattern).match(body.asBytes());
    }

    return ((StringValuePattern) bodyPattern).match(body.asString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MultipartValuePattern that = (MultipartValuePattern) o;

    return Objects.equals(name, that.name)
        && Objects.equals(filename, that.filename)
        && Objects.equals(headers, that.headers)
        && Objects.equals(bodyPatterns, that.bodyPatterns)
        && matchingType == that.matchingType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, filename, headers, bodyPatterns, matchingType);
  }
}
