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
package com.github.tomakehurst.wiremock.common;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;

import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.RenderableDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.function.Function;

/** The enum Date time truncation. */
public enum DateTimeTruncation {
  /** First second of minute date time truncation. */
  FIRST_SECOND_OF_MINUTE(input -> input.truncatedTo(MINUTES)),
  /** First minute of hour date time truncation. */
  FIRST_MINUTE_OF_HOUR(input -> input.truncatedTo(HOURS)),
  /** First hour of day date time truncation. */
  FIRST_HOUR_OF_DAY(input -> input.truncatedTo(DAYS)),

  /** First day of month date time truncation. */
  FIRST_DAY_OF_MONTH(input -> input.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(DAYS)),
  /** First day of next month date time truncation. */
  FIRST_DAY_OF_NEXT_MONTH(
      input -> input.with(TemporalAdjusters.firstDayOfNextMonth()).truncatedTo(DAYS)),
  /** Last day of month date time truncation. */
  LAST_DAY_OF_MONTH(input -> input.with(TemporalAdjusters.lastDayOfMonth()).truncatedTo(DAYS)),

  /** First day of year date time truncation. */
  FIRST_DAY_OF_YEAR(input -> input.with(TemporalAdjusters.firstDayOfYear()).truncatedTo(DAYS)),
  /** First day of next year date time truncation. */
  FIRST_DAY_OF_NEXT_YEAR(
      input -> input.with(TemporalAdjusters.firstDayOfNextYear()).truncatedTo(DAYS)),
  /** Last day of year date time truncation. */
  LAST_DAY_OF_YEAR(input -> input.with(TemporalAdjusters.lastDayOfYear()).truncatedTo(DAYS));

  private final Function<ZonedDateTime, ZonedDateTime> fn;

  DateTimeTruncation(Function<ZonedDateTime, ZonedDateTime> fn) {
    this.fn = fn;
  }

  /**
   * Truncate zoned date time.
   *
   * @param input the input
   * @return the zoned date time
   */
  public ZonedDateTime truncate(ZonedDateTime input) {
    return input != null ? fn.apply(input) : null;
  }

  /**
   * Truncate date.
   *
   * @param input the input
   * @return the date
   */
  public Date truncate(Date input) {
    ZoneId zoneId = getTimezone(input);
    final ZonedDateTime zonedInput = input.toInstant().atZone(zoneId);
    final Date date = Date.from(truncate(zonedInput).toInstant());
    return new RenderableDate(date, null, zoneId);
  }

  private static ZoneId getTimezone(Date date) {
    if (date instanceof RenderableDate) {
      RenderableDate renderableDate = (RenderableDate) date;
      if (renderableDate.getTimezone() != null) {
        return renderableDate.getTimezone();
      }

      return ZoneId.systemDefault();
    }

    return UTC;
  }

  @Override
  public String toString() {
    return name().replace('_', ' ').toLowerCase();
  }

  /**
   * From string date time truncation.
   *
   * @param value the value
   * @return the date time truncation
   */
  public static DateTimeTruncation fromString(String value) {
    final String normalisedKey = value.toUpperCase().replace(' ', '_');
    return valueOf(normalisedKey);
  }
}
