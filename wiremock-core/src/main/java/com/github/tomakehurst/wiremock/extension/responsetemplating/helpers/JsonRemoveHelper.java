/*
 * Copyright (C) 2024-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidModificationException;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.PathNotFoundException;

class JsonRemoveHelper extends HandlebarsHelper<Object> {

  private final ParseContext parseContext = JsonPath.using(HelperUtils.jsonPathConfig);

  @Override
  public String apply(Object inputJson, Options options) {
    if (!(inputJson instanceof String)) {
      return handleError("Input JSON must be a string");
    }
    if (inputJson.equals("null")) {
      // No op
      return (String) inputJson;
    }
    if (options.params.length != 1) {
      return handleError("A single JSONPath expression parameter must be supplied");
    }
    Object jsonPathString = options.param(0);
    if (!(jsonPathString instanceof String)) {
      return handleError("JSONPath parameter must be a string");
    }
    DocumentContext jsonDocument;
    try {
      jsonDocument = parseContext.parse((String) inputJson);
    } catch (Exception e) {
      return handleError("Input JSON string is not valid JSON ('" + inputJson + "')", e);
    }
    try {
      if (((String) jsonPathString).isEmpty())
        throw new InvalidPathException("JSONPath expression is empty");
      return jsonDocument.delete((String) jsonPathString).jsonString();
    } catch (PathNotFoundException e) {
      return (String) inputJson;
    } catch (InvalidPathException e) {
      String message =
          "JSONPath parameter is not a valid JSONPath expression ('" + jsonPathString + "')";
      return handleError(message, e);
    } catch (InvalidModificationException e) {
      String message =
          "Delete operation cannot be applied to JSONPath expression ('" + jsonPathString + "')";
      return handleError(message, e);
    }
  }
}
