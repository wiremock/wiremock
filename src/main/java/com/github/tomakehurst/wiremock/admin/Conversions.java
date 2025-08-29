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
package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.InvalidInputException;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * A utility class providing static methods to convert data types.
 *
 * <p>This class is used within the admin context to safely convert {@link QueryParameter} values
 * into other common types like {@code Integer} and {@code Date}.
 */
public class Conversions {

  /**
   * Converts a query parameter to an {@link Integer}.
   *
   * @param parameter The query parameter to convert.
   * @return The integer value of the parameter, or {@code null} if the parameter is not present.
   */
  public static Integer toInt(QueryParameter parameter) {
    return parameter.isPresent() ? Integer.valueOf(parameter.firstValue()) : null;
  }

  /**
   * Converts a query parameter to a {@link Date}.
   *
   * <p>The parameter's value is expected to be a string in ISO 8601 date-time format.
   *
   * @param parameter The query parameter to convert.
   * @return The parsed {@code Date}, or {@code null} if the parameter is not present.
   * @throws InvalidInputException if the parameter's value is not a valid ISO 8601 date-time
   *     string.
   */
  public static Date toDate(QueryParameter parameter) {
    try {
      return parameter.isPresent()
          ? Date.from(ZonedDateTime.parse(parameter.firstValue()).toInstant())
          : null;
    } catch (DateTimeParseException e) {
      throw new InvalidInputException(
          Errors.validation(
              parameter.key(), parameter.firstValue() + " is not a valid ISO8601 date"));
    }
  }
}
