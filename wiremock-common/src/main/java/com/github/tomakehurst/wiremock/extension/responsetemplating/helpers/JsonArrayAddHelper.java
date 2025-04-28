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
import com.github.jknack.handlebars.TagType;
import com.github.tomakehurst.wiremock.common.Json;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.PathNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

class JsonArrayAddHelper extends HandlebarsHelper<Object> {

  private final ParseContext parseContext = JsonPath.using(HelperUtils.jsonPathConfig);

  @Override
  public String apply(Object inputJson, Options options) throws IOException {
    if (!(inputJson instanceof String)) {
      return handleError("Base JSON must be a string");
    }

    DocumentContext root;
    try {
      root = parseContext.parse((String) inputJson);
    } catch (Exception e) {
      return handleError("Base JSON is not valid JSON ('" + inputJson + "')", e);
    }

    Object jsonPathString = options.hash.get("jsonPath");
    if (jsonPathString != null && !(jsonPathString instanceof String))
      return handleError("jsonPath option must be a string");

    Object currentList;
    if (jsonPathString == null) {
      currentList = root.json();
    } else {
      try {
        if (((String) jsonPathString).isEmpty())
          throw new InvalidPathException("JSONPath expression is empty");
        currentList = root.read((String) jsonPathString);
      } catch (PathNotFoundException e) {
        currentList = null;
      } catch (InvalidPathException e) {
        return handleError(
            "jsonPath option is not valid JSONPath expression ('" + jsonPathString + "')");
      }
    }
    if (!(currentList instanceof List)) {
      String detail;
      if (jsonPathString == null) {
        detail = "'" + inputJson + "'";
      } else {
        detail = "root: '" + inputJson + "', jsonPath: '" + jsonPathString + "'";
      }
      return handleError("Target JSON is not a JSON array (" + detail + ")");
    }

    Object itemToAddString;
    if (options.tagType == TagType.SECTION) {
      itemToAddString = options.fn().toString();
    } else {
      itemToAddString = options.params.length > 0 ? options.params[0] : null;
    }
    if (!(itemToAddString instanceof String)) {
      return handleError("Item-to-add JSON must be a string");
    }

    Object toAdd;
    try {
      toAdd = Json.read((String) itemToAddString, Object.class);
    } catch (Exception e) {
      return handleError("Item-to-add JSON is not valid JSON ('" + itemToAddString + "')", e);
    }
    boolean flatten;
    {
      Object flatten0 = options.hash.get("flatten");
      if (flatten0 instanceof Boolean) {
        flatten = (boolean) flatten0;
      } else if (flatten0 == null) {
        flatten = false;
      } else {
        return handleError("flatten option must be a boolean");
      }
    }
    if (flatten && toAdd instanceof Collection) {
      //noinspection rawtypes,unchecked
      ((List) currentList).addAll((Collection) toAdd);
    } else {
      //noinspection rawtypes,unchecked
      ((List) currentList).add(toAdd);
    }

    Object maxItems = options.hash.get("maxItems");
    if (maxItems != null) {
      if (!(maxItems instanceof Integer)) return handleError("maxItems option must be an integer");
      if ((int) maxItems < 0) return handleError("maxItems option integer must be positive");
      if (((List<?>) currentList).size() - (int) maxItems > 0) {
        ((List<?>) currentList).subList(0, ((List<?>) currentList).size() - (int) maxItems).clear();
      }
    }

    return root.jsonString();
  }
}
