/*
 * Copyright (C) 2019-2024 Thomas Akehurst
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
import static org.eclipse.jetty.http.HttpVersion.HTTP_2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Http2AcceptanceTest {

  @RegisterExtension
  public WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(
              wireMockConfig()
                  .dynamicPort()
                  .dynamicHttpsPort()
                  .httpServerFactory(new Jetty12HttpServerFactory()))
          .build();

  @Test
  public void supportsHttp2Connections() throws Exception {
    HttpClient client = Http2ClientFactory.create();

    wm.stubFor(get("/thing").willReturn(ok("HTTP/2 response")));

    ContentResponse response = client.GET(wm.getRuntimeInfo().getHttpsBaseUrl() + "/thing");
    assertThat(response.getStatus(), is(200));
  }

  @Test
  public void supportsHttp2PlaintextConnections() throws Exception {
    HttpClient client = Http2ClientFactory.create();

    wm.stubFor(get("/thing").willReturn(ok("HTTP/2 response")));

    ContentResponse response = client.GET(wm.url("/thing"));
    assertThat(response.getVersion(), is(HTTP_2));
    assertThat(response.getStatus(), is(200));
  }

  @Test
  public void supportsHttp1_1Connections() throws Exception {
    CloseableHttpClient client = HttpClientFactory.createClient();

    wm.stubFor(get("/thing").willReturn(ok("HTTP/1.1 response")));

    HttpGet get = new HttpGet(wm.getRuntimeInfo().getHttpsBaseUrl() + "/thing");
    try (CloseableHttpResponse response = client.execute(get)) {
      assertThat(response.getCode(), is(200));
    }
  }
}
