/*
 * Copyright (C) 2016-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.InvalidInputException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import org.wiremock.url.Query;
import org.wiremock.url.QueryParamValue;

public class Conversions {

  public static Integer toInt(Query query, String key) {
    QueryParamValue parameter = query.getFirst(key);
    return parameter != null ? Integer.valueOf(parameter.decode()) : null;
  }

  public static Date toDate(Query query, String key) {
    QueryParamValue parameter = query.getFirst(key);
    if (parameter == null) {
      return null;
    }

    String value = parameter.decode();
    try {
      return Date.from(ZonedDateTime.parse(value).toInstant());
    } catch (DateTimeParseException ignored) {
      // Fall through and try LocalDate
    }

    try {
      return Date.from(LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant());
    } catch (DateTimeParseException e) {
      throw new InvalidInputException(
          Errors.validation(
              key,
              "Invalid format for "
                  + key
                  + ", supported formats are `2025-07-24` and `2025-07-24T10:20:03Z`"));
    }
  }
}
