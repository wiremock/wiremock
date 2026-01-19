/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package org.wiremock.url;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

final class QueryValue implements Query {

  static final Query EMPTY = new QueryValue("", List.of(), true);

  private final String query;
  private volatile @Nullable List<Map.Entry<QueryParamKey, @Nullable QueryParamValue>> paramEntries;
  private final boolean isNormalForm;

  QueryValue(String query) {
    this(query, false);
  }

  QueryValue(String query, boolean isNormalForm) {
    this(query, null, isNormalForm);
  }

  QueryValue(List<Entry<QueryParamKey, @Nullable QueryParamValue>> params) {
    this(params, false);
  }

  QueryValue(List<Entry<QueryParamKey, @Nullable QueryParamValue>> params, boolean isNormalForm) {
    this(joinToString(params), Collections.unmodifiableList(params), isNormalForm);
  }

  private static String joinToString(List<Entry<QueryParamKey, @Nullable QueryParamValue>> params) {
    return params.stream()
        .map(
            entry -> {
              var value = entry.getValue();
              return value == null ? entry.getKey().toString() : entry.getKey() + "=" + value;
            })
        .collect(Collectors.joining("&"));
  }

  private QueryValue(
      String query,
      @Nullable List<Map.Entry<QueryParamKey, @Nullable QueryParamValue>> paramEntries,
      boolean isNormalForm) {
    this.query = query;
    this.paramEntries = paramEntries;
    this.isNormalForm = isNormalForm;
  }

  @Override
  public String toString() {
    return query;
  }

  @Override
  public Query normalise() {
    if (isNormalForm) {
      return this;
    }

    var currentParams = getEntries();
    List<Map.Entry<QueryParamKey, @Nullable QueryParamValue>> normalised =
        currentParams.stream()
            .map(
                entry -> {
                  var value = entry.getValue();
                  @SuppressWarnings("UnnecessaryLocalVariable")
                  // it's not unnecessary, the compiler needs it
                  Entry<QueryParamKey, @Nullable QueryParamValue> normalEntry =
                      new SimpleEntry<>(
                          entry.getKey().normalise(), (value != null ? value.normalise() : value));
                  return normalEntry;
                })
            .toList();

    if (normalised.equals(currentParams)) {
      return this;
    } else {
      return new QueryValue(normalised, true);
    }
  }

  private volatile @Nullable Map<QueryParamKey, List<@Nullable QueryParamValue>> asMap = null;

  @Override
  public Map<QueryParamKey, List<@Nullable QueryParamValue>> asMap() {
    Map<QueryParamKey, List<@Nullable QueryParamValue>> local = asMap;
    if (local == null) {
      local = Query.super.asMap();
      asMap = local;
    }
    return local;
  }

  @Override
  public boolean isNormalForm() {
    return isNormalForm
        || getEntries().stream()
            .allMatch(
                entry -> {
                  QueryParamValue value = entry.getValue();
                  return entry.getKey().isNormalForm() && (value == null || value.isNormalForm());
                });
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof Query that) {
      return Objects.equals(this.toString(), that.toString());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(query);
  }

  @Override
  public Builder thaw() {
    return new QueryBuilder(getEntries());
  }

  @Override
  public List<Entry<QueryParamKey, @Nullable QueryParamValue>> getEntries() {
    var entries = paramEntries;
    if (entries != null) {
      return entries;
    } else {
      var result = new ArrayList<Map.Entry<QueryParamKey, @Nullable QueryParamValue>>();
      var keyValuePairStrs = query.split("&", -1);
      for (String keyValuePairStr : keyValuePairStrs) {
        var keyValuePair = keyValuePairStr.split("=", 2);
        var key = new QueryParamKeyValue(keyValuePair[0]);
        var value = keyValuePair.length == 2 ? new QueryParamValueValue(keyValuePair[1]) : null;
        result.add(new SimpleEntry<>(key, value));
      }
      var unmodifiableResult = Collections.unmodifiableList(result);
      paramEntries = unmodifiableResult;
      return unmodifiableResult;
    }
  }

  static List<@Nullable QueryParamValue> encodeValues(
      @Nullable String value, @Nullable String[] otherValues) {
    return Lists.of(value, otherValues).stream().map(QueryValue::encodeValue).toList();
  }

  private static @Nullable QueryParamValue encodeValue(@Nullable String value) {
    return value != null ? QueryParamValue.encode(value) : null;
  }
}
