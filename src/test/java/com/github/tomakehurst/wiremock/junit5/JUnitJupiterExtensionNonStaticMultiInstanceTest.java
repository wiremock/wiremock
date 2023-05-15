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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getAllServeEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

@TestMethodOrder(OrderAnnotation.class)
public class JUnitJupiterExtensionNonStaticMultiInstanceTest {

  CloseableHttpClient client;

  @RegisterExtension
  WireMockExtension wm1 =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
          .configureStaticDsl(true)
          .build();

  @RegisterExtension
  WireMockExtension wm2 =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().extensions(new ResponseTemplateTransformer(true)))
          .build();

  @BeforeEach
  void init() {
    client = HttpClientFactory.createClient();
  }

  @Test
  @Order(1)
  void extension_field_provides_wiremock_info() throws Exception {
    WireMockRuntimeInfo wm1RuntimeInfo = wm1.getRuntimeInfo();
    assertDoesNotThrow(wm1RuntimeInfo::getHttpsPort);

    stubFor(get("/wm1").willReturn(ok()));
    HttpGet request = new HttpGet(wm1RuntimeInfo.getHttpsBaseUrl() + "/wm1");
    try (CloseableHttpResponse response = client.execute(request)) {
      assertThat(response.getCode(), is(200));
    }

    WireMockRuntimeInfo wm2RuntimeInfo = wm2.getRuntimeInfo();
    wm2.stubFor(get("/wm2").willReturn(ok("{{request.path.0}}")));
    request = new HttpGet(wm2RuntimeInfo.getHttpBaseUrl() + "/wm2");
    try (CloseableHttpResponse response = client.execute(request)) {
      assertThat(response.getCode(), is(200));
      assertThat(
          EntityUtils.toString(response.getEntity()), is("wm2")); // Ensures templating is enabled
    }
  }

  @Test
  @Order(2)
  void wiremock_is_reset_between_tests() throws Exception {
    WireMockRuntimeInfo wm1RuntimeInfo = wm1.getRuntimeInfo();

    assertTrue(getAllServeEvents().isEmpty(), "The request log should be empty");

    HttpGet request = new HttpGet(wm1RuntimeInfo.getHttpsBaseUrl() + "/wm1");
    try (CloseableHttpResponse response = client.execute(request)) {
      assertThat(response.getCode(), is(404));
    }
  }
}
