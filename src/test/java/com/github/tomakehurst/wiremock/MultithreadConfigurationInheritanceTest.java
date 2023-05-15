/*
 * Copyright (C) 2018-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class MultithreadConfigurationInheritanceTest {

  private static WireMockServer wireMockServer;
  private static WireMockTestClient client;

  @BeforeAll
  public static void setup() {
    wireMockServer = new WireMockServer(0);
    wireMockServer.start();
    WireMock.configureFor(wireMockServer.port());
    client = new WireMockTestClient(wireMockServer.port());
  }

  @AfterAll
  public static void shutdown() {
    wireMockServer.shutdown();
  }

  @Test
  @Timeout(5000) // Add a timeout so the test will execute in a new thread
  public void verifyConfigurationInherited() {
    // Make a call to the wiremock server. If this doesn't call to 8082 this will fail
    // with an exception
    stubFor(any(urlEqualTo("/foo/bar")).willReturn(aResponse().withStatus(200)));

    client.get("/foo/bar");

    verify(getRequestedFor(urlPathEqualTo("/foo/bar")));
  }
}
