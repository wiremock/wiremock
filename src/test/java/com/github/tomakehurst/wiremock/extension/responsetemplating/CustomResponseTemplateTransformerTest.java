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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CustomResponseTemplateTransformerTest {

  private WireMockServer wireMockServer;
  private WireMockTestClient testClient;

  @BeforeEach
  public void init() {
    WireMockConfiguration config = new WireMockConfiguration();
    config =
        config
            .dynamicPort()
            .extensions(
                new ResponseTemplateTransformer(
                    TemplateEngine.defaultTemplateEngine(), true, null, Collections.emptyList()));
    wireMockServer = new WireMockServer(config);
    wireMockServer.start();
    WireMock.configureFor(wireMockServer.port());
    testClient = new WireMockTestClient(wireMockServer.port());
  }

  @AfterEach
  public void stopServer() {
    wireMockServer.stop();
  }

  @Test
  public void makeSureCustomResponseTemplateTransformerTakenIntoAccount() {
    WireMock wireMock = WireMock.create().port(wireMockServer.port()).build();

    wireMock.register(
        get(urlEqualTo("/my/new/resource"))
            .willReturn(aResponse().withBody("Path: {{request.path}}")));

    assertThat(testClient.get("/my/new/resource").content(), is("Path: /my/new/resource"));
  }
}
