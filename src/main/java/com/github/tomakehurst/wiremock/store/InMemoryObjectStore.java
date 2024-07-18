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

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class InMemoryObjectStore implements ObjectStore {

  private final ConcurrentHashMap<String, Object> cache;
  private final Queue<String> keyUseOrder = new ConcurrentLinkedQueue<>();
  private final int maxItems;
  private final List<Consumer<StoreEvent<String, Object>>> listeners = new ArrayList<>();

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
      touch(key);
    }
    return value;
  }

  @Override
  public Stream<String> getAllKeys() {
    return cache.keySet().stream();
  }

  @Override
  public void put(String key, Object content) {
    Object previousValue = cache.put(key, content);
    touchAndResize(key);
    handleEvent(new StoreEvent<>(key, previousValue, content));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T compute(String key, Function<T, T> valueFunction) {
    final AtomicReference<T> previousValue = new AtomicReference<>();
    final T result =
        (T)
            cache.compute(
                key,
                (k, currentValue) -> {
                  previousValue.set((T) currentValue);
                  return valueFunction.apply((T) currentValue);
                });
    if (result != null) {
      touchAndResize(key);
    } else {
      keyUseOrder.remove(key);
    }
    handleEvent(new StoreEvent<>(key, previousValue.get(), result));
    return result;
  }

  @Override
  public void remove(String key) {
    Object previousValue = cache.remove(key);
    keyUseOrder.remove(key);
    if (previousValue != null) {
      handleEvent(new StoreEvent<>(key, previousValue, null));
    }
  }

  @Override
  public void clear() {
    cache.clear();
    keyUseOrder.clear();
  }

  @Override
  public void registerEventListener(Consumer<StoreEvent<String, Object>> handler) {
    listeners.add(handler);
  }

  private void handleEvent(StoreEvent<String, Object> event) {
    for (Consumer<StoreEvent<String, Object>> listener : listeners) {
      try {
        listener.accept(event);
      } catch (Exception e) {
        notifier().error("Error handling store event", e);
      }
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
