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

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;

/** The enum Date time unit. */
public enum DateTimeUnit {
  /** Seconds date time unit. */
  SECONDS(Calendar.SECOND),
  /** Minutes date time unit. */
  MINUTES(Calendar.MINUTE),
  /** Hours date time unit. */
  HOURS(Calendar.HOUR),
  /** Days date time unit. */
  DAYS(Calendar.DAY_OF_MONTH),
  /** Months date time unit. */
  MONTHS(Calendar.MONTH),
  /** Years date time unit. */
  YEARS(Calendar.YEAR);

  private final int calendarField;

  DateTimeUnit(int calendarField) {
    this.calendarField = calendarField;
  }

  /**
   * To temporal unit temporal unit.
   *
   * @return the temporal unit
   */
  public TemporalUnit toTemporalUnit() {
    return ChronoUnit.valueOf(name());
  }
}
