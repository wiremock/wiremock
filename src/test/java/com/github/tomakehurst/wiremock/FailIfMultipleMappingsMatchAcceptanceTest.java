/*
 * Copyright (C) 2015-2023 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class FailIfMultipleMappingsMatchAcceptanceTest {

  @RegisterExtension
  public WireMockExtension wireMockRule =
      WireMockExtension.newInstance()
          .configureStaticDsl(true)
          .options(WireMockConfiguration.options().failIfMultipleMappingsMatch(true))
          .build();

  private WireMockTestClient testClient;

  @BeforeEach
  public void init() {
    this.testClient = new WireMockTestClient(this.wireMockRule.getPort());
  }

  @Test
  public void zeroMappingShouldNotFail() {
    final WireMockResponse actual = this.testClient.get("/some/resource");

    assertThat(actual.content())
        .isEqualTo(
            "No response could be served as there are no stub mappings in this WireMock instance.");
  }

  @Test
  public void oneMappingShouldNotFail() {
    stubFor(
        get(urlEqualTo("/some/resource"))
            .willReturn(aResponse().withStatus(201).withBody("Content")));

    final WireMockResponse actual = this.testClient.get("/some/resource");

    assertThat(actual.content()).isEqualTo("Content");
  }

  @Test
  public void twoMappingShouldFail() {
    stubFor(
        get(urlEqualTo("/some/resource"))
            .withId(UUID.fromString("9e15e543-8de1-4bbc-afb8-ab75e51b0b5a"))
            .willReturn(aResponse().withStatus(201).withBody("Content")));
    stubFor(
        get(urlEqualTo("/some/resource"))
            .withId(UUID.fromString("82d63dd0-8df8-4818-b23a-05aa0bf2f182"))
            .willReturn(aResponse().withStatus(202).withBody("Content")));

    final WireMockResponse actual = this.testClient.get("/some/resource");

    assertThat(actual.content())
        .contains(
            "Several mappings matched the request: 82d63dd0-8df8-4818-b23a-05aa0bf2f182, 9e15e543-8de1-4bbc-afb8-ab75e51b0b5a");
  }
}
