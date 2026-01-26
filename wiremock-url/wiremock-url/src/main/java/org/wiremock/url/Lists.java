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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public final class Lists {
  static <T> List<@Nullable T> of(@Nullable T value, @Nullable T[] otherValues) {
    var values = new ArrayList<@Nullable T>(1 + otherValues.length);
    values.add(value);
    Collections.addAll(values, otherValues);
    return values;
  }

  public static <C extends Collection<T>, T> List<? extends T> concat(
      Collection<? extends C> lists) {
    return concat(lists.stream());
  }

  @SafeVarargs
  public static <T> List<? extends T> concat(Collection<? extends T>... lists) {
    return concat(Stream.of(lists));
  }

  public static <C extends Collection<? extends T>, T> List<? extends T> concat(Stream<C> lists) {
    return lists.flatMap(Collection::stream).toList();
  }
}
