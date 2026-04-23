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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemoisedNormalisableTests {

  @Nested
  class Normalise {

    @Test
    void returns_original_when_known_to_be_normal_form() {
      var original = new TestNormalisable("value");
      var normalised = new TestNormalisable("normalised");

      var memoised = new MemoisedNormalisable<>(original, true, () -> false, () -> normalised);

      assertThat(memoised.normalise()).isSameAs(original);
    }

    @Test
    void returns_normalised_when_normalisation_work_returns_new_instance() {
      var original = new TestNormalisable("value");
      var normalised = new TestNormalisable("normalised");

      var memoised = new MemoisedNormalisable<>(original, null, () -> false, () -> normalised);

      assertThat(memoised.normalise()).isSameAs(normalised);
    }

    @Test
    void returns_original_when_normalisation_work_returns_null() {
      var original = new TestNormalisable("value");

      var memoised = new MemoisedNormalisable<>(original, null, () -> true, () -> null);

      assertThat(memoised.normalise()).isSameAs(original);
    }

    @Test
    void memoises_result_and_only_calls_normalisation_work_once() {
      var original = new TestNormalisable("value");
      var normalised = new TestNormalisable("normalised");
      var callCount = new AtomicInteger(0);

      var memoised =
          new MemoisedNormalisable<>(
              original,
              null,
              () -> false,
              () -> {
                callCount.incrementAndGet();
                return normalised;
              });

      var result1 = memoised.normalise();
      var result2 = memoised.normalise();
      var result3 = memoised.normalise();

      assertThat(result1).isSameAs(normalised);
      assertThat(result2).isSameAs(normalised);
      assertThat(result3).isSameAs(normalised);
      assertThat(callCount.get()).isEqualTo(1);
    }

    @Test
    void sets_isNormalForm_to_true_when_normalisation_returns_null() {
      var original = new TestNormalisable("value");

      var memoised =
          new MemoisedNormalisable<>(
              original,
              null,
              () -> {
                throw new AssertionError("should not be called");
              },
              () -> null);

      memoised.normalise();

      assertThat(memoised.isNormalForm()).isTrue();
    }

    @Test
    void sets_isNormalForm_to_false_when_normalisation_returns_new_instance() {
      var original = new TestNormalisable("value");
      var normalised = new TestNormalisable("normalised");

      var memoised =
          new MemoisedNormalisable<>(
              original,
              null,
              () -> {
                throw new AssertionError("should not be called");
              },
              () -> normalised);

      memoised.normalise();

      assertThat(memoised.isNormalForm()).isFalse();
    }
  }

  @Nested
  class IsNormalForm {

    @Test
    void returns_true_when_known_to_be_normal_form() {
      var original = new TestNormalisable("value");

      var memoised =
          new MemoisedNormalisable<>(
              original,
              true,
              () -> {
                throw new AssertionError("should not be called");
              },
              () -> null);

      assertThat(memoised.isNormalForm()).isTrue();
    }

    @Test
    void returns_false_when_known_to_not_be_normal_form() {
      var original = new TestNormalisable("value");

      var memoised =
          new MemoisedNormalisable<>(
              original,
              false,
              () -> {
                throw new AssertionError("should not be called");
              },
              () -> null);

      assertThat(memoised.isNormalForm()).isFalse();
    }

    @Test
    void calls_isNormalFormWork_when_unknown_and_returns_result() {
      var original = new TestNormalisable("value");

      var memoisedTrue = new MemoisedNormalisable<>(original, null, () -> true, () -> null);

      var memoisedFalse = new MemoisedNormalisable<>(original, null, () -> false, () -> null);

      assertThat(memoisedTrue.isNormalForm()).isTrue();
      assertThat(memoisedFalse.isNormalForm()).isFalse();
    }

    @Test
    void memoises_result_and_only_calls_isNormalFormWork_once() {
      var original = new TestNormalisable("value");
      var callCount = new AtomicInteger(0);

      var memoised =
          new MemoisedNormalisable<>(
              original,
              null,
              () -> {
                callCount.incrementAndGet();
                return true;
              },
              () -> null);

      var result1 = memoised.isNormalForm();
      var result2 = memoised.isNormalForm();
      var result3 = memoised.isNormalForm();

      assertThat(result1).isTrue();
      assertThat(result2).isTrue();
      assertThat(result3).isTrue();
      assertThat(callCount.get()).isEqualTo(1);
    }

    @Test
    void populates_normalised_with_original_when_isNormalFormWork_returns_true() {
      var original = new TestNormalisable("value");
      var normalisationCallCount = new AtomicInteger(0);

      var memoised =
          new MemoisedNormalisable<>(
              original,
              null,
              () -> true,
              () -> {
                normalisationCallCount.incrementAndGet();
                return new TestNormalisable("should not be used");
              });

      // First check isNormalForm
      assertThat(memoised.isNormalForm()).isTrue();

      // Then normalise should return original without calling normalisation work
      assertThat(memoised.normalise()).isSameAs(original);
      assertThat(normalisationCallCount.get()).isEqualTo(0);
    }
  }

  @Nested
  class CrossMethodInteraction {

    @Test
    void normalise_first_then_isNormalForm_does_not_call_isNormalFormWork() {
      var original = new TestNormalisable("value");
      var normalised = new TestNormalisable("normalised");
      var isNormalFormWorkCallCount = new AtomicInteger(0);

      var memoised =
          new MemoisedNormalisable<>(
              original,
              null,
              () -> {
                isNormalFormWorkCallCount.incrementAndGet();
                return false;
              },
              () -> normalised);

      // Normalise first
      memoised.normalise();

      // isNormalForm should not call isNormalFormWork
      assertThat(memoised.isNormalForm()).isFalse();
      assertThat(isNormalFormWorkCallCount.get()).isEqualTo(0);
    }

    @Test
    void isNormalForm_true_first_then_normalise_does_not_call_normalisationWork() {
      var original = new TestNormalisable("value");
      var normalisationWorkCallCount = new AtomicInteger(0);

      var memoised =
          new MemoisedNormalisable<>(
              original,
              null,
              () -> true,
              () -> {
                normalisationWorkCallCount.incrementAndGet();
                return new TestNormalisable("should not be used");
              });

      // isNormalForm first
      assertThat(memoised.isNormalForm()).isTrue();

      // normalise should return original without calling normalisation work
      assertThat(memoised.normalise()).isSameAs(original);
      assertThat(normalisationWorkCallCount.get()).isEqualTo(0);
    }

    @Test
    void isNormalForm_false_first_then_normalise_still_calls_normalisationWork() {
      var original = new TestNormalisable("value");
      var normalised = new TestNormalisable("normalised");
      var normalisationWorkCallCount = new AtomicInteger(0);

      var memoised =
          new MemoisedNormalisable<>(
              original,
              null,
              () -> false,
              () -> {
                normalisationWorkCallCount.incrementAndGet();
                return normalised;
              });

      // isNormalForm first
      assertThat(memoised.isNormalForm()).isFalse();

      // normalise should still call normalisation work since we don't know the normalised value
      assertThat(memoised.normalise()).isSameAs(normalised);
      assertThat(normalisationWorkCallCount.get()).isEqualTo(1);
    }
  }

  @Nested
  class ConstructorWithoutIsNormalForm {

    @Test
    void uses_null_for_isNormalForm() {
      var original = new TestNormalisable("value");
      var isNormalFormWorkCallCount = new AtomicInteger(0);

      var memoised =
          new MemoisedNormalisable<>(
              original,
              () -> {
                isNormalFormWorkCallCount.incrementAndGet();
                return true;
              },
              () -> null);

      // Should call isNormalFormWork since isNormalForm was not provided
      assertThat(memoised.isNormalForm()).isTrue();
      assertThat(isNormalFormWorkCallCount.get()).isEqualTo(1);
    }
  }

  /** A simple test implementation of Normalisable for testing purposes. */
  private static class TestNormalisable implements Normalisable<TestNormalisable> {
    private final String value;

    TestNormalisable(String value) {
      this.value = value;
    }

    @Override
    public TestNormalisable normalise() {
      return this;
    }

    @Override
    public boolean isNormalForm() {
      return true;
    }

    @Override
    public String toString() {
      return value;
    }
  }
}
