/*
 * Copyright (C) 2023 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class UriComplianceTest {

  @RegisterExtension
  public WireMockExtension wireMockServer =
      WireMockExtension.newInstance()
          .options(options().dynamicPort())
          .configureStaticDsl(true)
          .build();

  private WireMockTestClient testClient;

  @BeforeEach
  public void init() {
    testClient = new WireMockTestClient(wireMockServer.getPort());
  }

  @Test
  public void buildsMappingWithEmptySegment() {
    givenThat(get(urlEqualTo("/my//resource")).willReturn(aResponse().withStatus(200)));

    assertThat(testClient.get("/my//resource").statusCode(), is(200));
  }

  @Test
  public void buildsMappingWithAmbiguousSegment() {
    givenThat(get(urlPathEqualTo("/my/%2e/resource")).willReturn(aResponse().withStatus(200)));

    assertThat(testClient.get("/my/%2e/resource").statusCode(), is(200));
  }

  @Test
  public void buildsMappingWithAmbiguousPathSeparator() {
    givenThat(get(urlPathEqualTo("/foo/b%2fr")).willReturn(aResponse().withStatus(200)));

    assertThat(testClient.get("/foo/b%2fr").statusCode(), is(200));
  }

  @Test
  public void buildsMappingWithAmbiguousPathParameter() {
    givenThat(get(urlPathEqualTo("/foo/..;/bar")).willReturn(aResponse().withStatus(200)));

    assertThat(testClient.get("/foo/..;/bar").statusCode(), is(200));
  }

  @Test
  public void buildsMappingWithAmbiguousPathEncoding() {
    givenThat(get(urlPathEqualTo("/%2557EB-INF")).willReturn(aResponse().withStatus(200)));

    assertThat(testClient.get("/%2557EB-INF").statusCode(), is(200));
  }
}
