/*
 * Copyright (C) 2019-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.eclipse.jetty.http.HttpVersion.HTTP_2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Http2AcceptanceTest {

  @RegisterExtension
  public static WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
          .build();

  @Test
  public void supportsHttp2Connections() throws Exception {
    HttpClient client = Http2ClientFactory.create();

    wm.stubFor(get("/thing").willReturn(ok("HTTP/2 response")));

    ContentResponse response = client.GET(wm.getRuntimeInfo().getHttpsBaseUrl() + "/thing");
    assertThat(response.getVersion(), is(HTTP_2));
    assertThat(response.getStatus(), is(200));
  }

  @Test
  public void supportsHttp2PlaintextConnections() throws Exception {
    HttpClient client = Http2ClientFactory.create();

    wm.stubFor(get("/thing").willReturn(ok("HTTP/2 response")));

    ContentResponse response = client.GET(wm.getRuntimeInfo().getHttpBaseUrl() + "/thing");
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

  @Test
  void connectionResetByPeerFault() {
    HttpClient client = Http2ClientFactory.create();

    wm.stubFor(
        get(urlEqualTo("/connection/reset"))
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

    getAndAssertUnderlyingExceptionInstanceClass(
        client,
        wm.getRuntimeInfo().getHttpsBaseUrl() + "/connection/reset",
        ClosedChannelException.class);
  }

  @Test
  void emptyResponseFault() {
    HttpClient client = Http2ClientFactory.create();

    wm.stubFor(
        get(urlEqualTo("/empty/response")).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

    getAndAssertUnderlyingExceptionInstanceClass(
        client,
        wm.getRuntimeInfo().getHttpsBaseUrl() + "/empty/response",
        ClosedChannelException.class);
  }

  @Test
  void malformedResponseChunkFault() {
    HttpClient client = Http2ClientFactory.create();

    wm.stubFor(
        get(urlEqualTo("/malformed/response"))
            .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

    IOException e =
        getAndAssertUnderlyingExceptionInstanceClass(
            client,
            wm.getRuntimeInfo().getHttpsBaseUrl() + "/malformed/response",
            IOException.class);
    assertThat(e.getMessage(), is("frame_size_error/invalid_frame_length"));
  }

  @Test
  void randomDataOnSocketFault() {
    HttpClient client = Http2ClientFactory.create();

    wm.stubFor(
        get(urlEqualTo("/random/data"))
            .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

    IOException e =
        getAndAssertUnderlyingExceptionInstanceClass(
            client, wm.getRuntimeInfo().getHttpsBaseUrl() + "/random/data", IOException.class);
    assertThat(e.getMessage(), is("frame_size_error/invalid_frame_length"));
  }

  private <T> T getAndAssertUnderlyingExceptionInstanceClass(
      HttpClient httpClient, String url, Class<T> expectedClass) {
    try {
      contentFor(httpClient, url);
    } catch (Exception e) {
      Throwable cause = e.getCause();
      if (cause != null) {
        assertThat(e.getCause(), instanceOf(expectedClass));
        //noinspection unchecked
        return (T) e.getCause();
      } else {
        assertThat(e, instanceOf(expectedClass));
        //noinspection unchecked
        return (T) e;
      }
    }

    return fail("No exception was thrown");
  }

  private void contentFor(HttpClient httpClient, String url) throws Exception {
    httpClient.GET(url).getContentAsString();
  }
}
