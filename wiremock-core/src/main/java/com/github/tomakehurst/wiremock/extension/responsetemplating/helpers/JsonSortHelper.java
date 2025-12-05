/*
 * Copyright (C) 2025 Thomas Akehurst
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
import com.github.tomakehurst.wiremock.common.RequestCache;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.PathNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class JsonSortHelper extends HandlebarsHelper<Object> {

  private final ParseContext parseContext = JsonPath.using(HelperUtils.jsonPathConfig);

  @Override
  public Object apply(Object inputJson, Options options) throws IOException {
    if (!(inputJson instanceof String)) {
      return handleError("Input JSON must be a string");
    }
    if (inputJson.equals("null")) {
      // No op
      return inputJson;
    }
    if (options.params.length != 1) {
      return handleError("A single JSONPath expression parameter must be supplied");
    }
    Object jsonPathString = options.param(0);
    if (!(jsonPathString instanceof String)) {
      return handleError("JSONPath parameter must be a string");
    }

    // Extract and validate optional 'order' parameter
    String order = "asc"; // default to ascending
    if (options.hash != null) {
      Object orderParam = options.hash.get("order");
      if (orderParam != null) {
        if (!(orderParam instanceof String)) {
          return handleError("order parameter must be a string");
        }
        order = (String) orderParam;
        if (!order.equals("asc") && !order.equals("desc")) {
          return handleError("order parameter must be 'asc' or 'desc'");
        }
      }
    }
    // Extract array path from JsonPath
    String arrayPath = extractArrayPath((String) jsonPathString);
    if (arrayPath == null) {
      return handleError(
          "JSONPath must include [*] to specify array location (e.g., '$[*].name' or '$.users[*].name')");
    }

    DocumentContext jsonDocument;
    try {
      jsonDocument = getParsedDocument((String) inputJson, options);
    } catch (Exception e) {
      return handleError("Input JSON string is not valid JSON ('" + inputJson + "')", e);
    }
    // Read the sort values using the full JsonPath with [*]
    List<?> sortValues;
    try {
      sortValues = readJsonPath(jsonDocument, (String) jsonPathString, List.class, options);
    } catch (PathNotFoundException e) {
      return handleError("JSONPath expression did not match any values ('" + jsonPathString + "')");
    } catch (Exception e) {
      return handleError("Invalid JSONPath expression ('" + jsonPathString + "')", e);
    }
    // Read the array to sort
    Object arrayObject;
    try {
      arrayObject = readJsonPath(jsonDocument, arrayPath, Object.class, options);
    } catch (PathNotFoundException e) {
      return handleError("Array not found at path ('" + arrayPath + "')");
    } catch (Exception e) {
      return handleError("Error reading array at path ('" + arrayPath + "')", e);
    }

    // Validate it's actually an array
    if (!(arrayObject instanceof List)) {
      return handleError("JSONPath does not reference an array ('" + arrayPath + "')");
    }

    @SuppressWarnings("unchecked")
    List<Object> array = (List<Object>) arrayObject;

    // Validate sort values list matches array size
    if (sortValues.size() != array.size()) {
      return handleError(
          "Number of sort values ("
              + sortValues.size()
              + ") does not match array size ("
              + array.size()
              + ")");
    }

    // Handle empty arrays early - nothing to validate or sort
    if (array.isEmpty()) {
      return jsonDocument.jsonString();
    }

    // Check for nulls (missing fields)
    for (Object value : sortValues) {
      if (value == null) {
        return handleError(
            "All objects in the array must have the sort field specified by JSONPath expression ('"
                + jsonPathString
                + "')");
      }
    }

    // Validate all values are the same comparable type
    Class<?> commonType = detectCommonType(sortValues);
    if (commonType == null) {
      return handleError(
          "All sort field values must be of the same comparable type (Number, String, or Boolean)");
    }

    // Handle single element arrays - validated, but no sorting needed
    if (array.size() == 1) {
      return jsonDocument.jsonString();
    }

    // Create pairs of (array element, sort value)
    List<SortPair> pairs = new ArrayList<>();
    for (int i = 0; i < array.size(); i++) {
      pairs.add(new SortPair(array.get(i), sortValues.get(i)));
    }

    // Create comparator based on detected type
    Comparator<Object> valueComparator = createComparator(commonType);
    if ("desc".equals(order)) {
      valueComparator = valueComparator.reversed();
    }

    // Sort pairs by their sort values
    final Comparator<Object> finalComparator = valueComparator;
    pairs.sort((a, b) -> finalComparator.compare(a.sortValue, b.sortValue));

    // Extract sorted objects
    List<Object> sortedArray = pairs.stream().map(p -> p.object).collect(Collectors.toList());

    // Special case: if we're sorting the root array, we can't use set() as JsonPath doesn't allow
    // setting the root element. Instead, we serialize the sorted array directly.
    if ("$".equals(arrayPath)) {
      return parseContext.parse(sortedArray).jsonString();
    }

    // Update the document with a sorted array
    jsonDocument.set(arrayPath, sortedArray);

    return jsonDocument.jsonString();
  }

  private String extractArrayPath(String jsonPath) {
    // Extract the array path from expressions like "$[*].name" or "$.users[*].priority"
    int wildcardIndex = jsonPath.indexOf("[*]");
    if (wildcardIndex == -1) {
      return null; // Must contain [*]
    }

    // Return everything before [*]
    String path = jsonPath.substring(0, wildcardIndex);
    return path.isEmpty() ? "$" : path;
  }

  private Class<?> detectCommonType(List<?> values) {
    if (values.isEmpty()) {
      return null;
    }

    Class<?> firstType = getComparableType(values.get(0));
    if (firstType == null) {
      return null;
    }

    for (Object value : values) {
      Class<?> valueType = getComparableType(value);
      if (!firstType.equals(valueType)) {
        return null;
      }
    }

    return firstType;
  }

  private Class<?> getComparableType(Object value) {
    if (value == null) {
      return null;
    }
    if (Number.class.isAssignableFrom(value.getClass())) {
      return Number.class;
    }
    if (value instanceof String) {
      return String.class;
    }
    if (value instanceof Boolean) {
      return Boolean.class;
    }
    return null; // Unsupported type (object, array, etc.)
  }

  private record SortPair(Object object, Object sortValue) {}

  private Comparator<Object> createComparator(Class<?> type) {
    if (Number.class.equals(type)) {
      return (a, b) -> {
        BigDecimal bdA = toBigDecimal((Number) a);
        BigDecimal bdB = toBigDecimal((Number) b);
        return bdA.compareTo(bdB);
      };
    } else if (String.class.equals(type)) {
      return Comparator.comparing(v -> (String) v);
    } else if (Boolean.class.equals(type)) {
      return Comparator.comparing(v -> (Boolean) v);
    }
    throw new IllegalArgumentException("Unsupported type: " + type);
  }

  private BigDecimal toBigDecimal(Number number) {
    if (number instanceof BigDecimal) {
      return (BigDecimal) number;
    } else if (number instanceof Integer || number instanceof Long ||
            number instanceof Short || number instanceof Byte) {
      return BigDecimal.valueOf(number.longValue());
    } else if (number instanceof Float || number instanceof Double) {
      return BigDecimal.valueOf(number.doubleValue());
    } else {
      // Fallback for other Number types
      return new BigDecimal(number.toString());
    }
  }



  private DocumentContext getParsedDocument(String json, Options options) {
    RequestCache requestCache = getRequestCache(options);
    RequestCache.Key cacheKey = RequestCache.Key.keyFor(DocumentContext.class, json);
    DocumentContext document = requestCache.get(cacheKey);
    if (document == null) {
      document = parseContext.parse(json);
      requestCache.put(cacheKey, document);
    }
    return document;
  }

  private <T> T readJsonPath(DocumentContext document, String jsonPath, Class<T> returnType, Options options) {
    RequestCache requestCache = getRequestCache(options);
    RequestCache.Key cacheKey = RequestCache.Key.keyFor(returnType, jsonPath, document);
    T value = requestCache.get(cacheKey);
    if (value == null) {
      value = document.read(jsonPath, returnType);
      requestCache.put(cacheKey, value);
    }
    return value;
  }

}
