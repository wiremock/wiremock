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
package com.github.tomakehurst.wiremock.store;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.request;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;

import com.github.tomakehurst.wiremock.http.ImmutableRequest;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;
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
 * Compares finding the first matching stub with and without the stub index, across stub counts,
 * stub layouts and where the matching stub sits in priority order, to show the stub count above
 * which the index pays for itself.
 *
 * <p>Each stub also requires its own {@code X-Stub} header value, so exactly one stub matches even
 * when every stub shares a path and method.
 *
 * <p>Run with e.g. {@code ./gradlew :wiremock-core:jmhRun --args="StubMatchingBenchmark -rf json
 * -rff stub-matching.json"}
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class StubMatchingBenchmark {

  private static final RequestMethod[] METHODS = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE
  };

  public enum Store {
    INDEXED {
      @Override
      StubMappingStore create() {
        return new InMemoryStubMappingStore();
      }
    },
    UNINDEXED {
      @Override
      StubMappingStore create() {
        return new InMemoryStubMappingStore() {
          @Override
          public Stream<StubMapping> findCandidatesFor(Request request) {
            return getAll();
          }
        };
      }
    };

    abstract StubMappingStore create();
  }

  public enum Layout {
    /** Every stub has its own path, with methods spread evenly: the index's best case. */
    SPREAD {
      @Override
      RequestMethod methodOf(int stub) {
        return METHODS[stub % METHODS.length];
      }

      @Override
      UrlPattern urlPatternOf(int stub) {
        return urlPathEqualTo(urlOf(stub));
      }

      @Override
      String urlOf(int stub) {
        return "/service-" + stub + "/items";
      }
    },
    /** Every stub shares one path template and method, so the index cannot narrow anything. */
    CLUSTERED {
      @Override
      RequestMethod methodOf(int stub) {
        return RequestMethod.GET;
      }

      @Override
      UrlPattern urlPatternOf(int stub) {
        return urlPathTemplate("/api/items/{id}");
      }

      @Override
      String urlOf(int stub) {
        return "/api/items/" + stub;
      }
    },
    /**
     * Mostly exact paths spread across methods, with every tenth stub a regex that has no literal
     * start and accepts any method, so the index has to merge an exact-path bucket with the
     * catch-all bucket for every request.
     */
    MIXED {
      @Override
      RequestMethod methodOf(int stub) {
        return isCatchAll(stub) ? RequestMethod.ANY : METHODS[stub % METHODS.length];
      }

      @Override
      UrlPattern urlPatternOf(int stub) {
        return isCatchAll(stub) ? urlMatching(".*" + urlOf(stub)) : urlPathEqualTo(urlOf(stub));
      }

      @Override
      String urlOf(int stub) {
        return "/service-" + stub + "/items";
      }
    };

    private static boolean isCatchAll(int stub) {
      return stub % 10 == 5;
    }

    abstract RequestMethod methodOf(int stub);

    abstract UrlPattern urlPatternOf(int stub);

    abstract String urlOf(int stub);
  }

  public enum Target {
    /** The last stub added, which is first in priority order. */
    FIRST_IN_ORDER,
    /** The first stub added, which is last in priority order. */
    LAST_IN_ORDER,
    /** A stub has the request's path and method but not its header, so no stub matches. */
    NEAR_MISS,
    /** Nothing shares the request's path or method, so only catch-all stubs are tried. */
    COMPLETE_MISS
  }

  @Param({"1", "10", "100", "1000", "10000"})
  public int stubCount;

  @Param({"SPREAD", "CLUSTERED", "MIXED"})
  public Layout layout;

  @Param({"FIRST_IN_ORDER", "LAST_IN_ORDER", "NEAR_MISS", "COMPLETE_MISS"})
  public Target target;

  @Param({"INDEXED", "UNINDEXED"})
  public Store store;

  private StubMappingStore stubMappingStore;
  private Request request;

  @Setup(Level.Trial)
  public void setUp() {
    stubMappingStore = store.create();
    IntStream.range(0, stubCount).forEach(stub -> stubMappingStore.add(stubFor(stub)));
    request = requestFor(target);

    boolean expectMatch = target == Target.FIRST_IN_ORDER || target == Target.LAST_IN_ORDER;
    if (findFirstMatch().isPresent() != expectMatch) {
      throw new IllegalStateException(
          "Benchmark fixture is wrong: expected "
              + (expectMatch ? "a" : "no")
              + " match for "
              + target);
    }
  }

  @Benchmark
  public Optional<StubMapping> findFirstMatch() {
    return stubMappingStore.findAllMatchingRequest(request, Map.of(), subEvent -> {}).findFirst();
  }

  private StubMapping stubFor(int stub) {
    return request(layout.methodOf(stub).getName(), layout.urlPatternOf(stub))
        .withHeader("X-Stub", equalTo(String.valueOf(stub)))
        .willReturn(ok())
        .build();
  }

  private Request requestFor(Target target) {
    return switch (target) {
      case FIRST_IN_ORDER -> requestFor(stubCount - 1);
      case LAST_IN_ORDER -> requestFor(0);
      case NEAR_MISS -> requestFor(layout.methodOf(0), layout.urlOf(0), -1);
      case COMPLETE_MISS -> requestFor(RequestMethod.PATCH, "/nothing", -1);
    };
  }

  private Request requestFor(int stub) {
    return requestFor(layout.methodOf(stub), layout.urlOf(stub), stub);
  }

  private static Request requestFor(RequestMethod method, String url, int stub) {
    return ImmutableRequest.create()
        .withAbsoluteUrl("http://localhost" + url)
        .withMethod(method)
        .withHeader("X-Stub", String.valueOf(stub))
        .withProtocol("HTTP/1.1")
        .withClientIp("127.0.0.1")
        .build();
  }
}
