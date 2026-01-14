/*
 * Copyright (C) 2026 Thomas Akehurst
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
import java.util.List;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

class QueryBuilder implements Query.Builder {

  private final List<Entry<QueryParamKey, @Nullable QueryParamValue>> mutableParams;

  QueryBuilder() {
    mutableParams = new ArrayList<>();
  }

  QueryBuilder(List<Entry<QueryParamKey, @Nullable QueryParamValue>> query) {
    mutableParams = new ArrayList<>(query);
  }

  @Override
  public Query.Builder append(String key, @Nullable String value, @Nullable String... otherValues) {
    append(key, value);
    for (var otherValue : otherValues) {
      append(key, otherValue);
    }
    return this;
  }

  @Override
  public Query.Builder append(
      QueryParamKey key,
      @Nullable QueryParamValue value,
      @Nullable QueryParamValue... otherValues) {
    append(key, value);
    for (var otherValue : otherValues) {
      append(key, otherValue);
    }
    return this;
  }

  @Override
  public Query.Builder append(QueryParamKey key, List<? extends @Nullable QueryParamValue> values) {
    for (var value : values) {
      append(key, value);
    }
    return this;
  }

  private void append(String key, @Nullable String value) {
    append(QueryParamKey.encode(key), value);
  }

  private void append(QueryParamKey key, @Nullable String value) {
    append(key, value != null ? QueryParamValue.encode(value) : null);
  }

  private void append(QueryParamKey key, @Nullable QueryParamValue value) {
    mutableParams.add(new SimpleEntry<>(key, value));
  }

  @Override
  public Query.Builder put(String key, @Nullable String value, @Nullable String... otherValues) {
    var encodedKey = QueryParamKey.encode(key);
    remove(encodedKey);
    append(encodedKey, value);
    for (var otherValue : otherValues) {
      append(encodedKey, otherValue);
    }
    return this;
  }

  @Override
  public Query.Builder put(
      QueryParamKey key,
      @Nullable QueryParamValue value,
      @Nullable QueryParamValue... otherValues) {
    remove(key);
    this.append(key, value, otherValues);
    return this;
  }

  @Override
  public Query.Builder put(QueryParamKey key, List<? extends @Nullable QueryParamValue> values) {
    remove(key);
    append(key, values);
    return this;
  }

  @Override
  public Query.Builder remove(String key) {
    return remove(QueryParamKey.encode(key));
  }

  @Override
  public Query.Builder remove(QueryParamKey key) {
    mutableParams.removeIf(e -> e.getKey().equals(key));
    return this;
  }

  @Override
  public Query.Builder remove(String key, @Nullable String value, @Nullable String... otherValues) {
    return remove(QueryParamKey.encode(key), QueryValue.encodeValues(value, otherValues));
  }

  @Override
  public Query.Builder remove(
      QueryParamKey key,
      @Nullable QueryParamValue value,
      @Nullable QueryParamValue... otherValues) {
    return remove(key, Lists.of(value, otherValues));
  }

  @Override
  public Query.Builder remove(QueryParamKey key, List<? extends @Nullable QueryParamValue> values) {
    mutableParams.removeIf(e -> e.getKey().equals(key) && values.contains(e.getValue()));
    return this;
  }

  @Override
  public Query build() {
    if (mutableParams.isEmpty()) {
      return Query.EMPTY;
    } else {
      return new QueryValue(mutableParams);
    }
  }
}
