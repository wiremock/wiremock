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
package com.github.tomakehurst.wiremock.common;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Isolates the cost of {@link Lazy#get()} in the two ways it is used: on a long-lived instance,
 * where initialisation is amortised away, and on an instance created per request, where every call
 * pays whatever initialisation costs.
 *
 * <p>The freshly created instance is published to a {@link Blackhole} before {@code get()} is
 * called, so that escape analysis cannot elide the lock. In real use the instance escapes into a
 * stream predicate, so eliding it here would flatter the measurement.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(3)
@State(Scope.Benchmark)
public class LazyBenchmark {

  private static final Supplier<String> SUPPLIER = () -> "value";

  private Lazy<String> alreadyInitialised;

  @Setup(Level.Trial)
  public void setUp() {
    alreadyInitialised = Lazy.lazy(SUPPLIER);
    alreadyInitialised.get();
  }

  /** The steady state for a long-lived Lazy: initialisation has already happened. */
  @Benchmark
  public String getAlreadyInitialised() {
    return alreadyInitialised.get();
  }

  /** The per-request pattern, where initialisation cost is paid on every call. */
  @Benchmark
  public void createAndGet(Blackhole blackhole) {
    Lazy<String> lazy = Lazy.lazy(SUPPLIER);
    blackhole.consume(lazy);
    blackhole.consume(lazy.get());
  }
}
