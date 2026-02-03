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

import static org.wiremock.url.Lazy.lazy;

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

  private final String query;
  private final Lazy<List<Map.Entry<QueryParamKey, @Nullable QueryParamValue>>> paramEntries;
  private final MemoisedNormalisable<Query> memoisedNormalisable;

  QueryValue(String query) {
    this(query, null);
  }

  QueryValue(String query, @Nullable Boolean isNormalForm) {
    this(query, null, isNormalForm);
  }

  QueryValue(List<Entry<QueryParamKey, @Nullable QueryParamValue>> params) {
    this(params, null);
  }

  QueryValue(
      List<Entry<QueryParamKey, @Nullable QueryParamValue>> params,
      @Nullable Boolean isNormalForm) {
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

  QueryValue(
      String query,
      @Nullable List<Map.Entry<QueryParamKey, @Nullable QueryParamValue>> paramEntries,
      @Nullable Boolean isNormalForm) {
    this.query = query;
    this.paramEntries = lazy(paramEntries, this::parseEntries);
    this.memoisedNormalisable =
        new MemoisedNormalisable<>(this, isNormalForm, this::isNormalFormWork, this::normaliseWork);
  }

  @Override
  public String toString() {
    return query;
  }

  @Override
  public Query normalise() {
    return memoisedNormalisable.normalise();
  }

  private @Nullable Query normaliseWork() {
    String result = PercentEncoding.simpleNormalise(query, QueryParser.queryCharSet);
    return result != null ? new QueryValue(result, true) : null;
  }

  @Override
  public boolean isNormalForm() {
    return memoisedNormalisable.isNormalForm();
  }

  private boolean isNormalFormWork() {
    return PercentEncoding.isSimpleNormalForm(query, QueryParser.queryCharSet);
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
    return paramEntries.get();
  }

  private List<Entry<QueryParamKey, @Nullable QueryParamValue>> parseEntries() {
    var keyValuePairStrs = query.split("&", -1);
    var result = new ArrayList<Entry<QueryParamKey, @Nullable QueryParamValue>>();
    for (String keyValuePairStr : keyValuePairStrs) {
      var keyValuePair = keyValuePairStr.split("=", 2);
      var key = new QueryParamKeyValue(keyValuePair[0]);
      var value = keyValuePair.length == 2 ? new QueryParamValueValue(keyValuePair[1]) : null;
      result.add(new SimpleEntry<>(key, value));
    }
    return Collections.unmodifiableList(result);
  }

  static List<@Nullable QueryParamValue> encodeValues(
      @Nullable String value, @Nullable String[] otherValues) {
    return Lists.of(value, otherValues).stream().map(QueryValue::encodeValue).toList();
  }

  private static @Nullable QueryParamValue encodeValue(@Nullable String value) {
    return value != null ? QueryParamValue.encode(value) : null;
  }
}
