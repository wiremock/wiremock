/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class ParameterUtils {

  private ParameterUtils() {}

  public static <T> boolean isNullOrEmptyCollection(Collection<T> collection) {
    return collection == null || collection.isEmpty();
  }

  public static <T> boolean isNotNullOrEmptyCollection(Collection<T> collection) {
    return !isNullOrEmptyCollection(collection);
  }

  public static <T> T getFirstNonNull(T first, T second) {
    if (first != null) {
      return first;
    }
    if (second != null) {
      return second;
    }
    throw new NullPointerException("Both parameters are null");
  }

  public static <T> T getFirstNonNull(T first, T second, String etr) {
    if (first != null) {
      return first;
    }
    if (second != null) {
      return second;
    }
    throw new NullPointerException(etr);
  }

  public static void checkParameter(boolean condition, String errorMessage) {
    if (!condition) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  public static void checkState(boolean expression, String errorMessage) {
    if (!expression) {
      throw new IllegalStateException(errorMessage);
    }
  }

  public static <T> T checkNotNull(T value, String errorMessage) {
    if (value == null) {
      throw new NullPointerException(errorMessage);
    }
    return value;
  }

  public static <T> int indexOf(Iterable<T> iterable, Predicate<? super T> predicate) {
    checkNotNull(iterable, "iterable");
    checkNotNull(predicate, "predicate");
    Iterator<T> iterator = iterable.iterator();
    for (int i = 0; iterator.hasNext(); i++) {
      T current = iterator.next();
      if (predicate.test(current)) {
        return i;
      }
    }

    return -1;
  }

  public static <T> T getFirst(Iterable<T> iterable, T defaultValue) {
    return iterable != null && iterable.iterator().hasNext()
        ? iterable.iterator().next()
        : defaultValue;
  }

  public static <T> T getLast(Iterable<T> iterable) {
    if (iterable == null || !iterable.iterator().hasNext()) {
      throw new NoSuchElementException();
    }
    Iterator<T> iterator = iterable.iterator();
    while (true) {
      T current = iterator.next();
      if (!iterator.hasNext()) {
        return current;
      }
    }
  }
}
