/*
 * Copyright (C) 2024-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty12;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.net.http.HttpClient.Version.HTTP_1_1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Http2DisabledAcceptanceTest {

  @RegisterExtension
  public WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(
              wireMockConfig()
                  .dynamicPort()
                  .dynamicHttpsPort()
                  .http2PlainDisabled(true)
                  .http2TlsDisabled(true))
          .build();

  HttpClient client;

  @BeforeEach
  void init() throws Exception {
    client = HttpClient.newBuilder().sslContext(trustEverything()).build();
  }

  @Test
  public void usesHttp1_1OverPlainText() throws Exception {
    wm.stubFor(get("/thing").willReturn(ok("HTTP/2 response")));

    URI uri = URI.create(wm.getRuntimeInfo().getHttpBaseUrl() + "/thing");

    HttpResponse<String> response =
        client.send(HttpRequest.newBuilder(uri).build(), HttpResponse.BodyHandlers.ofString());
    assertThat(response.version(), is(HTTP_1_1));
    assertThat(response.statusCode(), is(200));
  }

  @Test
  void usesHttp1_1OverTls() throws Exception {
    wm.stubFor(get("/thing").willReturn(ok("HTTP/2 response")));

    URI uri = URI.create(wm.getRuntimeInfo().getHttpsBaseUrl() + "/thing");

    HttpResponse<String> response =
        client.send(HttpRequest.newBuilder(uri).build(), HttpResponse.BodyHandlers.ofString());
    assertThat(response.version(), is(HTTP_1_1));
    assertThat(response.statusCode(), is(200));
  }

  private SSLContext trustEverything() throws Exception {
    X509ExtendedTrustManager trustManager =
        new X509ExtendedTrustManager() {
          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
          }

          @Override
          public void checkClientTrusted(X509Certificate[] chain, String authType) {}

          @Override
          public void checkServerTrusted(X509Certificate[] chain, String authType) {}

          @Override
          public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {}

          @Override
          public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {}

          @Override
          public void checkClientTrusted(
              X509Certificate[] chain, String authType, SSLEngine engine) {}

          @Override
          public void checkServerTrusted(
              X509Certificate[] chain, String authType, SSLEngine engine) {}
        };
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, new TrustManager[] {trustManager}, new SecureRandom());

    return sslContext;
  }
}
