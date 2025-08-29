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

/** The type Before date time pattern. */
public class BeforeDateTimePattern extends AbstractDateTimePattern {

  /**
   * Instantiates a new Before date time pattern.
   *
   * @param zonedDateTime the zoned date time
   */
  public BeforeDateTimePattern(ZonedDateTime zonedDateTime) {
    super(zonedDateTime);
  }

  /**
   * Instantiates a new Before date time pattern.
   *
   * @param localDateTime the local date time
   */
  public BeforeDateTimePattern(LocalDateTime localDateTime) {
    super(localDateTime);
  }

  /**
   * Instantiates a new Before date time pattern.
   *
   * @param dateTimeSpec the date time spec
   */
  public BeforeDateTimePattern(String dateTimeSpec) {
    super(dateTimeSpec);
  }

  /**
   * Instantiates a new Before date time pattern.
   *
   * @param dateTimeSpec the date time spec
   * @param actualDateFormat the actual date format
   * @param truncateExpected the truncate expected
   * @param truncateActual the truncate actual
   * @param applyTruncationLast the apply truncation last
   * @param expectedOffsetAmount the expected offset amount
   * @param expectedOffsetUnit the expected offset unit
   */
  public BeforeDateTimePattern(
      @JsonProperty("before") String dateTimeSpec,
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
        return zonedActual.isBefore(zonedExpected);
      }

      @Override
      protected boolean matchLocalLocal() {
        return localActual.isBefore(localExpected);
      }

      @Override
      protected boolean matchLocalZoned() {
        return zonedActual.toLocalDateTime().isBefore(localExpected);
      }

      @Override
      protected boolean matchZonedLocal() {
        return localActual.atZone(ZoneId.systemDefault()).isBefore(zonedExpected);
      }
    };
  }

  /**
   * Gets before.
   *
   * @return the before
   */
  public String getBefore() {
    return getValue();
  }
}
