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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.InvalidInputException;
import com.github.tomakehurst.wiremock.common.Json;
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

  public static Class<?> toJsonView(Query query) {
    QueryParamValue parameter = query.getFirst("format");
    if (parameter != null && "v4".equals(parameter.decode())) {
      return Json.V4StyleView.class;
    }
    return Json.PublicView.class;
  }

  public static Class<?> detectJsonViewFromRequestBody(String requestBody) {
    JsonNode root = Json.node(requestBody);
    JsonNode responseNode = root.path("response");
    JsonNode bodyNode = responseNode.path("body");
    if (bodyNode.isObject()) {
      return Json.V4StyleView.class;
    }
    return Json.PublicView.class;
  }

  public static Date toDate(Query query, String key) {
    QueryParamValue parameter = query.getFirst(key);
    try {
      return parameter != null
          ? Date.from(ZonedDateTime.parse(parameter.decode()).toInstant())
          : null;
    } catch (DateTimeParseException e) {
      throw new InvalidInputException(
          Errors.validation(key, parameter.decode() + " is not a valid ISO8601 date"));
    }
  }
}
