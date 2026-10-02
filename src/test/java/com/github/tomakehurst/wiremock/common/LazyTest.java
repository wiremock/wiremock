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

import static com.github.tomakehurst.wiremock.common.Lazy.lazy;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class LazyTest {

  @Test
  void initialisesFromSupplierOnlyOnce() {
    AtomicInteger count = new AtomicInteger(0);

    Lazy<String> lazy =
        lazy(
            () -> {
              count.incrementAndGet();
              return "Lazily";
            });

    lazy.get();
    lazy.get();

    assertThat(lazy.get()).isEqualTo("Lazily");
    assertThat(count.get()).isEqualTo(1);
  }

  @Test
  void initialisesFromSupplierOnlyOnceWhenThreadsRaceToInitialise() throws Exception {
    int threads = 8;
    ExecutorService pool = Executors.newFixedThreadPool(threads);

    try {
      for (int attempt = 0; attempt < 200; attempt++) {
        AtomicInteger count = new AtomicInteger(0);
        Lazy<String> lazy =
            lazy(
                () -> {
                  count.incrementAndGet();
                  // Real suppliers do enough work to be preempted part way through, e.g. building
                  // an HTTP client. Yielding here widens the window so that an implementation
                  // permitting a second invocation reliably shows it.
                  Thread.yield();
                  return "Lazily";
                });

        CountDownLatch allThreadsReady = new CountDownLatch(1);
        List<Future<String>> results = new ArrayList<>();
        for (int thread = 0; thread < threads; thread++) {
          results.add(
              pool.submit(
                  () -> {
                    allThreadsReady.await();
                    return lazy.get();
                  }));
        }
        allThreadsReady.countDown();

        for (Future<String> result : results) {
          assertThat(result.get()).isEqualTo("Lazily");
        }
        assertThat(count.get())
            .describedAs(
                "supplier invocations on attempt %d; more than one means racing callers each "
                    + "initialised the value, so a supplier that allocates a resource would leak "
                    + "everything but the last",
                attempt)
            .isEqualTo(1);
      }
    } finally {
      pool.shutdownNow();
    }
  }
}
