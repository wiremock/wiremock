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
package com.github.tomakehurst.wiremock.stubbing;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import com.github.tomakehurst.wiremock.http.ImmutableRequest;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Measures the per-request cost of resolving scenario state while matching a stub.
 *
 * <p>Written against {@link StubMappings#serveFor} only, so that the same source compiles and runs
 * unchanged on either side of a change to how scenario state is read.
 *
 * <p>Stubs are matched in reverse insertion order, so each fixture adds the scenario-independent
 * stub last and targets the last-added scenario. The matching benchmarks therefore match within the
 * first few candidates, keeping request-matching cost (which is unaffected by scenario state
 * handling) from swamping the thing being measured.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(3)
public class ScenarioMatchingBenchmark {

  private static final String SCENARIO_INDEPENDENT_URL = "/health";
  private static final String UNMATCHABLE_URL = "/matches-no-stub";

  public enum Fixture {
    /** Hundreds of scenarios, hundreds of stubs: 200 scenarios x 3 states = 600 scenario stubs. */
    MANY(200, 3, 0),
    /** A handful of each: 2 scenarios x 5 states = 10 scenario stubs. */
    FEW(2, 5, 0),
    /**
     * No scenarios at all, but the same stub count as {@link #MANY}, so that comparing the two
     * isolates the cost of scenario count from the cost of stub count.
     */
    NONE(0, 0, 600);

    private final int scenarioCount;
    private final int statesPerScenario;
    private final int scenarioIndependentStubCount;

    Fixture(int scenarioCount, int statesPerScenario, int scenarioIndependentStubCount) {
      this.scenarioCount = scenarioCount;
      this.statesPerScenario = statesPerScenario;
      this.scenarioIndependentStubCount = scenarioIndependentStubCount;
    }

    private StubMappings build() {
      StubMappings stubMappings = new InMemoryStubMappings();
      IntStream.range(0, scenarioCount).forEach(index -> addScenario(stubMappings, index));
      IntStream.range(0, scenarioIndependentStubCount)
          .forEach(
              index ->
                  stubMappings.addMapping(
                      post(urlEqualTo("/plain/" + index)).willReturn(ok()).build()));
      stubMappings.addMapping(get(urlEqualTo(SCENARIO_INDEPENDENT_URL)).willReturn(ok()).build());
      return stubMappings;
    }

    /** A scenario whose states form a loop, so repeatedly serving it never runs out of states. */
    private void addScenario(StubMappings stubMappings, int scenarioIndex) {
      IntStream.range(0, statesPerScenario)
          .forEach(
              stateIndex ->
                  stubMappings.addMapping(
                      post(urlEqualTo(urlOfScenario(scenarioIndex)))
                          .inScenario("scenario-" + scenarioIndex)
                          .whenScenarioStateIs(stateName(stateIndex))
                          .willSetStateTo(stateName((stateIndex + 1) % statesPerScenario))
                          .willReturn(ok())
                          .build()));
    }

    private String stateName(int stateIndex) {
      return stateIndex == 0 ? STARTED : "state-" + stateIndex;
    }

    private String urlOfScenario(int scenarioIndex) {
      return "/scenario/" + scenarioIndex + "/next";
    }
  }

  /** Fixtures for the benchmarks that do not need a scenario to exist. */
  @State(Scope.Benchmark)
  public static class AnyFixture {

    @Param({"MANY", "FEW", "NONE"})
    public Fixture fixture;

    StubMappings stubMappings;
    LoggedRequest scenarioIndependentRequest;
    LoggedRequest unmatchableRequest;

    @Setup(Level.Trial)
    public void setUp() {
      stubMappings = fixture.build();
      scenarioIndependentRequest = requestFor(RequestMethod.GET, SCENARIO_INDEPENDENT_URL);
      unmatchableRequest = requestFor(RequestMethod.GET, UNMATCHABLE_URL);

      requireMatched(stubMappings.serveFor(ServeEvent.of(scenarioIndependentRequest)));
      requireUnmatched(stubMappings.serveFor(ServeEvent.of(unmatchableRequest)));
    }
  }

  /** Fixtures that contain at least one scenario, so that a scenario stub can be matched. */
  @State(Scope.Benchmark)
  public static class ScenarioFixture {

    @Param({"MANY", "FEW"})
    public Fixture fixture;

    StubMappings stubMappings;
    LoggedRequest scenarioRequest;

    @Setup(Level.Trial)
    public void setUp() {
      stubMappings = fixture.build();
      scenarioRequest =
          requestFor(RequestMethod.POST, fixture.urlOfScenario(fixture.scenarioCount - 1));

      requireMatched(stubMappings.serveFor(ServeEvent.of(scenarioRequest)));
    }
  }

  /** Cost of matching a request that is constrained by scenario state. */
  @Benchmark
  public ServeEvent matchScenarioStub(ScenarioFixture state) {
    return state.stubMappings.serveFor(ServeEvent.of(state.scenarioRequest));
  }

  /** Cost paid by a request whose stub has nothing to do with any scenario. */
  @Benchmark
  public ServeEvent matchScenarioIndependentStub(AnyFixture state) {
    return state.stubMappings.serveFor(ServeEvent.of(state.scenarioIndependentRequest));
  }

  /**
   * Cost of a request that matches nothing, and so is scanned against every stub. Reference point
   * for how expensive it is when concurrent scenario advancement strands a request that should have
   * matched.
   */
  @Benchmark
  public ServeEvent failToMatchAnyStub(AnyFixture state) {
    return state.stubMappings.serveFor(ServeEvent.of(state.unmatchableRequest));
  }

  private static LoggedRequest requestFor(RequestMethod method, String url) {
    return LoggedRequest.createFrom(
        ImmutableRequest.create()
            .withAbsoluteUrl("http://localhost" + url)
            .withMethod(method)
            .withProtocol("HTTP/1.1")
            .withClientIp("127.0.0.1")
            .build());
  }

  private static void requireMatched(ServeEvent serveEvent) {
    if (!serveEvent.getWasMatched()) {
      throw new IllegalStateException(
          "Benchmark fixture is wrong: "
              + serveEvent.getRequest().getUrl()
              + " matched no stub, so the benchmark would measure the unmatched path");
    }
  }

  private static void requireUnmatched(ServeEvent serveEvent) {
    if (serveEvent.getWasMatched()) {
      throw new IllegalStateException(
          "Benchmark fixture is wrong: "
              + serveEvent.getRequest().getUrl()
              + " was expected to match no stub");
    }
  }
}
