/*
 * Copyright (C) 2012-2024 Thomas Akehurst
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
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.admin.model.GetScenariosResult;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.io.IOException;
import java.util.List;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;

public class HttpAdminClientTest {
  private static final String ADMIN_TEST_PREFIX = "/admin-test";

  @Test
  public void returnsOptionsWhenCallingGetOptions() {
    var client = new HttpAdminClient("localhost", 8080);
    assertThat(client.getOptions().portNumber()).isEqualTo(8080);
    assertThat(client.getOptions().bindAddress()).isEqualTo("localhost");
  }

  @Test
  public void shouldSendEmptyRequestForResetToDefaultMappings() {
    var server = new WireMockServer(options().dynamicPort());
    server.start();
    server.addStubMapping(
        server.stubFor(
            post(urlPathEqualTo(ADMIN_TEST_PREFIX + "/__admin/mappings/reset"))
                .withHeader(HttpHeaders.CONTENT_LENGTH, equalTo("0"))
                .willReturn(ok())));
    var client = new HttpAdminClient("localhost", server.port(), ADMIN_TEST_PREFIX);

    client.resetToDefaultMappings();
  }

  @Test
  public void shouldSendEmptyRequestForResetAll() {
    var server = new WireMockServer(options().dynamicPort());
    server.start();
    server.addStubMapping(
        server.stubFor(
            post(urlPathEqualTo(ADMIN_TEST_PREFIX + "/__admin/reset"))
                .withHeader(HttpHeaders.CONTENT_LENGTH, equalTo("0"))
                .willReturn(ok())));
    var client = new HttpAdminClient("localhost", server.port(), ADMIN_TEST_PREFIX);

    client.resetAll();
  }

  @Test
  public void shouldNotSendEntityForGetAllScenarios() {
    var server = new WireMockServer(options().dynamicPort());
    server.start();
    var expectedResponse = new GetScenariosResult(List.of(Scenario.inStartedState("scn1")));
    server.addStubMapping(
        server.stubFor(
            get(urlPathEqualTo(ADMIN_TEST_PREFIX + "/__admin/scenarios"))
                .withHeader(HttpHeaders.CONTENT_LENGTH, absent())
                .willReturn(jsonResponse(expectedResponse, HttpStatus.SC_OK))));
    var client = new HttpAdminClient("localhost", server.port(), ADMIN_TEST_PREFIX);

    assertThat(client.getAllScenarios()).usingRecursiveComparison().isEqualTo(expectedResponse);
  }

  @Test
  public void reuseConnections() throws InterruptedException, IOException {
    var server = new SingleConnectionServer();
    server.start();
    var client = new HttpAdminClient("localhost", server.getPort(), ADMIN_TEST_PREFIX);

    client.resetAll();
    client.resetAll();
    server.stop();
  }
}
