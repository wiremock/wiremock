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
package org.wiremock.url;

import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

class Lazy<T> {

  static <T> Lazy<T> lazy(Supplier<T> supplier) {
    return lazy(null, supplier);
  }

  static <T> Lazy<T> lazy(@Nullable T initial, Supplier<T> supplier) {
    @SuppressWarnings("UnnecessaryLocalVariable")
    Lazy<T> lazy = new Lazy<>(initial, supplier);
    return lazy;
  }

  private final Supplier<T> supplier;
  private volatile @Nullable T ref;

  private Lazy(@Nullable T initial, Supplier<T> supplier) {
    this.ref = initial;
    this.supplier = supplier;
  }

  T get() {
    T local = ref;
    if (local == null) {
      local = supplier.get();
      ref = local;
    }
    return local;
  }
}
