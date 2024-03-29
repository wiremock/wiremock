/*
 * Copyright (C) 2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class MultiValues<T extends MultiValue> implements Map<String, T> {

  private Map<String, T> map = new HashMap<>();

  public MultiValues() {}

  public MultiValues(Map<String, T> items) {
    if (items == null) this.map = Collections.unmodifiableMap(new HashMap<>());
    else this.map = Collections.unmodifiableMap(items);
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public T get(Object key) {
    return map.get(key);
  }

  @Override
  public T put(String key, T value) {
    return map.put(key, value);
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public Set<String> keySet() {
    return map.keySet();
  }

  @Override
  public Collection<T> values() {
    return map.values();
  }

  @Override
  public T remove(Object key) {
    return map.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends T> m) {
    map.putAll(m);
  }

  @Override
  public Set<Entry<String, T>> entrySet() {
    return this.map.entrySet();
  }

  public Object getMap() {
    return this.map;
  }

  /**
   * Provides a sorted, deterministic, summary of the contents. Intended to be asserted on in test
   * cases to provide a clear overview of the content.
   */
  public String summary() {
    Map<String, List<String>> sorted = new TreeMap<>();
    this.map
        .entrySet()
        .forEach(
            e -> {
              String key = e.getKey();
              if (!sorted.containsKey(key)) {
                sorted.put(key, new ArrayList<>());
              }
              for (String value : e.getValue().getValues()) {
                sorted.get(key).add(value);
              }
            });
    return sorted.entrySet().stream()
        .map(e -> e.getKey() + ": [" + e.getValue().stream().sorted().collect(joining(", ")) + "]")
        .collect(joining("\n"));
  }

  @Override
  public int hashCode() {
    return Objects.hash(map);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MultiValues<?> other = (MultiValues<?>) obj;
    return Objects.equals(map, other.map);
  }

  @Override
  public String toString() {
    return "MultiValues [map=" + map + "]";
  }
}
