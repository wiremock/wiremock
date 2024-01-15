/*
 * Copyright (C) 2024 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.testsupport.TestHttpHeader;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class ResponseDefinitionBuilderAcceptanceTest {

  private WireMockServer wm;
  private WireMockTestClient client;

  @AfterEach
  public void stopServer() {
    wm.stop();
  }

  private void initialise() {
    wm = new WireMockServer(options().dynamicPort().dynamicHttpsPort());
    wm.start();
    client = new WireMockTestClient(wm.port());
  }

  @Test
  void wireMockServerWithStubForWithGzipDisabledTrue() {
    initialise();

    wm.stubFor(
        get(urlEqualTo("/todo/items"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(
                        "Here is some kind of response body"
                            + "Here is some kind of response body"
                            + "Here is some kind of response body")));

    WireMockResponse compressedResponse =
        client.get("/todo/items", new TestHttpHeader("Accept-Encoding", "gzip"));

    wm.stubFor(
        get(urlEqualTo("/todo/items"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withGzipDisabled(true)
                    .withBody(
                        "Here is some kind of response body"
                            + "Here is some kind of response body"
                            + "Here is some kind of response body")));

    WireMockResponse ordinaryResponse =
        client.get("/todo/items", new TestHttpHeader("Accept-Encoding", "gzip"));

    assertTrue(compressedResponse.content().length() < ordinaryResponse.content().length());
    assertTrue(Gzip.isGzipped(compressedResponse.binaryContent()));
    assertFalse(Gzip.isGzipped(ordinaryResponse.binaryContent()));
  }
}
