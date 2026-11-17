/*
 * Copyright (C) 2023-2026 Thomas Akehurst
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

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class Lazy<T> {

  public static <T> Lazy<T> lazy(Supplier<T> supplier) {
    return new Lazy<>(supplier);
  }

  private final Supplier<T> supplier;
  private volatile @Nullable T ref;

  private Lazy(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  /**
   * Double-checked locking, so that a supplier which allocates a resource cannot be run twice and
   * leak the instance that loses the race. Once initialised the fast path is a single volatile
   * read, so concurrent readers never contend.
   */
  public @Nullable T get() {
    T local = ref;
    if (local == null) {
      synchronized (this) {
        local = ref;
        if (local == null) {
          local = supplier.get();
          ref = local;
        }
      }
    }
    return local;
  }
}
