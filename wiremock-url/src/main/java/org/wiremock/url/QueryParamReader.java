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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * All methods accepting String as a parameter will call {@code encode} to encode it to a valid
 * {@link QueryParamKey} or {@link QueryParamValue}, so must be called with an <b>unencoded</b>
 * value.
 */
public interface QueryParamReader {

  List<Entry<QueryParamKey, @Nullable QueryParamValue>> getEntries();

  default List<@Nullable QueryParamValue> get(String key) {
    return get(QueryParamKey.encode(key));
  }

  default List<@Nullable QueryParamValue> get(QueryParamKey key) {
    List<@Nullable QueryParamValue> result = asMap().get(key.normalise());
    return result != null ? result : List.of();
  }

  default boolean contains(String key) {
    return contains(QueryParamKey.encode(key));
  }

  default boolean contains(QueryParamKey key) {
    return asMap().containsKey(key.normalise());
  }

  default Set<QueryParamKey> getKeys() {
    return asMap().keySet();
  }

  default Map<QueryParamKey, List<@Nullable QueryParamValue>> asMap() {
    return getEntries().stream()
        .collect(
            Collectors.groupingBy(
                entry -> entry.getKey().normalise(),
                Collectors.mapping(Entry::getValue, Collectors.toList())));
  }

  default @Nullable QueryParamValue getFirst(String key) {
    return getFirst(QueryParamKey.encode(key));
  }

  default @Nullable QueryParamValue getFirst(QueryParamKey key) {
    List<@Nullable QueryParamValue> values = get(key);
    return values.isEmpty() ? null : values.get(0);
  }
}
