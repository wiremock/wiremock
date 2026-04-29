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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.common.Metadata.metadata;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.FilteringRecorderServeEventTransformer;
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

  private static final String EXPECTED_STUB =
      // language=json
      """
      {
          "mappings": [
              {
                  "request" : {
                      "url" : "/foo/bar",
                      "method" : "POST",
                      "bodyPatterns" : [
                          { "equalTo" : "transformed request body" }
                      ]
                  },
                  "response" : {
                      "status" : 200,
                      "body" : "transformed response body",
                      "headers" : {
                          "Content-Type" : "text/plain"
                      }
                  }
              }
          ]
      }""";

  @SuppressWarnings("unchecked")
  @Test
  public void appliesGlobalRecorderServeEventTransformerToRecordedStubs() {
    wireMockServer.stubFor(any(anyUrl()).willReturn(okJson("{\"original\": true}")));

    proxyServerStart(
        wireMockConfig()
            .withRootDirectory("src/test/resources/empty")
            .extensions(HeaderModifyingRecorderServeEventTransformer.class));

    proxyingTestClient.postJson("/foo/bar", "{\"key\": \"value\"}");

    SnapshotRecordResult recordResult =
        proxyingService.snapshotRecord(
            recordSpec()
                    .makeStubsPersistent(false)
                    .extractBinaryBodiesOver(Long.MAX_VALUE)
                    .extractTextBodiesOver(Long.MAX_VALUE)
                    .captureHeader("Content-Type")
        );
    assertThat(recordResult.getErrors(), empty());

    StubMapping recordedStub =
        proxyingService.listAllStubMappings().getMappings().stream()
            .filter(stub -> "/foo/bar".equals(stub.getRequest().getUrl()))
            .findFirst()
            .orElseThrow(
                () ->
                    new AssertionError("Expected a recorded stub to be present with URL /foo/bar"));

    assertThat(
        recordedStub.getRequest().getHeaders().get("Content-Type").getExpected(), is("text/plain"));
    assertThat(
        recordedStub.getRequest().getBodyPatterns().get(0).getExpected(),
        is("transformed request body"));
    assertThat(
        recordedStub.getResponse().getHeaders().getHeader("Content-Type").firstValue(),
        is("text/plain"));
    assertThat(recordedStub.getResponse().getBody(), is("transformed response body"));
  }

  @Test
  public void filtersOutServeEventsWhenTransformerReturnsEmpty() {
    wireMockServer.stubFor(any(anyUrl()).willReturn(ok("response")));

    proxyServerStart(
        wireMockConfig()
            .withRootDirectory("src/test/resources/empty")
            .extensions(FilteringRecorderServeEventTransformer.class));

    proxyingTestClient.get("/include/this");
    proxyingTestClient.get("/exclude/this");
    proxyingTestClient.get("/include/also");

    SnapshotRecordResult recordResult =
        proxyingService.snapshotRecord(recordSpec()
                .makeStubsPersistent(false)
                .extractBinaryBodiesOver(Long.MAX_VALUE)
                .extractTextBodiesOver(Long.MAX_VALUE)
        );

    List<StubMapping> recordedStubs = recordResult.getStubMappings();
    assertThat(recordedStubs, hasSize(2));

    List<StubMapping> allStubs = proxyingService.listAllStubMappings().getMappings();
    boolean hasExcludedStub =
        allStubs.stream()
            .anyMatch(
                stub -> {
                  String url = stub.getRequest().getUrl();
                  return url != null && url.startsWith("/exclude");
                });
    assertThat(hasExcludedStub, is(false));
  }
}
