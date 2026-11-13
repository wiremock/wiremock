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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class InMemoryStubMappingStoreTest {

  private final InMemoryStubMappingStore store = new InMemoryStubMappingStore();

  @Test
  void replacingAStaleVersionOfAStubReplacesWhatIsStoredUnderItsId() {
    StubMapping original =
        store.add(get(urlEqualTo("/a")).atPriority(1).willReturn(ok("v1")).build());
    store.replace(original, versionOf(original, "v2", 2));

    store.replace(original, versionOf(original, "v3", 3));

    assertThat(bodiesOf(store.getAll()), contains("v3"));
    assertThat(bodiesOf(store.findCandidatesFor(requestFor("/a"))), contains("v3"));
  }

  @Test
  void replacingAStubThatIsNotStoredStoresNothing() {
    StubMapping neverAdded = get(urlEqualTo("/a")).willReturn(ok("v1")).build();

    store.replace(neverAdded, versionOf(neverAdded, "v2", 2));

    assertThat(store.getAll().toList(), empty());
    assertThat(store.findCandidatesFor(requestFor("/a")).toList(), empty());
  }

  @Test
  void candidatesFollowAReplacedStubToItsNewPath() {
    StubMapping onA = store.add(get(urlEqualTo("/a")).willReturn(ok("moved")).build());

    store.replace(
        onA,
        onA.transform(builder -> builder.setRequest(get(urlEqualTo("/b")).build().getRequest())));

    assertThat(bodiesOf(store.findCandidatesFor(requestFor("/b"))), contains("moved"));
    assertThat(store.findCandidatesFor(requestFor("/a")).toList(), empty());
  }

  @Test
  void removedStubsAreNoLongerCandidates() {
    StubMapping stub = store.add(get(urlEqualTo("/a")).willReturn(ok("gone")).build());

    store.remove(stub.getId());

    assertThat(store.findCandidatesFor(requestFor("/a")).toList(), empty());
  }

  @Test
  void clearedStubsAreNoLongerCandidates() {
    store.add(get(urlEqualTo("/a")).willReturn(ok("gone")).build());

    store.clear();

    assertThat(store.findCandidatesFor(requestFor("/a")).toList(), empty());
  }

  private static StubMapping versionOf(StubMapping stub, String body, int priority) {
    return stub.transform(builder -> builder.setPriority(priority).setResponse(ok(body).build()));
  }

  private static List<String> bodiesOf(Stream<StubMapping> stubs) {
    return stubs.map(stub -> stub.getResponse().getBody()).toList();
  }

  private static Request requestFor(String url) {
    return mockRequest().method(GET).url(url);
  }
}
