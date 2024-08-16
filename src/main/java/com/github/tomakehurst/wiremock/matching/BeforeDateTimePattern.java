/*
 * Copyright (C) 2021-2024 Thomas Akehurst
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

public class BeforeDateTimePattern extends AbstractDateTimePattern {

  public BeforeDateTimePattern(ZonedDateTime zonedDateTime) {
    super(zonedDateTime);
  }

  public BeforeDateTimePattern(LocalDateTime localDateTime) {
    super(localDateTime);
  }

  public BeforeDateTimePattern(String dateTimeSpec) {
    super(dateTimeSpec);
  }

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

  public String getBefore() {
    return getValue();
  }
}
