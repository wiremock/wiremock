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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.testsupport.Network;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class JUnitJupiterExtensionJvmProxyStaticProgrammaticTest {

  @RegisterExtension
  static WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(wireMockConfig().port(Network.findFreePort()).dynamicHttpsPort())
          .configureStaticDsl(true)
          .proxyMode(true)
          .build();

  CloseableHttpClient client;

  @BeforeEach
  void init() {
    client = HttpClientFactory.createClient();
  }

  @Test
  void configures_jvm_proxy_and_enables_browser_proxying() throws Exception {
    stubFor(get("/things").withHost(equalTo("one.my.domain")).willReturn(ok("1")));

    stubFor(get("/things").withHost(equalTo("two.my.domain")).willReturn(ok("2")));

    assertThat(getContent("http://one.my.domain/things"), is("1"));
    assertThat(getContent("https://two.my.domain/things"), is("2"));
  }

  private String getContent(String url) throws Exception {
    try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
      return EntityUtils.toString(response.getEntity());
    }
  }
}
