/*
 * Copyright (C) 2016-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoggedRequestsSummaryTest extends AcceptanceTestBase {
  private WireMockTestClient client;

  @BeforeEach
  public void init() {
    WireMock.reset();
    stubFor(get(WireMock.anyUrl()).willReturn(WireMock.ok()));
    client = new WireMockTestClient(wireMockServer.port());
  }

  @Test
  public void noRequests() {
    assertThat(findAll(anyRequestedFor(anyUrl())).requestSummary()).isEqualTo("");
  }

  @Test
  public void oneRequests() {
    client.get("/aaaaaa");

    assertThat(findAll(anyRequestedFor(anyUrl())).requestSummary()).isEqualTo("1	| GET /aaaaaa");
  }

  @Test
  public void mixedRequests() {
    client.get("/aaaaaa");
    client.delete("/zzzzzz");
    client.get("/oooooo");
    client.get("/oooooo");
    client.delete("/oooooo");
    client.putJson("/aaaaaa", "{}");

    assertThat(findAll(anyRequestedFor(anyUrl())).requestSummary())
        .isEqualTo(
            "1	| GET /aaaaaa\n"
                + "1	| PUT /aaaaaa\n"
                + "1	| DELETE /oooooo\n"
                + "2	| GET /oooooo\n"
                + "1	| DELETE /zzzzzz");
  }
}
