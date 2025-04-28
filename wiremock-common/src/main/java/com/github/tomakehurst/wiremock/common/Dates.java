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
package com.github.tomakehurst.wiremock.common;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class Dates {

  public static Date parse(String dateString) {
    try {
      return Date.from(ZonedDateTime.parse(dateString).toInstant());
    } catch (DateTimeParseException e) {
      return throwUnchecked(e, Date.class);
    }
  }

  public static String format(Date date) {
    return DateTimeFormatter.ISO_ZONED_DATE_TIME.format(date.toInstant().atZone(ZoneId.of("Z")));
  }
}
