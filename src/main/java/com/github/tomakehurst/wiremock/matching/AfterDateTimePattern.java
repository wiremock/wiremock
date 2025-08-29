/*
 * Copyright (C) 2021-2025 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.DateTimeUnit;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** The type After date time pattern. */
public class AfterDateTimePattern extends AbstractDateTimePattern {

  /**
   * Instantiates a new After date time pattern.
   *
   * @param zonedDateTime the zoned date time
   */
  public AfterDateTimePattern(ZonedDateTime zonedDateTime) {
    super(zonedDateTime);
  }

  /**
   * Instantiates a new After date time pattern.
   *
   * @param localDateTime the local date time
   */
  public AfterDateTimePattern(LocalDateTime localDateTime) {
    super(localDateTime);
  }

  /**
   * Instantiates a new After date time pattern.
   *
   * @param dateTimeSpec the date time spec
   */
  public AfterDateTimePattern(String dateTimeSpec) {
    super(dateTimeSpec);
  }

  /**
   * Instantiates a new After date time pattern.
   *
   * @param dateTimeSpec the date time spec
   * @param actualDateFormat the actual date format
   * @param truncateExpected the truncate expected
   * @param truncateActual the truncate actual
   * @param applyTruncationLast the apply truncation last
   * @param expectedOffsetAmount the expected offset amount
   * @param expectedOffsetUnit the expected offset unit
   */
  public AfterDateTimePattern(
      @JsonProperty("after") String dateTimeSpec,
      @JsonProperty("actualFormat") String actualDateFormat,
      @JsonProperty("truncateExpected") String truncateExpected,
      @JsonProperty("truncateActual") String truncateActual,
      @JsonProperty("applyTruncationLast") boolean applyTruncationLast,
      @JsonProperty("expectedOffset") Integer expectedOffsetAmount,
      @JsonProperty("expectedOffsetUnit") DateTimeUnit expectedOffsetUnit) {
    super(
        dateTimeSpec,
        actualDateFormat,
        truncateExpected,
        truncateActual,
        applyTruncationLast,
        expectedOffsetAmount,
        expectedOffsetUnit);
  }

  @Override
  protected MatchResult getMatchResult(
      ZonedDateTime zonedExpected,
      LocalDateTime localExpected,
      ZonedDateTime zonedActual,
      LocalDateTime localActual) {

    return new AbstractDateTimeMatchResult(zonedExpected, localExpected, zonedActual, localActual) {
      @Override
      protected boolean matchZonedZoned() {
        return zonedActual.isAfter(zonedExpected);
      }

      @Override
      protected boolean matchLocalLocal() {
        return localActual.isAfter(localExpected);
      }

      @Override
      protected boolean matchLocalZoned() {
        return zonedActual.toLocalDateTime().isAfter(localExpected);
      }

      @Override
      protected boolean matchZonedLocal() {
        return localActual.atZone(ZoneId.systemDefault()).isAfter(zonedExpected);
      }
    };
  }

  /**
   * Gets after.
   *
   * @return the after
   */
  public String getAfter() {
    return getValue();
  }
}
