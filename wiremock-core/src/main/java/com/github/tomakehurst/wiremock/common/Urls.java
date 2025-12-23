/*
 * Copyright (C) 2011-2026 Thomas Akehurst
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import com.github.tomakehurst.wiremock.http.QueryParameter;
import java.net.URLDecoder;
import java.text.ParsePosition;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.wiremock.url.Query;
import org.wiremock.url.Url;

public class Urls {

  private Urls() {}

  public static Map<String, QueryParameter> splitQueryFromUrl(String url) {
    return toQueryParameterMap(Url.parse(url).getQueryOrEmpty());
  }

  public static Map<String, QueryParameter> toQueryParameterMap(Query query) {
    if (query.isEmpty()) {
      return Collections.emptyMap();
    }

    return query.asMap().entrySet().stream()
        .map(
            e -> {
              var key = e.getKey().decode();
              var values =
                  e.getValue().stream()
                      .map(
                          queryParamValue ->
                              queryParamValue != null ? queryParamValue.decode() : "")
                      .toList();
              return Map.entry(key, new QueryParameter(key, values));
            })
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  public static String getPath(String url) {
    return url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
  }

  public static String urlToPathParts(Url uri) {
    List<String> uriPathNodes =
        uri.getPath().getSegments().stream()
            .filter(s -> !s.isEmpty())
            .map(Object::toString)
            .toList();
    int nodeCount = uriPathNodes.size();

    return nodeCount > 0 ? String.join("-", uriPathNodes) : "";
  }

  public static String decode(String encoded) {
    if (!isISOOffsetDateTime(encoded)) {
      return URLDecoder.decode(encoded, UTF_8);
    }
    return encoded;
  }

  private static boolean isISOOffsetDateTime(String encoded) {
    /*
    First we try to soft parse the string using a ParsePosition. This avoids the cost of
    exception handling in the case of a non-date string. If the soft parse succeeds, we
    then do a full parse to ensure the string is a valid date.
     */
    ParsePosition pos = new ParsePosition(0);
    TemporalAccessor temporalAccessor = ISO_OFFSET_DATE_TIME.parseUnresolved(encoded, pos);
    if (temporalAccessor == null || pos.getIndex() != encoded.length()) {
      return false;
    }
    try {
      ISO_OFFSET_DATE_TIME.parse(encoded);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }
}
