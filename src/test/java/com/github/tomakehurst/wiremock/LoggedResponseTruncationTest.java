/*
 * Copyright (C) 2022-2023 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class LoggedResponseTruncationTest {

  static final int MAX_SIZE = 1400;

  @RegisterExtension
  static WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().maxLoggedResponseSize(MAX_SIZE))
          .build();

  WireMockTestClient client;

  @BeforeEach
  void init() {
    wm.resetAll();
    client = new WireMockTestClient(wm.getPort());
  }

  @Test
  void includesFullResponseBodyWhenBelowConfiguredThreshold() {
    String bigBody = RandomStringUtils.randomAlphabetic(MAX_SIZE - 1);
    wm.stubFor(any(anyUrl()).willReturn(ok(bigBody)));
    client.get("/big");

    assertThat(wm.getAllServeEvents().get(0).getResponse().getBodyAsString(), is(bigBody));
  }

  @Test
  void includesFullResponseBodyWhenAtConfiguredThreshold() {
    String bigBody = RandomStringUtils.randomAlphabetic(MAX_SIZE - 1);
    wm.stubFor(any(anyUrl()).willReturn(ok(bigBody)));
    client.get("/big");

    assertThat(wm.getAllServeEvents().get(0).getResponse().getBodyAsString(), is(bigBody));
  }

  @Test
  void truncatesResponseBodyWhenOverConfiguredThreshold() {
    String bigBody = RandomStringUtils.randomAlphabetic(MAX_SIZE + 1);
    wm.stubFor(any(anyUrl()).willReturn(ok(bigBody)));
    client.get("/big");

    String expectedLoggedBody = bigBody.substring(0, MAX_SIZE);
    String loggedResponseBody = wm.getAllServeEvents().get(0).getResponse().getBodyAsString();
    assertThat(loggedResponseBody, is(expectedLoggedBody));
    assertThat(loggedResponseBody.length(), is(MAX_SIZE));
  }
}
