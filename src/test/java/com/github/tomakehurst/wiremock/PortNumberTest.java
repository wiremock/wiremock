/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples;
import com.github.tomakehurst.wiremock.testsupport.Network;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PortNumberTest {

  private List<WireMockServer> createdServers;

  @BeforeEach
  public void setup() {
    createdServers = new ArrayList<WireMockServer>();
  }

  @AfterEach
  public void stopServers() {
    for (WireMockServer wireMockServer : createdServers) {
      if (wireMockServer.isRunning()) {
        wireMockServer.stop();
      }
    }
  }

  @Test
  public void canRunOnAnotherPortThan8080() {
    int port = Network.findFreePort();
    WireMockServer wireMockServer = createServer(wireMockConfig().port(port));
    wireMockServer.start();
    WireMockTestClient wireMockClient = new WireMockTestClient(port);

    wireMockClient.addResponse(MappingJsonSamples.BASIC_MAPPING_REQUEST_WITH_RESPONSE_HEADER);
    WireMockResponse response = wireMockClient.get("/a/registered/resource");
    assertThat(response.statusCode(), is(401));
  }

  @Test
  public void configuredPortIsReportedListeningPort() {
    int port = Network.findFreePort();
    int httpsPort = Network.findFreePort();
    WireMockServer wireMockServer = createServer(wireMockConfig().port(port).httpsPort(httpsPort));
    wireMockServer.start();

    assertThat(wireMockServer.port(), is(port));
    assertThat(wireMockServer.httpsPort(), is(httpsPort));
  }

  @Test
  public void unstartedServerThrowsExceptionWhenAttemptingToRetrievePort() {
    assertThrows(
        IllegalStateException.class,
        () -> createServer(wireMockConfig().port(Network.findFreePort())).port());
  }

  @Test
  public void unstartedServerThrowsExceptionWhenAttemptingToRetrieveHttpsPort() {
    assertThrows(
        IllegalStateException.class,
        () -> createServer(wireMockConfig().httpsPort(Network.findFreePort())).httpsPort());
  }

  @Test
  public void serverWithoutHttpsThrowsExceptionWhenAttemptingToRetrieveHttpsPort() {
    assertThrows(
        IllegalStateException.class,
        () -> {
          WireMockServer wireMockServer =
              createServer(wireMockConfig().port(Network.findFreePort()));
          wireMockServer.start();
          wireMockServer.httpsPort();
        });
  }

  @Test
  public void configuringPortZeroPicksArbitraryPort() {
    WireMockServer wireMockServer = createServer(wireMockConfig().port(0).httpsPort(0));
    wireMockServer.start();
    assertThat(wireMockServer.port(), greaterThan(0));
    assertThat(wireMockServer.httpsPort(), greaterThan(0));
  }

  private WireMockServer createServer(WireMockConfiguration configuration) {
    final WireMockServer wireMockServer = new WireMockServer(configuration);
    createdServers.add(wireMockServer);
    return wireMockServer;
  }
}
