/*
 * Copyright (C) 2012-2025 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.security.NoClientAuthenticator.noClientAuthenticator;
import static org.apache.hc.core5.http.HttpHeaders.HOST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.admin.model.GetScenariosResult;
import com.github.tomakehurst.wiremock.common.ClientError;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.InvalidInputException;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.security.ClientAuthenticator;
import com.github.tomakehurst.wiremock.security.ClientTokenAuthenticator;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class HttpAdminClientTest {
  private static final String ADMIN_TEST_PREFIX = "/admin-test";

  @Test
  void returnsOptionsWhenCallingGetOptions() {
    var client = buildHttpAdminClient(8080, "");
    assertThat(client.getOptions().portNumber()).isEqualTo(8080);
    assertThat(client.getOptions().bindAddress()).isEqualTo("localhost");
  }

  @Test
  void shouldSendEmptyRequestForResetToDefaultMappings() {
    var server = new WireMockServer(options().dynamicPort());
    server.start();
    server.stubFor(
        post(urlPathEqualTo(ADMIN_TEST_PREFIX + "/__admin/mappings/reset"))
            .withHeader(HttpHeaders.CONTENT_LENGTH, equalTo("0"))
            .willReturn(ok()));
    var client = buildHttpAdminClient(server);

    client.resetToDefaultMappings();
  }

  @Test
  void shouldSendEmptyRequestForResetAll() {
    var server = new WireMockServer(options().dynamicPort());
    server.start();
    server.stubFor(
        post(urlPathEqualTo(ADMIN_TEST_PREFIX + "/__admin/reset"))
            .withHeader(HttpHeaders.CONTENT_LENGTH, equalTo("0"))
            .willReturn(ok()));
    var client = buildHttpAdminClient(server);

    client.resetAll();
  }

  @Test
  void shouldBeAbleToContactWiremockIfPortIsNotSpecified() throws IOException {

    HttpClient httpClient = mock(HttpClient.class);
    ClientAuthenticator authenticator = mock(ClientAuthenticator.class);
    when(authenticator.generateAuthHeaders()).thenReturn(Collections.emptyList());
    ArgumentCaptor<Request> httpRequestSentCaptor = ArgumentCaptor.forClass(Request.class);
    var scheme = "https";
    var domain = "my.domain.name";
    var client = new HttpAdminClient(scheme, domain, -1, "", "", authenticator, httpClient);

    try {
      client.getAllScenarios();
    } catch (Exception e) {
      // ignore
    }
    Mockito.verify(httpClient).execute(httpRequestSentCaptor.capture());
    Request value = httpRequestSentCaptor.getValue();
    assertThat(value.getAbsoluteUrl()).isEqualTo(scheme + "://" + domain + "/__admin/scenarios");
  }

  @Test
  void shouldInjectCorrectHeaders() throws IOException {

    HttpClient httpClient = mock(HttpClient.class);
    ClientAuthenticator authenticator = new ClientTokenAuthenticator("my_token");
    ArgumentCaptor<Request> httpRequestSentCaptor = ArgumentCaptor.forClass(Request.class);
    var scheme = "https";
    var domain = "my.domain.name";
    var client =
        new HttpAdminClient(scheme, domain, -1, "", "other.example.com", authenticator, httpClient);

    try {
      client.getAllScenarios();
    } catch (Exception e) {
      // ignore
    }
    Mockito.verify(httpClient).execute(httpRequestSentCaptor.capture());
    Request value = httpRequestSentCaptor.getValue();
    assertThat(value.getHeader(HOST)).isEqualTo("other.example.com");
    assertThat(value.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Token my_token");
  }

  @Test
  void shouldNotSendEntityForGetAllScenarios() {
    var server = new WireMockServer(options().dynamicPort());
    server.start();
    var expectedResponse = new GetScenariosResult(List.of(Scenario.inStartedState("scn1")));
    server.stubFor(
        get(urlPathEqualTo(ADMIN_TEST_PREFIX + "/__admin/scenarios"))
            .withHeader(HttpHeaders.CONTENT_LENGTH, absent())
            .willReturn(jsonResponse(expectedResponse, HttpStatus.SC_OK)));
    var client = buildHttpAdminClient(server);

    assertThat(client.getAllScenarios()).usingRecursiveComparison().isEqualTo(expectedResponse);
  }

  @Test
  void reuseConnections() throws InterruptedException, IOException {
    var server = new SingleConnectionServer();
    server.start();
    var client = buildHttpAdminClient(server.getPort(), ADMIN_TEST_PREFIX);

    client.resetAll();
    client.resetAll();
    server.stop();
  }

  @Test
  void shouldThrowExceptionWithUrlForStubMappingFromNonWireMockServerPort() throws IOException {
    var nonWireMockServer = HttpServer.create(new InetSocketAddress(0), 0);
    nonWireMockServer.start();
    var serverPort = nonWireMockServer.getAddress().getPort();
    var client = buildHttpAdminClient(serverPort, ADMIN_TEST_PREFIX);
    var mapping = post(urlPathMatching("/test")).willReturn(ok()).build();
    var thrown = assertThrows(InvalidInputException.class, () -> client.addStubMapping(mapping));
    assertThat(thrown.getErrors().getErrors()).hasSize(1);
    var thrownError = thrown.getErrors().first();
    assertThat(thrownError.getCode()).isEqualTo(10);
    assertThat(thrownError.getTitle()).isEqualTo("Error parsing JSON");
    assertThat(thrownError.getDetail())
        .matches(
            "Error parsing response body '(.|\n)*' with status code 404 for http://localhost:"
                + serverPort
                + "/admin-test/__admin/mappings. Error: (.|\n)*");

    nonWireMockServer.stop(0);
  }

  @Test
  void shouldParseErrorsLeniently() {
    var clientError =
        HttpAdminClient.parseClientError(
            "https://example.com",
            """
            {
              "errors": [
                {
                  "title": "Conflict",
                  "source": {}
                }
              ]
            }
            """,
            409);

    assertThat(clientError)
        .isEqualTo(
            ClientError.fromErrors(
                new Errors(
                    List.of(
                        new Errors.Error(null, new Errors.Error.Source(null), "Conflict", null)))));
  }

  private static HttpAdminClient buildHttpAdminClient(WireMockServer server) {
    return buildHttpAdminClient(server.port(), ADMIN_TEST_PREFIX);
  }

  private static HttpAdminClient buildHttpAdminClient(int port, String urlPathPrefix) {
    return new HttpAdminClient(
        "http",
        "localhost",
        port,
        urlPathPrefix,
        null,
        noClientAuthenticator(),
        WireMockBuilder.createClient());
  }
}
