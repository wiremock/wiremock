/*
 * Copyright (C) 2021-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.junit5;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest(httpsEnabled = true)
class JUnitJupiterExtensionDeclarativeWithRandomHttpsPortParameterTest {

  CloseableHttpClient client;

  @BeforeEach
  void init() {
    client = HttpClientFactory.createClient();
  }

  @Test
  void runs_on_a_random_port_when_enabled(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
    assertTrue(wmRuntimeInfo.isHttpsEnabled(), "Expected HTTPS to be enabled");

    stubFor(get("/thing").willReturn(ok()));

    HttpGet request = new HttpGet(wmRuntimeInfo.getHttpsBaseUrl() + "/thing");
    try (CloseableHttpResponse response = client.execute(request)) {
      assertThat(response.getCode(), is(200));
    }
  }
}
