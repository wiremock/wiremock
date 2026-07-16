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

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class ScenarioConcurrencyTest {

  @Test
  public void neverReturnsUnmatchedWhenOneScenarioIsAdvancedConcurrently() throws Exception {
    StubMappings stubMappings = new InMemoryStubMappings();
    addWrappingScenario(stubMappings);

    int threads = 8;
    int requestsPerThread = 5000;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    AtomicInteger unmatched = new AtomicInteger();

    try {
      List<Future<?>> futures = new ArrayList<>();
      for (int t = 0; t < threads; t++) {
        futures.add(
            pool.submit(
                () -> {
                  for (int i = 0; i < requestsPerThread; i++) {
                    ServeEvent served =
                        stubMappings.serveFor(
                            ServeEvent.of(
                                mockRequest()
                                    .method(POST)
                                    .url("/scenario/order")
                                    .asLoggedRequest()));
                    if (!served.getWasMatched()) {
                      unmatched.incrementAndGet();
                    }
                  }
                }));
      }
      for (Future<?> future : futures) {
        future.get();
      }
    } finally {
      pool.shutdownNow();
    }

    assertEquals(
        0,
        unmatched.get(),
        "Every POST /scenario/order should match one of the scenario's states; unmatched (404) "
            + "responses mean the scenario state was read inconsistently while it was advanced concurrently");
  }

  // Started -> PLACED -> SHIPPED -> DELIVERED -> Started, all on the same request.
  private static void addWrappingScenario(StubMappings stubMappings) {
    addTransition(stubMappings, STARTED, "PLACED");
    addTransition(stubMappings, "PLACED", "SHIPPED");
    addTransition(stubMappings, "SHIPPED", "DELIVERED");
    addTransition(stubMappings, "DELIVERED", STARTED);
  }

  private static void addTransition(StubMappings stubMappings, String from, String to) {
    stubMappings.addMapping(
        post(urlEqualTo("/scenario/order"))
            .inScenario("order")
            .whenScenarioStateIs(from)
            .willSetStateTo(to)
            .willReturn(ok())
            .build());
  }
}
