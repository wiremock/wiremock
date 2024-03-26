/*
 * Copyright (C) 2024 Thomas Akehurst
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
package benchmarks;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.stream.Collectors.toUnmodifiableList;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.openjdk.jmh.annotations.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2)
@Fork(1)
@Measurement(iterations = 5)
public class PathAndMethodMatchingBenchmark {

  static final List<String> IDS =
      Stream.iterate((String) null, ignored -> UUID.randomUUID().toString())
          .filter(Objects::nonNull)
          .limit(1000)
          .collect(toUnmodifiableList());

  @State(Scope.Benchmark)
  public static class PathAndMethodBenchmarkState {
    private WireMockServer wm;
    private WireMockTestClient client;

    @Setup
    public void setup() {
      wm =
          new WireMockServer(
              wireMockConfig().dynamicPort().disableRequestJournal().containerThreads(100));
      wm.start();
      client = new WireMockTestClient(wm.port());

      for (String id : IDS) {
        wm.stubFor(get("/things/" + id).willReturn(ok("GET " + id)));
        wm.stubFor(post("/things/" + id).willReturn(ok("POST " + id)));
      }
    }

    @TearDown
    public void tearDown() {
      wm.stop();
    }
  }

  @Benchmark
  @Threads(50)
  public boolean matched(PathAndMethodBenchmarkState state) {
    final String id = pickRandom(IDS);
    String get = state.client.get("/things/" + id).content();
    String post = state.client.postJson("/things/" + id, "{}").content();
    return get.equals("GET " + id) && post.equals("POST " + id);
  }

  private static String pickRandom(List<String> values) {
    return values.get((int) (Math.random() * values.size()));
  }
}
