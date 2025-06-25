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
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Conversions {

  public static Integer toInt(QueryParameter parameter) {
    return parameter.isPresent() ? Integer.valueOf(parameter.firstValue()) : null;
  }

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

  public static Predicate<ServeEvent> toPredicate(QueryParameter exclude, QueryParameter include) {

    return event -> {
      if (event.getRequest() == null || event.getRequest().getUrl() == null) {
        return false;
      }
      final String requestUrl = event.getRequest().getUrl();
      return filterQueryParam(requestUrl).test(include)
          && !filterQueryParam(requestUrl).test(exclude);
    };
  }

  private static Predicate<QueryParameter> filterQueryParam(String actual) {
    return queryParam -> {
      if (!queryParam.isPresent()) {
        return true;
      }
      return filterPredicates(actual).test(queryParam.getValues());
    };
  }

  private static Predicate<List<String>> filterPredicates(String actual) {
    return list -> {
      if (list == null || list.isEmpty()) {
        return true;
      }
      for (String item : list) {
        if (item.contains("|")) {
          for (String part : item.split(Pattern.quote("|"))) {
            if (actual.contains(part)) {
              return true;
            }
          }
        } else {
          if (actual.contains(item)) {
            return true;
          }
        }
      }
      return false;
    };
  }
}
