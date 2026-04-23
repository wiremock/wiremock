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
import static org.wiremock.url.Lazy.lazy;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class LazyTests {

  @Test
  void returns_pre_computed_value_when_provided() {
    var memoised =
        lazy(
            "pre-computed",
            () -> {
              throw new AssertionError("should not be called");
            });

    assertThat(memoised.get()).isEqualTo("pre-computed");
  }

  @Test
  void computes_value_when_not_provided() {
    var memoised = lazy(null, () -> "computed");

    assertThat(memoised.get()).isEqualTo("computed");
  }

  @Test
  void memoises_result_and_only_calls_toStringWork_once() {
    var callCount = new AtomicInteger(0);

    var memoised =
        lazy(
            null,
            () -> {
              callCount.incrementAndGet();
              return "computed";
            });

    var result1 = memoised.get();
    var result2 = memoised.get();
    var result3 = memoised.get();

    assertThat(result1).isEqualTo("computed");
    assertThat(result2).isEqualTo("computed");
    assertThat(result3).isEqualTo("computed");
    assertThat(callCount.get()).isEqualTo(1);
  }

  @Test
  void does_not_call_toStringWork_when_value_provided() {
    var callCount = new AtomicInteger(0);

    var memoised =
        lazy(
            "pre-computed",
            () -> {
              callCount.incrementAndGet();
              return "computed";
            });

    memoised.get();
    memoised.get();

    assertThat(callCount.get()).isEqualTo(0);
  }

  @Test
  void returns_same_instance_on_subsequent_calls() {
    var memoised = lazy(null, () -> "computed");

    var result1 = memoised.get();
    var result2 = memoised.get();

    assertThat(result1).isSameAs(result2);
  }

  @Test
  void handles_empty_string_as_pre_computed_value() {
    var memoised =
        lazy(
            "",
            () -> {
              throw new AssertionError("should not be called");
            });

    assertThat(memoised.get()).isEmpty();
  }

  @Test
  void computes_empty_string_when_work_returns_empty() {
    var memoised = lazy(null, () -> "");

    assertThat(memoised.get()).isEmpty();
  }
}
