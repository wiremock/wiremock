/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.stubbing.SubEvent.JSON_ERROR;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.JsonException;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.listener.Difference;
import net.javacrumbs.jsonunit.core.listener.DifferenceContext;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;

public class EqualToJsonPattern extends RequestBodyEqualToPattern<JsonNode> {

  private final JsonNode expected;
  private final Boolean ignoreArrayOrder;
  private final Boolean ignoreExtraElements;
  private final Boolean serializeAsString;

  public EqualToJsonPattern(
      @JsonProperty("equalToJson") String json,
      @JsonProperty("source") ExpectedSource source,
      @JsonProperty("ignoreArrayOrder") Boolean ignoreArrayOrder,
      @JsonProperty("ignoreExtraElements") Boolean ignoreExtraElements) {
    this(json, source, ignoreArrayOrder, ignoreExtraElements, true);
  }

  public EqualToJsonPattern(String json, Boolean ignoreArrayOrder, Boolean ignoreExtraElements) {
    this(json, ExpectedSource.RAW, ignoreExtraElements, ignoreExtraElements, true);
  }

  public EqualToJsonPattern(
      JsonNode jsonNode, Boolean ignoreArrayOrder, Boolean ignoreExtraElements) {
    this(Json.write(jsonNode), null, ignoreExtraElements, ignoreExtraElements, false);
  }

  private EqualToJsonPattern(
      String json,
      ExpectedSource source,
      Boolean ignoreArrayOrder,
      Boolean ignoreExtraElements,
      Boolean serializeAsString) {
    super(json, source);

    if (ExpectedSource.FILE == source) {
      expected = null;
    } else {
      expected = Json.read(json, JsonNode.class);
    }

    this.ignoreArrayOrder = ignoreArrayOrder;
    this.ignoreExtraElements = ignoreExtraElements;
    this.serializeAsString = serializeAsString;
  }

  @Override
  public MatchResult match(String value) {
    final CountingDiffListener diffListener = new CountingDiffListener();
    Configuration diffConfig = Configuration.empty().withDifferenceListener(diffListener);

    if (shouldIgnoreArrayOrder()) {
      diffConfig = diffConfig.withOptions(Option.IGNORING_ARRAY_ORDER);
    }

    if (shouldIgnoreExtraElements()) {
      diffConfig =
          diffConfig.withOptions(Option.IGNORING_EXTRA_ARRAY_ITEMS, Option.IGNORING_EXTRA_FIELDS);
    }

    final JsonNode actual;
    final Diff diff;
    try {
      actual = Json.read(value, JsonNode.class);
      diff =
          Diff.create(
              expected, // JsonUnit knows how to work with JsonNode
              actual,
              "",
              "",
              diffConfig);
    } catch (JsonException je) {
      return MatchResult.noMatch(new SubEvent(JSON_ERROR, je.getErrors()));
    } catch (Exception e) {
      return MatchResult.noMatch(SubEvent.warning(e.getMessage()));
    }

    return new MatchResult() {
      @Override
      public boolean isExactMatch() {
        return diff.similar();
      }

      @Override
      public double getDistance() {
        diff.similar();
        double maxNodes = maxDeepSize(expected, actual);
        return diffListener.count / maxNodes;
      }
    };
  }

  @JsonProperty("equalToJson")
  public Object getSerializedEqualToJson() {
    return serializeAsString ? getValue() : Json.read(getValue(), JsonNode.class);
  }

  public String getEqualToJson() {
    return expectedValue;
  }

  private boolean shouldIgnoreArrayOrder() {
    return ignoreArrayOrder != null && ignoreArrayOrder;
  }

  public Boolean isIgnoreArrayOrder() {
    return ignoreArrayOrder;
  }

  private boolean shouldIgnoreExtraElements() {
    return ignoreExtraElements != null && ignoreExtraElements;
  }

  public Boolean isIgnoreExtraElements() {
    return ignoreExtraElements;
  }

  @Override
  public String getExpected() {
    // TODO:: Add READY CHECK ???
    if (expected != null) {
      return Json.prettyPrint(getValue());
    } else {
      return expectedValue;
    }
  }

  @Override
  public EqualToJsonPattern withExpected(String json) {
    return new EqualToJsonPattern(
        json, this.getSource(), ignoreExtraElements, ignoreExtraElements, true);
  }

  @Override
  public RequestBodyEqualToPattern<JsonNode> withSource(ExpectedSource source) {
    return new EqualToJsonPattern(
        this.getExpected(), source, ignoreExtraElements, ignoreExtraElements, true);
  }

  private static class CountingDiffListener implements DifferenceListener {

    public int count = 0;

    @Override
    public void diff(Difference difference, DifferenceContext context) {
      final int delta = maxDeepSize(difference.getExpected(), difference.getActual());
      count += delta == 0 ? 1 : Math.abs(delta);
    }
  }

  public static int maxDeepSize(Object one, Object two) {
    return Math.max(one != null ? deepSize(one) : 0, two != null ? deepSize(two) : 0);
  }

  private static int deepSize(Object nodeObj) {
    JsonNode jsonNode = Json.getObjectMapper().convertValue(nodeObj, JsonNode.class);
    return Json.deepSize(jsonNode);
  }
}
