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

class MemoisedToStringTests {

  @Nested
  class ToString {

    @Test
    void returns_pre_computed_value_when_provided() {
      var memoised =
          new MemoisedToString(
              "pre-computed",
              () -> {
                throw new AssertionError("should not be called");
              });

      assertThat(memoised.toString()).isEqualTo("pre-computed");
    }

    @Test
    void computes_value_when_not_provided() {
      var memoised = new MemoisedToString(null, () -> "computed");

      assertThat(memoised.toString()).isEqualTo("computed");
    }

    @Test
    void memoises_result_and_only_calls_toStringWork_once() {
      var callCount = new AtomicInteger(0);

      var memoised =
          new MemoisedToString(
              null,
              () -> {
                callCount.incrementAndGet();
                return "computed";
              });

      var result1 = memoised.toString();
      var result2 = memoised.toString();
      var result3 = memoised.toString();

      assertThat(result1).isEqualTo("computed");
      assertThat(result2).isEqualTo("computed");
      assertThat(result3).isEqualTo("computed");
      assertThat(callCount.get()).isEqualTo(1);
    }

    @Test
    void does_not_call_toStringWork_when_value_provided() {
      var callCount = new AtomicInteger(0);

      var memoised =
          new MemoisedToString(
              "pre-computed",
              () -> {
                callCount.incrementAndGet();
                return "computed";
              });

      memoised.toString();
      memoised.toString();

      assertThat(callCount.get()).isEqualTo(0);
    }

    @Test
    void returns_same_instance_on_subsequent_calls() {
      var memoised = new MemoisedToString(null, () -> "computed");

      var result1 = memoised.toString();
      var result2 = memoised.toString();

      assertThat(result1).isSameAs(result2);
    }

    @Test
    void handles_empty_string_as_pre_computed_value() {
      var memoised =
          new MemoisedToString(
              "",
              () -> {
                throw new AssertionError("should not be called");
              });

      assertThat(memoised.toString()).isEmpty();
    }

    @Test
    void computes_empty_string_when_work_returns_empty() {
      var memoised = new MemoisedToString(null, () -> "");

      assertThat(memoised.toString()).isEmpty();
    }
  }
}
