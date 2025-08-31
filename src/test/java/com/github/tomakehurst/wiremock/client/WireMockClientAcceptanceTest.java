/*
 * Copyright (C) 2011-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.admin.model.ServeEventQuery;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.networknt.schema.SpecVersion;
import java.util.List;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WireMockClientAcceptanceTest {

  private WireMockServer wireMockServer;
  private WireMockTestClient testClient;

  @BeforeEach
  public void init() {
    wireMockServer = new WireMockServer(Options.DYNAMIC_PORT);
    wireMockServer.start();
    WireMock.configureFor(wireMockServer.port());
    testClient = new WireMockTestClient(wireMockServer.port());
  }

  @AfterEach
  public void stopServer() {
    wireMockServer.stop();
  }

  @Test
  void buildsMappingWithUrlOnlyRequestAndStatusOnlyResponse() {
    WireMock wireMock = WireMock.create().port(wireMockServer.port()).build();
    wireMock.register(get(urlEqualTo("/my/new/resource")).willReturn(aResponse().withStatus(304)));

    assertThat(testClient.get("/my/new/resource").statusCode(), is(304));
  }

  @Test
  void buildsMappingFromStaticSyntax() {
    givenThat(get(urlEqualTo("/my/new/resource")).willReturn(aResponse().withStatus(304)));

    assertThat(testClient.get("/my/new/resource").statusCode(), is(304));
  }

  @Test
  void buildsMappingWithUrlOnyRequestAndResponseWithJsonBodyWithDiacriticSigns() {
    WireMock wireMock = WireMock.create().port(wireMockServer.port()).build();
    wireMock.register(
        get(urlEqualTo("/my/new/resource"))
            .willReturn(
                aResponse().withBody("{\"address\":\"Puerto Banús, Málaga\"}").withStatus(200)));

    assertThat(
        testClient.get("/my/new/resource").content(), is("{\"address\":\"Puerto Banús, Málaga\"}"));
  }

  @Test
  void testGetOrHeadRequestWhenGetMatchesShouldReturnAResponseBody() {
    String path = "/get-or-head-test";
    WireMock wireMock = WireMock.create().port(wireMockServer.port()).build();
    wireMock.register(
        getOrHead(urlEqualTo(path))
            .willReturn(okJson("{\"key\": \"value\"}").withHeader("Content-Length", "16")));

    WireMockResponse response = testClient.get(path);

    assertThat(response.statusCode(), is(200));
    assertThat(response.firstHeader("Content-Type"), is("application/json"));
    assertThat(response.firstHeader("Content-Length"), is("16"));
    assertThat(response.content(), not(emptyOrNullString()));
  }

  @Test
  void testGetOrHeadRequestWhenHeadMatchesShouldNotReturnAResponseBody() {
    String path = "/get-or-head-test";
    WireMock wireMock = WireMock.create().port(wireMockServer.port()).build();

    wireMock.register(
        getOrHead(urlEqualTo(path))
            .willReturn(
                okJson("{\"key\": \"value\"}")
                    .withHeader("Content-Type", "application/json")
                    .withHeader("Content-Length", "16")));
    WireMockResponse response = testClient.head(path);

    assertThat(response.statusCode(), is(200));
    assertThat(response.firstHeader("Content-Type"), is("application/json"));
    assertThat(response.firstHeader("Content-Length"), is("16"));
    assertThat(response.content(), is(emptyOrNullString()));
  }

  @Test
  void testGetOrHeadRequestWhenNoMethodNotMatchesShouldReturn404() {
    String path = "/get-or-head-test";
    WireMock wireMock = WireMock.create().port(wireMockServer.port()).build();
    wireMock.register(
        getOrHead(urlEqualTo(path))
            .willReturn(ok().withHeader("Content-Type", "application/json")));
    WireMockResponse response = testClient.delete(path);

    assertThat(response.statusCode(), is(404));
  }

  @Test
  void testGetOrHeadRequestWhenPathDoesNotMatchShouldReturn404() {
    String correctPath = "/get-or-head-path-correct";
    String incorrectPath = "/get-or-head-path-incorrect";
    WireMock wireMock = WireMock.create().port(wireMockServer.port()).build();
    wireMock.register(
        getOrHead(urlEqualTo(correctPath))
            .willReturn(ok().withHeader("Content-Type", "application/json")));
    WireMockResponse responseGet = testClient.get(incorrectPath);
    WireMockResponse responseHead = testClient.head(incorrectPath);

    assertThat(responseGet.statusCode(), is(404));
    assertThat(responseHead.statusCode(), is(404));
  }

  @Test
  void supportsConfiguringDefaultInstanceFromClient() {
    WireMock client = WireMock.create().port(wireMockServer.port()).build();
    WireMock.configureFor(client);

    givenThat(get(urlEqualTo("/configured/by/client")).willReturn(ok()));

    assertThat(testClient.get("/configured/by/client").statusCode(), is(200));
  }

  @Test
  void stubsUsingUrlPathTemplate() {
    WireMock wireMock = WireMock.create().port(wireMockServer.port()).build();
    wireMock.register(get(urlPathTemplate("/v1/contacts/{contactId}")).willReturn(ok()));

    assertThat(testClient.get("/v1/contacts/123").statusCode(), is(200));
    assertThat(testClient.get("/v1/contacts").statusCode(), is(404));
  }

  @Test
  void canRemoveServeEventById() {
    assertThat(WireMock.getAllServeEvents().size(), is(0));

    testClient.get("/will-be-removed");

    List<ServeEvent> events = WireMock.getAllServeEvents();
    assertThat(events.size(), is(1));

    WireMock.removeServeEvent(events.get(0).getId());

    assertThat(WireMock.getAllServeEvents().size(), is(0));
  }

  @Test
  void canQueryOnlyUnmatchedServeEvents() {
    givenThat(get(urlEqualTo("/hit")).willReturn(ok()));

    testClient.get("/hit");
    testClient.get("/missed");

    List<ServeEvent> unmatched = WireMock.getAllServeEvents(ServeEventQuery.ALL_UNMATCHED);
    assertThat(unmatched.size(), is(1));
    assertThat(unmatched.get(0).getRequest().getUrl(), is("/missed"));
  }

  @Test
  void jsonSchemaVersionEnumMappingsAreCorrect() {
    assertThat(WireMock.JsonSchemaVersion.DEFAULT, is(WireMock.JsonSchemaVersion.V202012));
    assertThat(WireMock.JsonSchemaVersion.V4.toVersionFlag(), is(SpecVersion.VersionFlag.V4));
    assertThat(WireMock.JsonSchemaVersion.V6.toVersionFlag(), is(SpecVersion.VersionFlag.V6));
    assertThat(WireMock.JsonSchemaVersion.V7.toVersionFlag(), is(SpecVersion.VersionFlag.V7));
    assertThat(
        WireMock.JsonSchemaVersion.V201909.toVersionFlag(), is(SpecVersion.VersionFlag.V201909));
    assertThat(
        WireMock.JsonSchemaVersion.V202012.toVersionFlag(), is(SpecVersion.VersionFlag.V202012));
  }
}
