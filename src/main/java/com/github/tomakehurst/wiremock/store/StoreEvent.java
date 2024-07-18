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
package com.github.tomakehurst.wiremock.store;

import java.util.Objects;

public class StoreEvent<K, V> {

  public static <K, V> StoreEvent<K, V> set(K key, V previousValue, V newValue) {
    return new StoreEvent<>(key, previousValue, newValue);
  }

  public static <K, V> StoreEvent<K, V> remove(K key, V previousValue) {
    return new StoreEvent<>(key, previousValue, null);
  }

  private final K key;
  private final V oldValue;
  private final V newValue;

  public StoreEvent(K key, V oldValue, V newValue) {
    this.key = key;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  public K getKey() {
    return key;
  }

  public V getOldValue() {
    return oldValue;
  }

  public V getNewValue() {
    return newValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StoreEvent<?, ?> that = (StoreEvent<?, ?>) o;
    return Objects.equals(key, that.key)
        && Objects.equals(oldValue, that.oldValue)
        && Objects.equals(newValue, that.newValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, oldValue, newValue);
  }

  @Override
  public String toString() {
    return "StoreEvent{" + "key=" + key + ", oldValue=" + oldValue + ", newValue=" + newValue + '}';
  }
}
