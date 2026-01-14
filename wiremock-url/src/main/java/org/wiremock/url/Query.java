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

import static org.wiremock.url.QueryValue.encodeValues;

import java.util.List;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

/**
 * Represents the query component of a URI as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4">RFC 3986 Section 3.4</a>.
 *
 * <p>The query component contains non-hierarchical data, typically formatted as key-value pairs
 * separated by ampersands. Query strings may contain percent-encoded characters.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4">RFC 3986 Section 3.4</a>
 */
public interface Query extends PercentEncoded<Query>, QueryParamReader {

  Query EMPTY = QueryValue.EMPTY;

  /**
   * Parses a string into a query.
   *
   * @param query the string to parse
   * @return the parsed query
   * @throws IllegalQuery if the string is not a valid query
   */
  static Query parse(String query) throws IllegalQuery {
    return QueryParser.INSTANCE.parse(query);
  }

  /**
   * Encodes a string into a valid query with proper percent-encoding.
   *
   * @param unencoded the unencoded string
   * @return the encoded query
   */
  static Query encode(String unencoded) {
    return QueryParser.INSTANCE.encode(unencoded);
  }

  @Override
  Query normalise();

  default Query with(String key, @Nullable String value, @Nullable String... otherValues) {
    return with(QueryParamKey.encode(key), encodeValues(value, otherValues));
  }

  default Query with(
      QueryParamKey key,
      @Nullable QueryParamValue value,
      @Nullable QueryParamValue... otherValues) {
    return with(key, Lists.of(value, otherValues));
  }

  default Query with(QueryParamKey key, List<? extends @Nullable QueryParamValue> values) {
    return transform(q -> q.append(key, values));
  }

  default Query replace(String key, @Nullable String value, @Nullable String... otherValues) {
    return replace(QueryParamKey.encode(key), encodeValues(value, otherValues));
  }

  default Query replace(
      QueryParamKey key,
      @Nullable QueryParamValue value,
      @Nullable QueryParamValue... otherValues) {
    return replace(key, Lists.of(value, otherValues));
  }

  default Query replace(QueryParamKey key, List<? extends @Nullable QueryParamValue> values) {
    return transform(q -> q.put(key, values));
  }

  default Query without(String key) {
    return without(QueryParamKey.encode(key));
  }

  default Query without(QueryParamKey key) {
    return transform(q -> q.remove(key));
  }

  default Query without(String key, @Nullable String value, @Nullable String... otherValues) {
    return without(QueryParamKey.encode(key), encodeValues(value, otherValues));
  }

  default Query without(
      QueryParamKey key,
      @Nullable QueryParamValue value,
      @Nullable QueryParamValue... otherValues) {
    return without(key, Lists.of(value, otherValues));
  }

  default Query without(QueryParamKey key, List<? extends @Nullable QueryParamValue> values) {
    return transform(q -> q.remove(key, values));
  }

  static Builder builder() {
    return new QueryBuilder();
  }

  Builder thaw();

  default Query transform(Consumer<Builder> transformer) {
    var builder = thaw();
    transformer.accept(builder);
    return builder.build();
  }

  interface Builder {

    Builder append(String key, @Nullable String value, @Nullable String... otherValues);

    Builder append(
        QueryParamKey key,
        @Nullable QueryParamValue value,
        @Nullable QueryParamValue... otherValues);

    Builder append(QueryParamKey key, List<? extends @Nullable QueryParamValue> values);

    Builder put(String key, @Nullable String value, @Nullable String... otherValues);

    Builder put(
        QueryParamKey key,
        @Nullable QueryParamValue value,
        @Nullable QueryParamValue... otherValues);

    Builder put(QueryParamKey key, List<? extends @Nullable QueryParamValue> values);

    Builder remove(String key);

    Builder remove(QueryParamKey key);

    Builder remove(String key, @Nullable String value, @Nullable String... otherValues);

    Builder remove(
        QueryParamKey key,
        @Nullable QueryParamValue value,
        @Nullable QueryParamValue... otherValues);

    Builder remove(QueryParamKey key, List<? extends @Nullable QueryParamValue> values);

    Query build();
  }
}
