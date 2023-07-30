/*
 * Copyright (C) 2013-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.Network;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class WireMockServerTests {

  @TempDir public File tempDir;

  @Test
  public void instantiationWithEmptyFileSource() throws IOException {
    Options options =
        new WireMockConfiguration()
            .port(Network.findFreePort())
            .fileSource(new SingleRootFileSource(tempDir));

    WireMockServer wireMockServer = null;
    try {
      wireMockServer = new WireMockServer(options);
      wireMockServer.start();
    } finally {
      if (wireMockServer != null) {
        wireMockServer.stop();
      }
    }
  }

  @Test
  public void returnsOptionsWhenCallingGetOptions() {
    Options options = new WireMockConfiguration();
    WireMockServer wireMockServer = new WireMockServer(options);
    assertThat(wireMockServer.getOptions(), is(options));
  }

  @Test
  public void addFilenameTemplateAsOptionAndValidFormat() {
    Options options = options().filenameTemplate("{{{request.url}}}-{{{request.url}}}.json");
    WireMockServer wireMockServer = new WireMockServer(options);
    wireMockServer.start();
    assertThat(wireMockServer.getOptions(), is(options));
  }

  @Test
  public void buildsQualifiedHttpUrlFromPath() {
    WireMockServer wireMockServer = new WireMockServer(options().port(Network.findFreePort()));
    wireMockServer.start();
    int port = wireMockServer.port();

    assertThat(
        wireMockServer.url("/something"), is(String.format("http://localhost:%d/something", port)));
    assertThat(
        wireMockServer.url("something"), is(String.format("http://localhost:%d/something", port)));
  }

  @Test
  public void buildsQualifiedHttpsUrlFromPath() {
    WireMockServer wireMockServer =
        new WireMockServer(
            options().port(Network.findFreePort()).httpsPort(Network.findFreePort()));
    wireMockServer.start();
    int port = wireMockServer.httpsPort();

    assertThat(
        wireMockServer.url("/something"),
        is(String.format("https://localhost:%d/something", port)));
    assertThat(
        wireMockServer.url("something"), is(String.format("https://localhost:%d/something", port)));
  }

  @Test
  public void buildsBaseHttpUrl() {
    WireMockServer wireMockServer = new WireMockServer(options().port(Network.findFreePort()));
    wireMockServer.start();
    int port = wireMockServer.port();

    assertThat(wireMockServer.baseUrl(), is(String.format("http://localhost:%d", port)));
  }

  @Test
  public void buildsBaseHttpsUrl() {
    WireMockServer wireMockServer =
        new WireMockServer(
            options().port(Network.findFreePort()).httpsPort(Network.findFreePort()));
    wireMockServer.start();
    int port = wireMockServer.httpsPort();

    assertThat(wireMockServer.baseUrl(), is(String.format("https://localhost:%d", port)));
  }

  // https://github.com/tomakehurst/wiremock/issues/193
  @Test
  public void supportsRecordingProgrammaticallyWithoutHeaderMatching() {
    WireMockServer wireMockServer =
        new WireMockServer(
            Network.findFreePort(),
            new SingleRootFileSource(tempDir),
            false,
            new ProxySettings("proxy.company.com", Network.findFreePort()));
    wireMockServer.start();
    wireMockServer.enableRecordMappings(
        new SingleRootFileSource(tempDir + "/mappings"),
        new SingleRootFileSource(tempDir + "/__files"));
    wireMockServer.stubFor(get(urlEqualTo("/something")).willReturn(aResponse().withStatus(200)));

    WireMockTestClient client = new WireMockTestClient(wireMockServer.port());
    assertThat(
        client.get("http://localhost:" + wireMockServer.port() + "/something").statusCode(),
        is(200));
  }
}
