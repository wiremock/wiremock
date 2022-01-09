/*
 * Copyright (C) 2021 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class JvmProxyConfigAcceptanceTest {

  WireMockServer wireMockServer;

  @AfterEach
  public void cleanup() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test
  public void configuresHttpProxyingOnlyFromAWireMockServer() throws Exception {
    wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().enableBrowserProxying(true));
    wireMockServer.start();

    JvmProxyConfigurer.configureFor(wireMockServer);

    wireMockServer.stubFor(
        get("/stuff").withHost(equalTo("example.com")).willReturn(ok("Proxied stuff")));

    assertThat(
        getContentUsingDefaultJvmHttpClient("http://example.com/stuff"), is("Proxied stuff"));
  }

  @Test
  public void configuresHttpsProxyingOnlyFromAWireMockServer() throws Exception {
    CloseableHttpClient httpClient = HttpClientFactory.createClient();

    wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().enableBrowserProxying(true));
    wireMockServer.start();

    JvmProxyConfigurer.configureFor(wireMockServer);

    wireMockServer.stubFor(
        get("/stuff").withHost(equalTo("example.com")).willReturn(ok("Proxied stuff")));

    try (CloseableHttpResponse response =
        httpClient.execute(new HttpGet("https://example.com/stuff"))) {
      assertThat(EntityUtils.toString(response.getEntity()), is("Proxied stuff"));
    }
  }

  @Test
  public void restoresPreviousSettings() {
    String previousHttpProxyHost = "prevhttpproxyhost";
    String previousHttpProxyPort = "1234";
    String previousHttpsProxyHost = "prevhttpsproxyhost";
    String previousHttpsProxyPort = "4321";
    String previousNonProxyHosts = "blah.com";
    System.setProperty("http.proxyHost", previousHttpProxyHost);
    System.setProperty("http.proxyPort", previousHttpProxyPort);
    System.setProperty("https.proxyHost", previousHttpsProxyHost);
    System.setProperty("https.proxyPort", previousHttpsProxyPort);
    System.setProperty("http.nonProxyHosts", previousNonProxyHosts);

    wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
    wireMockServer.start();

    JvmProxyConfigurer.configureFor(wireMockServer);

    assertThat(System.getProperty("http.proxyHost"), is("localhost"));
    assertThat(System.getProperty("http.proxyPort"), is(String.valueOf(wireMockServer.port())));
    assertThat(System.getProperty("https.proxyHost"), is("localhost"));
    assertThat(System.getProperty("https.proxyPort"), is(String.valueOf(wireMockServer.port())));
    assertThat(System.getProperty("http.nonProxyHosts"), is("localhost|127.*|[::1]"));

    JvmProxyConfigurer.restorePrevious();

    assertThat(System.getProperty("http.proxyHost"), is(previousHttpProxyHost));
    assertThat(System.getProperty("http.proxyPort"), is(previousHttpProxyPort));
    assertThat(System.getProperty("https.proxyHost"), is(previousHttpsProxyHost));
    assertThat(System.getProperty("https.proxyPort"), is(previousHttpsProxyPort));
    assertThat(System.getProperty("http.nonProxyHosts"), is(previousNonProxyHosts));
  }

  private String getContentUsingDefaultJvmHttpClient(String url) throws Exception {
    final HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
    try (InputStream in = urlConnection.getInputStream()) {
      return new String(ByteStreams.toByteArray(in));
    }
  }
}
