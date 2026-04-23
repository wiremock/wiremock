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

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * A helper class that memoises the results of {@link Normalisable#normalise()} and {@link
 * Normalisable#isNormalForm()} operations.
 *
 * <p>This class is thread-safe in the sense that it will always return correct results, but it does
 * not guarantee that the work will only be done once. Under concurrent access, both {@code
 * isNormalFormWork} and {@code normalisationWork} may be invoked multiple times by different
 * threads. Therefore:
 *
 * <ul>
 *   <li>Both suppliers must be idempotent and always return the same result for the same input
 *   <li>Both suppliers must be consistent with each other: if {@code normalisationWork} returns
 *       {@code null} (meaning the original is already in normal form), then {@code
 *       isNormalFormWork} must return {@code true}, and vice versa
 * </ul>
 *
 * @param <NORMALISED> the type of the normalisable value
 */
final class MemoisedNormalisable<NORMALISED extends Normalisable<NORMALISED>> {

  private final NORMALISED original;
  private final BooleanSupplier isNormalFormWork;
  private final Supplier<@Nullable NORMALISED> normalisationWork;

  private volatile @Nullable NORMALISED normalised;
  private volatile @Nullable Boolean isNormalForm;

  MemoisedNormalisable(
      NORMALISED original,
      BooleanSupplier isNormalFormWork,
      Supplier<@Nullable NORMALISED> normalisationWork) {
    this(original, null, isNormalFormWork, normalisationWork);
  }

  MemoisedNormalisable(
      NORMALISED original,
      @Nullable Boolean isNormalForm,
      BooleanSupplier isNormalFormWork,
      Supplier<@Nullable NORMALISED> normalisationWork) {
    this.original = original;
    this.isNormalForm = isNormalForm;
    this.isNormalFormWork = isNormalFormWork;
    this.normalisationWork = normalisationWork;
    initialiseNormalised(original);
  }

  private void initialiseNormalised(NORMALISED original) {
    if (Boolean.TRUE.equals(this.isNormalForm)) {
      this.normalised = original;
    } else {
      this.normalised = null;
    }
  }

  public NORMALISED normalise() {
    NORMALISED localNormalised = this.normalised;
    if (localNormalised == null) {
      localNormalised = normalisationWork.get();
      if (localNormalised == null) {
        localNormalised = original;
        this.isNormalForm = true;
      } else {
        this.isNormalForm = false;
      }
      this.normalised = localNormalised;
    }
    return localNormalised;
  }

  public boolean isNormalForm() {
    var normalForm = isNormalForm;
    if (normalForm == null) {
      normalForm = isNormalFormWork.getAsBoolean();
      this.isNormalForm = normalForm;
      if (normalForm) {
        this.normalised = original;
      }
    }
    return normalForm;
  }
}
