/*
 * Copyright (C) 2026 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.proxyAllTo;
import static com.github.tomakehurst.wiremock.common.Metadata.metadata;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.HeaderModifyingRecorderServeEventTransformer;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecorderServeEventTransformerAcceptanceTest extends AcceptanceTestBase {

  private WireMockServer proxyingService;
  private WireMockTestClient proxyingTestClient;

  private void proxyServerStart(WireMockConfiguration config) {
    proxyingService = new WireMockServer(config.dynamicPort());
    proxyingService.start();
    String proxyTargetUrl = wireMockServer.getBaseUrl().toString();
    proxyingService.stubFor(
        proxyAllTo(proxyTargetUrl).withMetadata(metadata().attr("proxy", true)));

    proxyingTestClient = new WireMockTestClient(proxyingService.port());
    wireMockServer.stubFor(any(anyUrl()).willReturn(ok()));
  }

  @BeforeEach
  public void clearTargetServerMappings() {
    wireMockServer.resetMappings();
  }

  @AfterEach
  public void proxyServerShutdown() {
    proxyingService.resetMappings();
    proxyingService.stop();
  }

  private static final String STUB_WITH_ADDED_HEADER =
      // language=json
      """
      {
          "mappings": [
              {
                  "request" : {
                      "url" : "/foo/bar",
                      "method" : "GET"
                  },
                  "response" : {
                      "status" : 200,
                      "headers" : {
                          "X-Custom-Header" : "transformed"
                      }
                  }
              }
          ]
      }""";

  @SuppressWarnings("unchecked")
  @Test
  public void appliesGlobalRecorderServeEventTransformerToRecordedStubs() {
    proxyServerStart(
        wireMockConfig()
            .withRootDirectory("src/test/resources/empty")
            .extensions(HeaderModifyingRecorderServeEventTransformer.class));

    proxyingTestClient.get("/foo/bar");

    String recordedStubJson =
        proxyingTestClient.snapshot(
            // language=json
            """
            {
                "outputFormat": "full",
                "persist": "false"
            }""");

    assertThat(
        recordedStubJson,
        jsonEquals(STUB_WITH_ADDED_HEADER).withOptions(List.of(IGNORING_EXTRA_FIELDS)));
  }
}
