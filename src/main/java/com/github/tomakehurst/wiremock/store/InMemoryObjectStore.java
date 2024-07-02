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

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class InMemoryObjectStore implements ObjectStore {

  private final ConcurrentHashMap<String, Object> cache;
  private final Queue<String> keyUseOrder = new ConcurrentLinkedQueue<>();
  private final int maxItems;

  private final Executor executor =
      Executors.newSingleThreadExecutor(
          r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
          });
  private final Object lock = new Object();

  public InMemoryObjectStore(int maxItems) {
    this.cache = new ConcurrentHashMap<>();
    this.maxItems = maxItems;
  }

  @Override
  public <T> Optional<T> get(String key, Class<T> type) {
    return get(key).map(type::cast);
  }

  @Override
  public Optional<Object> get(String key) {
    Optional<Object> value = Optional.ofNullable(cache.get(key));
    if (value.isPresent()) {
      executor.execute(
          () -> {
            synchronized (lock) {
              touch(key);
            }
          });
    }
    return value;
  }

  @Override
  public Stream<String> getAllKeys() {
    return cache.keySet().stream();
  }

  @Override
  public void put(String key, Object content) {
    synchronized (lock) {
      cache.put(key, content);
      touchAndResize(key);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T compute(String key, Function<T, T> valueFunction) {
    synchronized (lock) {
      final T result =
          (T) cache.compute(key, (k, currentValue) -> valueFunction.apply((T) currentValue));
      touchAndResize(key);
      return result;
    }
  }

  @Override
  public void remove(String key) {
    synchronized (lock) {
      cache.remove(key);
      keyUseOrder.remove(key);
    }
  }

  @Override
  public void clear() {
    synchronized (lock) {
      cache.clear();
      keyUseOrder.clear();
    }
  }

  private void touchAndResize(String key) {
    touch(key);
    resize();
  }

  private void touch(String key) {
    keyUseOrder.remove(key);
    keyUseOrder.offer(key);
  }

  private void resize() {
    while (keyUseOrder.size() > maxItems) {
      final String keyToRemove = keyUseOrder.poll();
      remove(keyToRemove);
    }
  }
}
