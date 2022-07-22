/*
 * Copyright (C) 2021-2022 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;

public class JUnitJupiterExtensionProgrammaticWithNestedTest {

  @RegisterExtension
  static WireMockExtension wm1 =
      WireMockExtension.newInstance()
          .options(wireMockConfig().port(8765))
          .configureStaticDsl(true)
          .build();

  CloseableHttpClient client;

  @BeforeEach
  void init() {
    client = HttpClientFactory.createClient();
  }

  @Test
  void runs_on_the_supplied_port() throws Exception {
    WireMockRuntimeInfo wm1RuntimeInfo = wm1.getRuntimeInfo();
    assertThat(wm1RuntimeInfo.getHttpPort(), is(8765));

    stubFor(get("/wm1").willReturn(ok()));
    HttpGet request = new HttpGet(wm1RuntimeInfo.getHttpBaseUrl() + "/wm1");
    try (CloseableHttpResponse response = client.execute(request)) {
      assertThat(response.getCode(), is(200));
    }
  }

  @Nested
  class RunsOn8766 {
    @RegisterExtension
    WireMockExtension wm2 =
        WireMockExtension.newInstance().options(wireMockConfig().port(8766)).build();

    @Test
    void runs_on_the_supplied_port() throws Exception {
      WireMockRuntimeInfo wm1RuntimeInfo = wm1.getRuntimeInfo();
      assertThat(wm1RuntimeInfo.getHttpPort(), is(8765));

      stubFor(get("/wm1").willReturn(ok()));
      HttpGet request1 = new HttpGet(wm1RuntimeInfo.getHttpBaseUrl() + "/wm1");
      try (CloseableHttpResponse response = client.execute(request1)) {
        assertThat(response.getCode(), is(200));
      }

      WireMockRuntimeInfo wm2RuntimeInfo = wm2.getRuntimeInfo();
      assertThat(wm2RuntimeInfo.getHttpPort(), is(8766));

      wm2.stubFor(get("/wm2").willReturn(ok()));
      HttpGet request2 = new HttpGet(wm2RuntimeInfo.getHttpBaseUrl() + "/wm2");
      try (CloseableHttpResponse response = client.execute(request2)) {
        assertThat(response.getCode(), is(200));
      }
    }
  }

  private String getContent(String url) throws Exception {
    try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
      return EntityUtils.toString(response.getEntity());
    }
  }
}
