/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.proxyAllTo;
import static com.github.tomakehurst.wiremock.common.Metadata.metadata;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.client.netty.NettyClientFactory;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LooseUrlAcceptanceTest extends AcceptanceTestBase {

  private WireMockServer proxyingService;
  private OkHttpClient client;
  private String proxyTargetUrl;

  private void proxyServerStart(WireMockConfiguration config) {
    proxyingService =
        new WireMockServer(config.dynamicPort().httpClientFactory(new NettyClientFactory()));
    proxyingService.start();
    proxyTargetUrl = "http://localhost:" + wireMockServer.port();
    proxyingService.stubFor(
        proxyAllTo(proxyTargetUrl).withMetadata(metadata().attr("proxy", true)));

    client = new OkHttpClient();
    wireMockServer.stubFor(any(anyUrl()).willReturn(ok()));
  }

  @BeforeEach
  public void clearTargetServerMappings() {
    wireMockServer.resetMappings();
  }

  @AfterEach
  public void proxyServerShutdown() {
    // delete any persisted stub mappings to ensure test isolation
    proxyingService.resetMappings();
    proxyingService.stop();
  }

  @Test
  void canRecordAUrlThatDoesNotConformToRfc3986() throws Exception {
    proxyServerStart(wireMockConfig().withRootDirectory(setupTempFileRoot().getAbsolutePath()));
    Response response =
        client
            .newCall(new Builder().url(proxyingService.baseUrl() + "/foo/bar?q={}").build())
            .execute();
    assertThat(response.code()).isEqualTo(200);
    List<ServeEvent> proxyTargetServeEvents = wireMockServer.getAllServeEvents();
    assertThat(proxyTargetServeEvents).hasSize(1);
    LoggedRequest targetRequest = proxyTargetServeEvents.get(0).getRequest();
    assertThat(targetRequest.getUrl()).isEqualTo("/foo/bar?q={}");
    targetRequest.getQueryParams().get("q").hasValueMatching(equalTo("{}"));
  }
}
