/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ContentTypes.AUTHORIZATION;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.security.*;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ClientAuthenticationAcceptanceTest {

  private WireMockServer server;
  private WireMock goodClient;
  private WireMock badClient;

  @AfterEach
  public void stopServer() {
    server.stop();
  }

  @Test
  void supportsCustomAuthenticator() {
    initialise(
        request -> request.containsHeader("X-Magic-Header"),
        () -> singletonList(httpHeader("X-Magic-Header", "blah")));

    WireMockTestClient noAuthClient = new WireMockTestClient(server.port());

    assertThat(noAuthClient.get("/__admin/mappings").statusCode(), is(401));
    assertThat(
        noAuthClient
            .get("/__admin/mappings", withHeader("X-Magic-Header", "anything"))
            .statusCode(),
        is(200));

    goodClient.getServeEvents(); // Throws an exception on a non 2xx response
  }

  @Test
  void supportsBasicAuthenticator() {
    initialise(
        new BasicAuthenticator(
            new BasicCredentials("user1", "password1"), new BasicCredentials("user2", "password2")),
        new ClientBasicAuthenticator("user1", "password1"));

    assertDoesNotThrow(() -> goodClient.getServeEvents());
  }

  @Test
  void throwsNotAuthorisedExceptionWhenWrongBasicCredentialsProvided() {
    initialise(
        new BasicAuthenticator(
            new BasicCredentials("user1", "password1"), new BasicCredentials("user2", "password2")),
        new ClientBasicAuthenticator("user1", "password1"));

    badClient =
        WireMock.create()
            .port(server.port())
            .authenticator(new ClientBasicAuthenticator("user1", "wrong_password"))
            .build();

    assertThrows(NotAuthorisedException.class, () -> badClient.getServeEvents());
  }

  @Test
  void supportsBasicAuthenticatorViaStaticDsl() {
    initialise(
        new BasicAuthenticator(
            new BasicCredentials("user1", "password1"), new BasicCredentials("user2", "password2")),
        new ClientBasicAuthenticator("user2", "password2"));
    WireMockTestClient client = new WireMockTestClient(server.port());

    WireMock.configureFor(goodClient);

    WireMock.getAllServeEvents(); // Expect no exception thrown
    assertThat(client.get("/__admin/requests").statusCode(), is(401));
  }

  @Test
  void supportsShorthandBasicAuthWithHttps() {
    server =
        new WireMockServer(
            wireMockConfig()
                .dynamicPort()
                .dynamicHttpsPort()
                .basicAdminAuthenticator("user", "password"));
    server.start();

    goodClient =
        WireMock.create()
            .port(server.httpsPort())
            .https()
            .basicAuthenticator("user", "password")
            .build();

    assertDoesNotThrow(() -> goodClient.getServeEvents());
  }

  @Test
  void canRequireHttpsOnAdminApi() {
    server =
        new WireMockServer(
            wireMockConfig()
                .dynamicPort()
                .dynamicHttpsPort()
                .basicAdminAuthenticator("user", "password")
                .requireHttpsForAdminApi());
    server.start();
    WireMockTestClient client = new WireMockTestClient(server.port());

    String authHeader = new BasicCredentials("user", "password").asAuthorizationHeaderValue();
    WireMockResponse response =
        client.get("/__admin/requests", withHeader(AUTHORIZATION, authHeader));

    assertThat(response.statusCode(), is(403));
    assertThat(response.content(), containsString("HTTPS is required for accessing the admin API"));
  }

  @Test
  void supportsTokenAuthenticatorViaStaticDsl() {
    final String TOKEN = "my_token_123";

    initialise(new TokenAuthenticator(TOKEN), new ClientTokenAuthenticator(TOKEN));
    WireMockTestClient client = new WireMockTestClient(server.port());

    WireMock.configureFor(goodClient);

    WireMock.getAllServeEvents(); // Expect no exception thrown
    assertThat(client.get("/__admin/requests").statusCode(), is(401));
  }

  private void initialise(
      Authenticator adminAuthenticator, ClientAuthenticator clientAuthenticator) {
    server =
        new WireMockServer(wireMockConfig().dynamicPort().adminAuthenticator(adminAuthenticator));
    server.start();

    goodClient = WireMock.create().port(server.port()).authenticator(clientAuthenticator).build();
  }
}
