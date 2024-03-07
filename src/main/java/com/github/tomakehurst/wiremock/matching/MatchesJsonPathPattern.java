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
package com.github.tomakehurst.wiremock.matching;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.common.RequestCache.Key.keyFor;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.ListOrSingle;
import com.github.tomakehurst.wiremock.common.RequestCache;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.security.MessageDigest;
import java.util.*;

@JsonSerialize(using = JsonPathPatternJsonSerializer.class)
public class MatchesJsonPathPattern extends PathPattern {

  private static final Configuration JSON_PATH_CONF =
          Configuration.builder()
                  .jsonProvider(new JacksonJsonNodeJsonProvider())
                  .mappingProvider(new JacksonMappingProvider())
                  .build();

  private final JsonPath jsonPath;

  public MatchesJsonPathPattern(
      @JsonProperty("matchesJsonPath") String expectedJsonPath, StringValuePattern valuePattern) {
    super(expectedJsonPath, valuePattern);
    jsonPath = JsonPath.compile(expectedJsonPath);
  }

  public MatchesJsonPathPattern(String value) {
    this(value, null);
  }

  public String getMatchesJsonPath() {
    return expectedValue;
  }

  protected MatchResult isSimpleMatch(String value) {
    // For performance reason, don't try to parse XML value
    if (value != null && value.trim().startsWith("<")) {
      final String message =
          String.format(
              "Warning: JSON path expression failed to match document '%s' because it's not JSON but probably XML",
              value);
      notifier().info(message);
      return MatchResult.noMatch(SubEvent.warning(message));
    }
    try {
      Object obj = evaluateJsonPath(value);

      boolean result;
      if (obj instanceof Collection) {
        result = !((Collection<?>) obj).isEmpty();
      } else if (obj instanceof Map) {
        result = !((Map<?, ?>) obj).isEmpty();
      } else {
        result = obj != null;
      }

      return MatchResult.of(result);
    } catch (Exception e) {
      String error;
      if (e.getMessage().equalsIgnoreCase("invalid path")) {
        error = "the JSON path didn't match the document structure";
      } else if (e.getMessage().equalsIgnoreCase("invalid container object")) {
        error = "the JSON document couldn't be parsed";
      } else {
        error = "of error '" + e.getMessage() + "'";
      }

      String message =
          String.format(
              "Warning: JSON path expression failed to match document '%s' because %s",
              value, error);

      return MatchResult.noMatch(SubEvent.warning(message));
    }
  }

  protected MatchResult isAdvancedMatch(String value) {
    try {
      ListOrSingle<String> expressionResult = getExpressionResult(value);

      // Bit of a hack, but otherwise empty array results aren't matched as absent()
      if ((expressionResult == null || expressionResult.isEmpty())
          && AbsentPattern.class.isAssignableFrom(valuePattern.getClass())) {
        expressionResult = ListOrSingle.of((String) null);
      }

      final List<MatchResult> matchResults =
          expressionResult.stream().map(valuePattern::match).collect(toList());
      final List<SubEvent> subEvents =
          matchResults.stream()
              .map(MatchResult::getSubEvents)
              .flatMap(Collection::stream)
              .collect(toList());

      return matchResults.stream()
          .min(Comparator.comparingDouble(MatchResult::getDistance))
          .orElseGet(() -> MatchResult.noMatch(subEvents));
    } catch (SubExpressionException e) {
      return MatchResult.noMatch(SubEvent.warning(e.getMessage()));
    }
  }

  @Override
  public ListOrSingle<String> getExpressionResult(final String value) {
    // For performance reason, don't try to parse XML value
    if (value != null && value.trim().startsWith("<")) {
      final String message =
          String.format(
              "Warning: JSON path expression '%s' failed to match document '%s' because it's not JSON but probably XML",
              expectedValue, value);

      throw new SubExpressionException(message);
    }

    Object obj = null;
    try {
      obj = evaluateJsonPath(value);
    } catch (PathNotFoundException ignored) {
    } catch (Exception e) {
      String error;
      if (e.getMessage().equalsIgnoreCase("invalid container object")) {
        error = "the JSON document couldn't be parsed";
      } else {
        error = "of error '" + e.getMessage() + "'";
      }

      String message =
          String.format(
              "Warning: JSON path expression '%s' failed to match document '%s' because %s",
              expectedValue, value, error);

      throw new SubExpressionException(message, e);
    }

    ListOrSingle<String> expressionResult;
    if (obj instanceof Map
        || (obj instanceof List
            && EqualToJsonPattern.class.isAssignableFrom(valuePattern.getClass()))) {
      expressionResult = ListOrSingle.of(Json.write(obj));
    } else if (obj instanceof List) {
      final List<String> stringValues =
          ((List<?>) obj).stream().map(Object::toString).collect(toList());
      expressionResult = ListOrSingle.of(stringValues);
    } else if (obj instanceof Number || obj instanceof String || obj instanceof Boolean) {
      expressionResult = ListOrSingle.of(String.valueOf(obj));
    } else {
      expressionResult = ListOrSingle.of();
    }

    return expressionResult;
  }

  private Object evaluateJsonPath(String value) {
    final RequestCache requestCache = RequestCache.getCurrent();
    final MessageDigest md = Exceptions.uncheck(() -> MessageDigest.getInstance("MD5"), MessageDigest.class);
    md.update(value.getBytes());
    final byte[] valueDigest = md.digest();

    final JsonNode jsonNode = requestCache.get(
            keyFor(JsonNode.class, "requestBody", valueDigest),
            () -> Json.read(value, JsonNode.class)
    );

    return requestCache.get(
            keyFor(JsonNode.class, "jsonPathResult", expectedValue, valueDigest),
            () -> {
              final JsonNode resultNode = jsonPath.read(jsonNode, JSON_PATH_CONF);
              return Json.getObjectMapper().convertValue(resultNode, Object.class);
            });
  }
}
