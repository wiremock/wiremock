/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.listAllStubMappings;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStubs;
import static com.github.tomakehurst.wiremock.client.WireMock.saveAllMappings;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RemoveStubMappingsAcceptanceTest extends AcceptanceTestBase {

  File rootDir;

  @BeforeEach
  @Override
  public void init() throws InterruptedException {
    rootDir = setupServerWithTempFileRoot();
  }

  @AfterEach
  void cleanup() {
    serverShutdown();
  }

  @Test
  void removeStubsThatExistUsingUUID() {
    StubMapping stub1 =
        stubFor(
            get(urlEqualTo("/stub-1")).withName("stub 1").willReturn(aResponse().withStatus(200)));
    StubMapping stub2 =
        stubFor(
            get(urlEqualTo("/stub-2")).withName("stub 2").willReturn(aResponse().withStatus(201)));
    StubMapping stub3 =
        stubFor(
            get(urlEqualTo("/stub-3")).withName("stub 3").willReturn(aResponse().withStatus(202)));

    saveAllMappings();

    assertThat(testClient.get("/stub-1").statusCode(), is(200));
    assertThat(testClient.get("/stub-2").statusCode(), is(201));
    assertThat(testClient.get("/stub-3").statusCode(), is(202));
    assertEquals(1, matchingStubCount("/stub-1"));
    assertEquals(1, matchingStubCount("/stub-2"));
    assertEquals(1, matchingStubCount("/stub-3"));
    File mappingsDir = rootDir.toPath().resolve(WireMockApp.MAPPINGS_ROOT).toFile();
    assertThat(
        Arrays.stream(Objects.requireNonNull(mappingsDir.listFiles())).toList(),
        containsInAnyOrder(
            new File(mappingsDir, "stub-1-" + stub1.getId() + ".json"),
            new File(mappingsDir, "stub-2-" + stub2.getId() + ".json"),
            new File(mappingsDir, "stub-3-" + stub3.getId() + ".json")));

    removeStubs(
        List.of(
            get("/whatever").withId(stub1.getId()).build(),
            get("/whatever").withId(stub3.getId()).build()));

    assertThat(testClient.get("/stub-1").statusCode(), is(404));
    assertThat(testClient.get("/stub-2").statusCode(), is(201));
    assertThat(testClient.get("/stub-3").statusCode(), is(404));
    assertEquals(0, matchingStubCount("/stub-1"));
    assertEquals(1, matchingStubCount("/stub-2"));
    assertEquals(0, matchingStubCount("/stub-3"));
    assertThat(
        Arrays.stream(Objects.requireNonNull(mappingsDir.listFiles())).toList(),
        containsInAnyOrder(new File(mappingsDir, "stub-2-" + stub2.getId() + ".json")));
  }

  @Test
  void removeStubsThatExistUsingRequestMatch() {
    StubMapping stub1 =
        stubFor(
            get(urlEqualTo("/stub-1")).withName("stub 1").willReturn(aResponse().withStatus(200)));
    StubMapping stub2 =
        stubFor(
            get(urlEqualTo("/stub-2")).withName("stub 2").willReturn(aResponse().withStatus(201)));
    StubMapping stub3 =
        stubFor(
            get(urlEqualTo("/stub-3")).withName("stub 3").willReturn(aResponse().withStatus(202)));

    saveAllMappings();

    assertThat(testClient.get("/stub-1").statusCode(), is(200));
    assertThat(testClient.get("/stub-2").statusCode(), is(201));
    assertThat(testClient.get("/stub-3").statusCode(), is(202));
    assertEquals(1, matchingStubCount("/stub-1"));
    assertEquals(1, matchingStubCount("/stub-2"));
    assertEquals(1, matchingStubCount("/stub-3"));
    File mappingsDir = rootDir.toPath().resolve(WireMockApp.MAPPINGS_ROOT).toFile();
    assertThat(
        Arrays.stream(Objects.requireNonNull(mappingsDir.listFiles())).toList(),
        containsInAnyOrder(
            new File(mappingsDir, "stub-1-" + stub1.getId() + ".json"),
            new File(mappingsDir, "stub-2-" + stub2.getId() + ".json"),
            new File(mappingsDir, "stub-3-" + stub3.getId() + ".json")));

    removeStubs(
        List.of(
            get(urlEqualTo("/stub-1")).withId(UUID.randomUUID()).build(),
            get(urlEqualTo("/stub-3")).withId(UUID.randomUUID()).build()));

    assertThat(testClient.get("/stub-1").statusCode(), is(404));
    assertThat(testClient.get("/stub-2").statusCode(), is(201));
    assertThat(testClient.get("/stub-3").statusCode(), is(404));
    assertEquals(0, matchingStubCount("/stub-1"));
    assertEquals(1, matchingStubCount("/stub-2"));
    assertEquals(0, matchingStubCount("/stub-3"));
    assertThat(
        Arrays.stream(Objects.requireNonNull(mappingsDir.listFiles())).toList(),
        containsInAnyOrder(new File(mappingsDir, "stub-2-" + stub2.getId() + ".json")));
  }

  @Test
  void removesNothingWhenNoneOfTheStubsExist() {
    StubMapping stub1 =
        stubFor(
            get(urlEqualTo("/stub-1")).withName("stub 1").willReturn(aResponse().withStatus(200)));
    StubMapping stub2 =
        stubFor(
            get(urlEqualTo("/stub-2")).withName("stub 2").willReturn(aResponse().withStatus(201)));

    saveAllMappings();

    assertThat(testClient.get("/stub-1").statusCode(), is(200));
    assertThat(testClient.get("/stub-2").statusCode(), is(201));
    assertEquals(1, matchingStubCount("/stub-1"));
    assertEquals(1, matchingStubCount("/stub-2"));
    File mappingsDir = rootDir.toPath().resolve(WireMockApp.MAPPINGS_ROOT).toFile();
    assertThat(
        Arrays.stream(Objects.requireNonNull(mappingsDir.listFiles())).toList(),
        containsInAnyOrder(
            new File(mappingsDir, "stub-1-" + stub1.getId() + ".json"),
            new File(mappingsDir, "stub-2-" + stub2.getId() + ".json")));

    removeStubs(
        List.of(
            get(urlEqualTo("/whatever")).withId(UUID.randomUUID()).build(),
            get(urlEqualTo("/whatever")).withId(UUID.randomUUID()).build()));

    assertThat(testClient.get("/stub-1").statusCode(), is(200));
    assertThat(testClient.get("/stub-2").statusCode(), is(201));
    assertEquals(1, matchingStubCount("/stub-1"));
    assertEquals(1, matchingStubCount("/stub-2"));
    assertThat(
        Arrays.stream(Objects.requireNonNull(mappingsDir.listFiles())).toList(),
        containsInAnyOrder(
            new File(mappingsDir, "stub-1-" + stub1.getId() + ".json"),
            new File(mappingsDir, "stub-2-" + stub2.getId() + ".json")));
  }

  @Test
  void removesOnlyTheStubsThatExist() {
    StubMapping stub1 =
        stubFor(
            get(urlEqualTo("/stub-1")).withName("stub 1").willReturn(aResponse().withStatus(200)));
    StubMapping stub2 =
        stubFor(
            get(urlEqualTo("/stub-2")).withName("stub 2").willReturn(aResponse().withStatus(201)));
    StubMapping stub3 =
        stubFor(
            get(urlEqualTo("/stub-3")).withName("stub 3").willReturn(aResponse().withStatus(202)));

    saveAllMappings();

    assertThat(testClient.get("/stub-1").statusCode(), is(200));
    assertThat(testClient.get("/stub-2").statusCode(), is(201));
    assertThat(testClient.get("/stub-3").statusCode(), is(202));
    assertEquals(1, matchingStubCount("/stub-1"));
    assertEquals(1, matchingStubCount("/stub-2"));
    assertEquals(1, matchingStubCount("/stub-3"));
    File mappingsDir = rootDir.toPath().resolve(WireMockApp.MAPPINGS_ROOT).toFile();
    assertThat(
        Arrays.stream(Objects.requireNonNull(mappingsDir.listFiles())).toList(),
        containsInAnyOrder(
            new File(mappingsDir, "stub-1-" + stub1.getId() + ".json"),
            new File(mappingsDir, "stub-2-" + stub2.getId() + ".json"),
            new File(mappingsDir, "stub-3-" + stub3.getId() + ".json")));

    removeStubs(
        List.of(
            get(urlEqualTo("/whatever")).withId(UUID.randomUUID()).build(),
            get(urlEqualTo("/whatever")).withId(stub3.getId()).build()));

    assertThat(testClient.get("/stub-1").statusCode(), is(200));
    assertThat(testClient.get("/stub-2").statusCode(), is(201));
    assertThat(testClient.get("/stub-3").statusCode(), is(404));
    assertEquals(1, matchingStubCount("/stub-1"));
    assertEquals(1, matchingStubCount("/stub-2"));
    assertEquals(0, matchingStubCount("/stub-3"));
    assertThat(
        Arrays.stream(Objects.requireNonNull(mappingsDir.listFiles())).toList(),
        containsInAnyOrder(
            new File(mappingsDir, "stub-1-" + stub1.getId() + ".json"),
            new File(mappingsDir, "stub-2-" + stub2.getId() + ".json")));
  }

  private synchronized long matchingStubCount(String url) {
    return listAllStubMappings().getMappings().stream()
        .filter(stub -> stub.getRequest().getUrl().equals(url))
        .count();
  }
}
