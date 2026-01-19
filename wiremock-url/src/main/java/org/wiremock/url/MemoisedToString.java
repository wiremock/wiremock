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

import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * A helper class that memoises the result of a {@code toString()} computation.
 *
 * <p>This is useful when an object's string representation is expensive to compute (e.g., by
 * concatenating multiple component parts) but the object is immutable, so the result will always be
 * the same.
 *
 * <p>This class is thread-safe in the sense that it will always return correct results, but it does
 * not guarantee that the work will only be done once. Under concurrent access, {@code toStringWork}
 * may be invoked multiple times by different threads. Therefore, the supplier must be idempotent
 * and always return the same result.
 */
final class MemoisedToString {

  private volatile @Nullable String memoised;
  private final Supplier<String> toStringWork;

  MemoisedToString(@Nullable String stringValue, Supplier<String> toStringWork) {
    this.memoised = stringValue;
    this.toStringWork = toStringWork;
  }

  @Override
  public String toString() {
    String s = memoised;
    if (s == null) {
      s = toStringWork.get();
      memoised = s;
    }
    return s;
  }
}
