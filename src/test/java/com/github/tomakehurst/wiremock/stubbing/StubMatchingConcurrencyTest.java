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

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.request;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static com.github.tomakehurst.wiremock.http.RequestMethod.DELETE;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static com.github.tomakehurst.wiremock.http.RequestMethod.isNoneOf;
import static com.github.tomakehurst.wiremock.http.RequestMethod.isOneOf;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static java.util.function.Predicate.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.MappingsSaver;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import com.github.tomakehurst.wiremock.store.InMemoryStubMappingStore;
import com.github.tomakehurst.wiremock.store.files.FileSourceBlobStore;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Hammers stub matching from many threads while other threads change the stubs, and checks that
 * every response is one the stubs could legitimately have given at some instant during the request.
 *
 * <p>Each resource has a request that exactly one target stub answers, plus a decoy that also
 * matches it but loses on priority, or on insertion order when the priorities tie, because it was
 * added first. The target is rewritten over and over in different shapes (exact URL, path only,
 * regex, template, any URL plus header, oneOf and noneOf methods, case-insensitive) and different
 * priorities, so it keeps moving between index buckets. Every version answers with its version
 * number, and every change is timestamped, so a reader can tell whether the version it saw was live
 * at some point while its request was in flight.
 *
 * <p>Each test runs for {@value #LOAD_SECONDS_PROPERTY} seconds (a system property), two by
 * default.
 */
class StubMatchingConcurrencyTest {

  private static final int RESOURCES = 32;
  private static final int NOISE_STUBS = 64;
  private static final int READERS = 8;
  private static final int MUTATORS = 4;
  private static final int DECOY_PRIORITY = 9;
  private static final int MAX_VIOLATIONS_REPORTED = 20;
  private static final String LOAD_SECONDS_PROPERTY = "stubMatchingConcurrencyTest.seconds";
  private static final Duration LOAD_DURATION =
      Duration.ofSeconds(Long.getLong(LOAD_SECONDS_PROPERTY, 2));
  private static final Duration PAUSE_BETWEEN_CHANGES = Duration.ofMillis(1);

  private final InMemoryStubMappingStore store = new InMemoryStubMappingStore();
  private final StubMappings stubMappings =
      new StoreBackedStubMappings(
          store,
          new InMemoryScenarios(),
          Map.of(),
          Map.of(),
          Map.of(),
          new FileSourceBlobStore(new SingleRootFileSource(".")),
          List.of(),
          Map.of(),
          MappingsSaver.NOOP);

  private final List<Resource> resources =
      IntStream.range(0, RESOURCES).mapToObj(Resource::new).toList();
  private final List<StubMapping> fixedStubs = new ArrayList<>();
  private final ConcurrentLinkedQueue<String> violations = new ConcurrentLinkedQueue<>();
  private final LongAdder requestsServed = new LongAdder();
  private final LongAdder changesMade = new LongAdder();

  @Test
  void servesTheLiveStubWhileEachStubIsEditedRemovedAndReAddedConcurrently() throws Exception {
    addFixedStubs();
    resources.forEach(resource -> change(resource, Mutation.ADD));

    runUnderLoad(
        this::observe,
        IntStream.range(0, MUTATORS)
            .<LongConsumer>mapToObj(
                mutator -> deadline -> mutateUntil(deadline, resourcesOwnedBy(mutator), mutator))
            .toList());

    assertNoViolations();
    resources.forEach(this::assertIndexAgreesWithStoreFor);
  }

  @Test
  void servesTheLiveStubsWhileAllStubsAreReplacedOrResetAtOnce() throws Exception {
    addFixedStubs();
    resources.forEach(resource -> change(resource, Mutation.ADD));

    runUnderLoad(this::observe, List.of(this::replaceAllStubsUntil));

    assertNoViolations();
    resources.forEach(this::assertIndexAgreesWithStoreFor);
  }

  @Test
  void indexAgreesWithStoreAfterConcurrentEditsOfTheSameStubs() throws Exception {
    addFixedStubs();
    resources.forEach(resource -> change(resource, Mutation.ADD));

    runUnderLoad(
        this::serve,
        IntStream.range(0, MUTATORS)
            .<LongConsumer>mapToObj(mutator -> deadline -> editUntil(deadline, resources, mutator))
            .toList());

    assertThat(changesMade.sum(), greaterThan(100L));
    resources.forEach(this::assertIndexAgreesWithStoreFor);
  }

  private void addFixedStubs() {
    IntStream.range(0, NOISE_STUBS)
        .forEach(
            i -> {
              addFixed(get(urlPathEqualTo("/noise/" + i)).willReturn(ok("noise")));
              addFixed(
                  post(urlPathTemplate("/api/r" + (i % RESOURCES) + "/items/{id}"))
                      .willReturn(ok("noise")));
              addFixed(any(anyUrl()).withHeader("X-Noise", equalTo("yes")).willReturn(ok("noise")));
            });
    resources.forEach(resource -> addFixed(resource.decoy()));
  }

  private void addFixed(MappingBuilder stub) {
    fixedStubs.add(stubMappings.addMapping(stub.build()));
  }

  private void runUnderLoad(Consumer<Resource> read, List<LongConsumer> mutators) throws Exception {
    long deadline = System.nanoTime() + LOAD_DURATION.toNanos();
    ExecutorService pool = Executors.newFixedThreadPool(READERS + mutators.size());
    try {
      List<Future<?>> futures = new ArrayList<>();
      IntStream.range(0, READERS)
          .forEach(reader -> futures.add(pool.submit(() -> readUntil(deadline, reader, read))));
      mutators.forEach(mutator -> futures.add(pool.submit(() -> mutator.accept(deadline))));
      for (Future<?> future : futures) {
        future.get();
      }
    } finally {
      pool.shutdownNow();
    }
  }

  private void readUntil(long deadline, int reader, Consumer<Resource> read) {
    Random random = new Random(reader);
    while (System.nanoTime() < deadline) {
      read.accept(resources.get(random.nextInt(resources.size())));
    }
  }

  private void mutateUntil(long deadline, List<Resource> owned, int mutator) {
    Random random = new Random(mutator);
    Mutation[] mutations = {Mutation.EDIT, Mutation.REMOVE_THEN_ADD, Mutation.UPDATE};
    while (System.nanoTime() < deadline) {
      change(owned.get(random.nextInt(owned.size())), mutations[random.nextInt(mutations.length)]);
      pauseBetweenChanges();
    }
  }

  private void editUntil(long deadline, List<Resource> shared, int mutator) {
    Random random = new Random(mutator);
    while (System.nanoTime() < deadline) {
      Resource resource = shared.get(random.nextInt(shared.size()));
      try {
        stubMappings.editMapping(resource.stubAtVersion(random.nextInt(Shape.values().length)));
        changesMade.increment();
      } catch (NotFoundException editRacedWithAnotherEditOfTheSameStub) {
      }
    }
  }

  private void replaceAllStubsUntil(long deadline) {
    for (int round = 0; System.nanoTime() < deadline; round++) {
      List<Change> changes = resources.stream().map(Resource::startChange).toList();
      List<StubMapping> targets = resources.stream().map(Resource::nextStub).toList();
      List<StubMapping> all = Stream.concat(fixedStubs.stream(), targets.stream()).toList();
      List<StubMapping> applied;
      if (round % 2 == 0) {
        applied = stubMappings.setAllMappings(all);
      } else {
        stubMappings.reset();
        applied = stubMappings.updateMappings(all);
      }
      List<StubMapping> appliedTargets = applied.subList(fixedStubs.size(), applied.size());
      IntStream.range(0, RESOURCES)
          .forEach(i -> resources.get(i).currentStub = appliedTargets.get(i));
      changes.forEach(Change::end);
      changesMade.add(RESOURCES);
      pauseBetweenChanges();
    }
  }

  private static void pauseBetweenChanges() {
    LockSupport.parkNanos(PAUSE_BETWEEN_CHANGES.toNanos());
  }

  private List<Resource> resourcesOwnedBy(int mutator) {
    return resources.stream().filter(resource -> resource.number % MUTATORS == mutator).toList();
  }

  private void change(Resource resource, Mutation mutation) {
    Change change = resource.startChange();
    resource.currentStub = mutation.apply(stubMappings, resource.currentStub, resource.nextStub());
    change.end();
    changesMade.increment();
  }

  private void observe(Resource resource) {
    long started = System.nanoTime();
    String body = serve(resource);
    long ended = System.nanoTime();
    String violation = resource.violationIn(body, started, ended);
    if (violation != null && violations.size() < MAX_VIOLATIONS_REPORTED) {
      violations.add(violation);
    }
  }

  private String serve(Resource resource) {
    ServeEvent served = stubMappings.serveFor(ServeEvent.of(resource.request(GET)));
    requestsServed.increment();
    return served.getResponseDefinition().getBody();
  }

  private void assertNoViolations() {
    assertThat(
        "After "
            + requestsServed.sum()
            + " requests and "
            + changesMade.sum()
            + " stub changes, responses that no live stub could have given: "
            + violations,
        violations,
        empty());
    assertThat(requestsServed.sum(), greaterThan(1_000L));
    assertThat(changesMade.sum(), greaterThan(100L));
  }

  private void assertIndexAgreesWithStoreFor(Resource resource) {
    assertThat(
        "target stub lost from the store",
        stubMappings.get(resource.currentStub.getId()).isPresent(),
        is(true));
    Stream.of(GET, PUT).forEach(method -> assertIndexAgreesWithStoreFor(resource.request(method)));
  }

  private void assertIndexAgreesWithStoreFor(Request request) {
    List<StubMapping> all = stubMappings.getAll();
    List<StubMapping> matching =
        all.stream().filter(stub -> stub.getRequest().isMatchedBy(request, Map.of())).toList();
    List<StubMapping> candidates = store.findCandidatesFor(request).toList();
    Set<StubMapping> inStore = identitySetOf(all);
    Set<StubMapping> inIndex = identitySetOf(candidates);
    List<String> ghosts = bodiesOf(candidates.stream().filter(not(inStore::contains)));
    List<String> missing = bodiesOf(matching.stream().filter(not(inIndex::contains)));

    assertThat("index holds stubs the store does not", ghosts, empty());
    assertThat("index omits stubs that match", missing, empty());
    assertThat(
        bodyOf(stubMappings.serveFor(ServeEvent.of(request)).getStubMapping()),
        is(bodyOf(matching.get(0))));
  }

  private static Set<StubMapping> identitySetOf(List<StubMapping> stubs) {
    Set<StubMapping> set = Collections.newSetFromMap(new IdentityHashMap<>());
    set.addAll(stubs);
    return set;
  }

  private static List<String> bodiesOf(Stream<StubMapping> stubs) {
    return stubs.map(StubMatchingConcurrencyTest::bodyOf).toList();
  }

  private static String bodyOf(StubMapping stub) {
    return stub.getResponse().getBody();
  }

  private enum Mutation {
    ADD {
      @Override
      StubMapping apply(StubMappings mappings, StubMapping current, StubMapping next) {
        return mappings.addMapping(next);
      }
    },
    EDIT {
      @Override
      StubMapping apply(StubMappings mappings, StubMapping current, StubMapping next) {
        return mappings.editMapping(next);
      }
    },
    UPDATE {
      @Override
      StubMapping apply(StubMappings mappings, StubMapping current, StubMapping next) {
        return mappings.updateMappings(List.of(next)).get(0);
      }
    },
    REMOVE_THEN_ADD {
      @Override
      StubMapping apply(StubMappings mappings, StubMapping current, StubMapping next) {
        mappings.removeMapping(current);
        return mappings.addMapping(next.transform(builder -> builder.setId(UUID.randomUUID())));
      }
    };

    abstract StubMapping apply(StubMappings mappings, StubMapping current, StubMapping next);
  }

  private enum Shape {
    URL_EQUAL_TO {
      @Override
      MappingBuilder stubFor(Resource resource) {
        return get(urlEqualTo(resource.url));
      }
    },
    URL_PATH_EQUAL_TO {
      @Override
      MappingBuilder stubFor(Resource resource) {
        return get(urlPathEqualTo(resource.path));
      }
    },
    URL_MATCHING {
      @Override
      MappingBuilder stubFor(Resource resource) {
        return get(urlMatching("/api/" + resource.name + "/items/\\d+\\?q=1"));
      }
    },
    URL_PATH_MATCHING {
      @Override
      MappingBuilder stubFor(Resource resource) {
        return get(urlPathMatching("^/api/" + resource.name + "/.*"));
      }
    },
    URL_PATH_TEMPLATE {
      @Override
      MappingBuilder stubFor(Resource resource) {
        return get(urlPathTemplate("/api/" + resource.name + "/items/{id}"))
            .withPathParam("id", equalTo(String.valueOf(resource.number)));
      }
    },
    ANY_URL_WITH_HEADER {
      @Override
      MappingBuilder stubFor(Resource resource) {
        return any(anyUrl()).withHeader("X-Resource", equalTo(resource.name));
      }
    },
    ONE_OF_METHODS {
      @Override
      MappingBuilder stubFor(Resource resource) {
        return request(isOneOf(GET, POST), urlPathEqualTo(resource.path));
      }
    },
    NONE_OF_METHODS {
      @Override
      MappingBuilder stubFor(Resource resource) {
        return request(isNoneOf(DELETE), urlPathEqualTo(resource.path));
      }
    },
    CASE_INSENSITIVE_PATH {
      @Override
      MappingBuilder stubFor(Resource resource) {
        return get(
            new UrlPathPattern(new EqualToPattern(resource.path.toUpperCase(), true), false));
      }
    },
    ALTERNATION {
      @Override
      MappingBuilder stubFor(Resource resource) {
        return get(urlPathMatching("/nowhere|" + resource.path));
      }
    };

    abstract MappingBuilder stubFor(Resource resource);
  }

  private static final class Change {
    private final int version;
    private final Change previous;
    private final long started = System.nanoTime();
    private volatile long ended = Long.MAX_VALUE;

    private Change(Change previous) {
      this.version = previous == null ? 0 : previous.version + 1;
      this.previous = previous;
    }

    private void end() {
      ended = System.nanoTime();
    }

    private boolean inFlightDuring(long started, long ended) {
      return this.started <= ended && started <= this.ended;
    }
  }

  private static final class Resource {
    private final int number;
    private final String name;
    private final String path;
    private final String url;
    private volatile Change latest;
    private volatile StubMapping currentStub;

    private Resource(int number) {
      this.number = number;
      this.name = "r" + number;
      this.path = "/api/" + name + "/items/" + number;
      this.url = path + "?q=1";
    }

    private MappingBuilder decoy() {
      MappingBuilder matcher =
          number % 2 == 0
              ? any(urlPathMatching("/api/" + name + "/.*"))
              : any(anyUrl()).withHeader("X-Resource", equalTo(name));
      return matcher.atPriority(DECOY_PRIORITY).willReturn(ok(name + ":decoy"));
    }

    private Request request(RequestMethod method) {
      return mockRequest().method(method).url(url).header("X-Resource", name).asLoggedRequest();
    }

    private Change startChange() {
      latest = new Change(latest);
      return latest;
    }

    private StubMapping nextStub() {
      return stubAtVersion(latest.version);
    }

    private StubMapping stubAtVersion(int version) {
      Shape[] shapes = Shape.values();
      return shapes[version % shapes.length]
          .stubFor(this)
          .withId(currentStub == null ? UUID.randomUUID() : currentStub.getId())
          .atPriority(1 + version % DECOY_PRIORITY)
          .willReturn(ok(name + ":v" + version))
          .build();
    }

    private String violationIn(String body, long started, long ended) {
      if (body == null || body.equals(name + ":decoy")) {
        return anyChangeInFlightDuring(started, ended)
            ? null
            : describe(body, started, ended, "no stub change was in flight");
      }
      if (!body.startsWith(name + ":v")) {
        return describe(body, started, ended, "belongs to another resource");
      }
      return violationInVersion(
          Integer.parseInt(body.substring(name.length() + 2)), started, ended);
    }

    private String violationInVersion(int version, long started, long ended) {
      Change next = null;
      Change seen = latest;
      while (seen != null && seen.version > version) {
        next = seen;
        seen = seen.previous;
      }
      if (seen == null || seen.version != version) {
        return describe("v" + version, started, ended, "was never recorded");
      }
      if (ended < seen.started) {
        return describe("v" + version, started, ended, "was served before its change started");
      }
      if (next != null && next.ended < started) {
        return describe(
            "v" + version, started, ended, "was served after v" + next.version + " replaced it");
      }
      return null;
    }

    private boolean anyChangeInFlightDuring(long started, long ended) {
      for (Change change = latest;
          change != null && change.ended >= started;
          change = change.previous) {
        if (change.inFlightDuring(started, ended)) {
          return true;
        }
      }
      return false;
    }

    private String describe(String body, long started, long ended, String problem) {
      return name
          + ": response "
          + body
          + " for request in ["
          + started
          + ", "
          + ended
          + "] "
          + problem
          + " (latest change v"
          + latest.version
          + " ["
          + latest.started
          + ", "
          + latest.ended
          + "])";
    }
  }
}
