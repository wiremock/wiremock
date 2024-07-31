/*
 * Copyright (C) 2023-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class HttpClientSubstitutionTest {

  WireMockServer wm;
  WireMockTestClient client;

  void startWireMockServer(WireMockConfiguration options) {
    wm = new WireMockServer(options);
    wm.start();

    client = new WireMockTestClient(wm.port());

    // Doesn't matter what the proxy URL is - we're faking the client
    wm.stubFor(WireMock.proxyAllTo("http://localhost:1234"));
  }

  @AfterEach
  void cleanup() {
    wm.close();
  }

  @Test
  void viaOptions() {
    startWireMockServer(
        wireMockConfig().dynamicPort().httpClientFactory(new FakeHttpClientFactory()));

    assertThat(client.get("/whatever").statusCode()).isEqualTo(418);
  }

  @Test
  void viaExtension() {
    startWireMockServer(wireMockConfig().dynamicPort().extensions(new FakeHttpClientFactory()));

    assertThat(client.get("/whatever").statusCode()).isEqualTo(418);
  }

  public static class FakeHttpClientFactory implements HttpClientFactory {

    @Override
    public HttpClient buildHttpClient(
        Options options,
        boolean trustAllCertificates,
        List<String> trustedHosts,
        boolean useSystemProperties) {
      return new HttpClient() {
        @Override
        public Response execute(Request request) {
          return Response.response().status(418).body("Teapot").build();
        }
      };
    }
  }
}
