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

/** The type Equal to json pattern. */
public class EqualToJsonPattern extends StringValuePattern {

  private final JsonNode expected;
  private final Boolean ignoreArrayOrder;
  private final Boolean ignoreExtraElements;
  private final Boolean serializeAsString;

  /**
   * Instantiates a new Equal to json pattern.
   *
   * @param json the json
   * @param ignoreArrayOrder the ignore array order
   * @param ignoreExtraElements the ignore extra elements
   */
  public EqualToJsonPattern(
      @JsonProperty("equalToJson") String json,
      @JsonProperty("ignoreArrayOrder") Boolean ignoreArrayOrder,
      @JsonProperty("ignoreExtraElements") Boolean ignoreExtraElements) {
    super(json);
    expected = Json.read(json, JsonNode.class);
    this.ignoreArrayOrder = ignoreArrayOrder;
    this.ignoreExtraElements = ignoreExtraElements;
    this.serializeAsString = true;
  }

  /**
   * Instantiates a new Equal to json pattern.
   *
   * @param jsonNode the json node
   * @param ignoreArrayOrder the ignore array order
   * @param ignoreExtraElements the ignore extra elements
   */
  public EqualToJsonPattern(
      JsonNode jsonNode, Boolean ignoreArrayOrder, Boolean ignoreExtraElements) {
    super(Json.write(jsonNode));
    expected = jsonNode;
    this.ignoreArrayOrder = ignoreArrayOrder;
    this.ignoreExtraElements = ignoreExtraElements;
    this.serializeAsString = false;
  }

  @Override
  public MatchResult match(String value) {
    final CountingDiffListener diffListener = new CountingDiffListener();
    Configuration diffConfig =
        Configuration.empty()
            .withDifferenceListener(diffListener)
            .withNumberComparator(new NormalisedNumberComparator());

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

  /**
   * Gets serialized equal to json.
   *
   * @return the serialized equal to json
   */
  @JsonProperty("equalToJson")
  public Object getSerializedEqualToJson() {
    return serializeAsString ? getValue() : Json.read(getValue(), JsonNode.class);
  }

  /**
   * Gets equal to json.
   *
   * @return the equal to json
   */
  public String getEqualToJson() {
    return expectedValue;
  }

  private boolean shouldIgnoreArrayOrder() {
    return ignoreArrayOrder != null && ignoreArrayOrder;
  }

  /**
   * Is ignore array order boolean.
   *
   * @return the boolean
   */
  public Boolean isIgnoreArrayOrder() {
    return ignoreArrayOrder;
  }

  private boolean shouldIgnoreExtraElements() {
    return ignoreExtraElements != null && ignoreExtraElements;
  }

  /**
   * Is ignore extra elements boolean.
   *
   * @return the boolean
   */
  public Boolean isIgnoreExtraElements() {
    return ignoreExtraElements;
  }

  @Override
  public String getExpected() {
    return Json.prettyPrint(getValue());
  }

  private static class CountingDiffListener implements DifferenceListener {

    /** The Count. */
    public int count = 0;

    /**
     * Diff.
     *
     * @param difference the difference
     * @param context the context
     */
    @Override
    public void diff(Difference difference, DifferenceContext context) {
      final int delta = maxDeepSize(difference.getExpected(), difference.getActual());
      count += delta == 0 ? 1 : Math.abs(delta);
    }
  }

  /**
   * Max deep size int.
   *
   * @param one the one
   * @param two the two
   * @return the int
   */
  public static int maxDeepSize(Object one, Object two) {
    return Math.max(one != null ? deepSize(one) : 0, two != null ? deepSize(two) : 0);
  }

  private static int deepSize(Object nodeObj) {
    JsonNode jsonNode = Json.getObjectMapper().convertValue(nodeObj, JsonNode.class);
    return Json.deepSize(jsonNode);
  }
}
