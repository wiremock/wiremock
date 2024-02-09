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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.http.HttpHeader.absent;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.http.QueryParameter.queryParam;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class MultiValuePatternTest {

  public static final String EXPECTED_DATE_TIME = "2024-01-01T00:00:00.000Z";
  public static final String ACTUAL_DATE_TIME_ONE_MILLI_EARLIER = "2023-12-31T23:59:59.999Z";
  public static final String ACTUAL_DATE_TIME_ONE_MILLI_LATER = "2024-01-01T00:00:00.001Z";
  public static final String ACTUAL_DATE_TIME_TWO_MILLIS_EARLIER = "2023-12-31T23:59:59.998Z";
  public static final String ACTUAL_DATE_TIME_TWO_MILLIS_LATER = "2024-01-01T00:00:00.002Z";
  public static final String ACTUAL_DATE_TIME_ONE_DAY_EARLIER = "2023-12-31T00:00:00.000Z";
  public static final String ACTUAL_DATE_TIME_ONE_DAY_LATER = "2024-01-02T00:00:00.000Z";
  public static final String ACTUAL_DATE_TIME_ONE_YEAR_EARLIER = "2023-01-01T00:00:00.000Z";
  public static final String ACTUAL_DATE_TIME_ONE_YEAR_LATER = "2025-01-01T00:00:00.000Z";
  public static final String ACTUAL_DATE_TIME_ONE_CENTURY_EARLIER = "1924-01-01T00:00:00.000Z";
  public static final String ACTUAL_DATE_TIME_ONE_CENTURY_LATER = "2124-01-01T00:00:00.000Z";
  public static final String ACTUAL_DATE_TIME_ONE_MILLENIUM_EARLIER = "1024-01-01T00:00:00.000Z";
  public static final String ACTUAL_DATE_TIME_ONE_MILLENIUM_LATER = "3024-01-01T00:00:00.000Z";

  @Test
  public void returnsExactMatchForAbsentHeaderWhenRequiredAbsent() {
    assertTrue(MultiValuePattern.absent().match(HttpHeader.absent("any-key")).isExactMatch());
  }

  @Test
  public void returnsNonMatchForPresentHeaderWhenRequiredAbsent() {
    assertFalse(
        MultiValuePattern.absent().match(httpHeader("the-key", "the value")).isExactMatch());
  }

  @Test
  public void returnsNonMatchForAbsentHeaderWhenRequiredBeforeNow() {
    assertFalse(
        MultiValuePattern.of(beforeNow()).match(HttpHeader.absent("any-key")).isExactMatch());
  }

  @Test
  public void returnsExactMatchForPresentHeaderWhenRequiredPresent() {
    assertTrue(
        MultiValuePattern.of(equalTo("required-value"))
            .match(httpHeader("the-key", "required-value"))
            .isExactMatch());
  }

  @Test
  public void returnsNonMatchForAbsentHeaderWhenRequiredPresent() {
    MatchResult matchResult =
        MultiValuePattern.of(equalTo("required-value")).match(absent("the-key"));

    assertFalse(matchResult.isExactMatch());
    assertThat(matchResult.getDistance(), is(1.0));
  }

  @Test
  public void returnsNonZeroDistanceWhenHeaderValuesAreSimilar() {
    assertThat(
        MultiValuePattern.of(equalTo("required-value"))
            .match(httpHeader("any-key", "require1234567"))
            .getDistance(),
        is(0.5));
  }

  @Test
  public void returnsTheBestMatchWhenSeveralHeaderValuesAreAvailableAndNoneAreExact() {
    assertThat(
        MultiValuePattern.of(equalTo("required-value"))
            .match(httpHeader("any-key", "require1234567", "requi12345", "1234567rrrr"))
            .getDistance(),
        is(0.5));
  }

  @Test
  public void returnsTheBestMatchWhenSeveralHeaderDateTimeValuesAreAvailableAndNoneAreExact() {
    double distanceOfOneMilliDifference =
        MultiValuePattern.of(equalToDateTime(EXPECTED_DATE_TIME))
            .match(
                httpHeader(
                    "any-key",
                    ACTUAL_DATE_TIME_ONE_MILLI_EARLIER,
                    ACTUAL_DATE_TIME_ONE_MILLI_LATER))
            .getDistance();
    double distanceOfGreaterThanOneMilliDifference =
        MultiValuePattern.of(equalToDateTime(EXPECTED_DATE_TIME))
            .match(
                httpHeader(
                    "any-key",
                    ACTUAL_DATE_TIME_TWO_MILLIS_EARLIER,
                    ACTUAL_DATE_TIME_TWO_MILLIS_LATER,
                    ACTUAL_DATE_TIME_ONE_DAY_EARLIER,
                    ACTUAL_DATE_TIME_ONE_DAY_LATER,
                    ACTUAL_DATE_TIME_ONE_YEAR_EARLIER,
                    ACTUAL_DATE_TIME_ONE_YEAR_LATER,
                    ACTUAL_DATE_TIME_ONE_CENTURY_EARLIER,
                    ACTUAL_DATE_TIME_ONE_CENTURY_LATER,
                    ACTUAL_DATE_TIME_ONE_MILLENIUM_EARLIER,
                    ACTUAL_DATE_TIME_ONE_MILLENIUM_LATER))
            .getDistance();
    assertThat(distanceOfOneMilliDifference, lessThan(distanceOfGreaterThanOneMilliDifference));
  }

  @Test
  public void returnsTheBestMatchWhenSeveralHeaderValuesAreAvailableAndOneIsExact() {
    assertTrue(
        MultiValuePattern.of(equalTo("required-value"))
            .match(httpHeader("any-key", "require1234567", "required-value", "1234567rrrr"))
            .isExactMatch());
  }

  @Test
  public void returnsTheBestMatchWhenSeveralHeaderDateTimeValuesAreAvailableAndOneIsExact() {
    assertTrue(
        MultiValuePattern.of(equalToDateTime(EXPECTED_DATE_TIME))
            .match(
                httpHeader(
                    "any-key",
                    ACTUAL_DATE_TIME_ONE_MILLI_EARLIER,
                    ACTUAL_DATE_TIME_ONE_MILLI_LATER,
                    ACTUAL_DATE_TIME_TWO_MILLIS_EARLIER,
                    ACTUAL_DATE_TIME_TWO_MILLIS_LATER,
                    ACTUAL_DATE_TIME_ONE_DAY_EARLIER,
                    ACTUAL_DATE_TIME_ONE_DAY_LATER,
                    ACTUAL_DATE_TIME_ONE_YEAR_EARLIER,
                    ACTUAL_DATE_TIME_ONE_YEAR_LATER,
                    ACTUAL_DATE_TIME_ONE_CENTURY_EARLIER,
                    ACTUAL_DATE_TIME_ONE_CENTURY_LATER,
                    ACTUAL_DATE_TIME_ONE_MILLENIUM_EARLIER,
                    ACTUAL_DATE_TIME_ONE_MILLENIUM_LATER,
                    EXPECTED_DATE_TIME))
            .isExactMatch());
  }

  @Test
  public void returnsTheBestMatchWhenSeveralQueryParamValuesAreAvailableAndOneIsExact() {
    assertTrue(
        MultiValuePattern.of(equalTo("required-value"))
            .match(queryParam("any-key", "require1234567", "required-value", "1234567rrrr"))
            .isExactMatch());
  }

  @Test
  public void correctlyRendersEqualToAsJson() throws Exception {
    String actual = Json.write(MultiValuePattern.of(equalTo("something")));
    JSONAssert.assertEquals(
        "{                              \n" + "  \"equalTo\": \"something\"   \n" + "}",
        actual,
        true);
  }

  @Test
  public void correctlyRendersAbsentAsJson() throws Exception {
    String actual = Json.write(MultiValuePattern.absent());
    JSONAssert.assertEquals(
        "{                   \n" + "  \"absent\": true   \n" + "}", actual, true);
  }
}
