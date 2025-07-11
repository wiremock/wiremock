/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.mappingssource;

import static com.github.tomakehurst.wiremock.client.WireMock.startRecording;
import static com.github.tomakehurst.wiremock.client.WireMock.stopRecording;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.AcceptanceTestBase;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import com.github.tomakehurst.wiremock.standalone.MappingsSource;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomMappingsSourceTest extends AcceptanceTestBase {

  private WireMockServer proxyingService;
  private String targetBaseUrl;
  private WireMockTestClient testClient;

  private CustomMappingsSource customMappingsSource;

  @Override
  @BeforeEach
  public void init() {
    File fileRoot = setupTempFileRoot();
    customMappingsSource = new CustomMappingsSource(fileRoot);
    proxyingService =
        new WireMockServer(
            wireMockConfig()
                .dynamicPort()
                .mappingSource(customMappingsSource)
                .withRootDirectory(fileRoot.getAbsolutePath()));
    proxyingService.start();

    WireMockServer targetService = wireMockServer;
    targetBaseUrl = "http://localhost:" + targetService.port();
    testClient = new WireMockTestClient(proxyingService.port());
    WireMock.configureFor(proxyingService.port());
  }

  @AfterEach
  void proxyServerShutdown() {
    proxyingService.resetMappings();
    proxyingService.stop();
  }

  /**
   * Verifies that custom MappingsSource implementations receive the original raw URL (before URL
   * decoding) when stub mappings are saved during recording. This is important for custom mappings
   * sources that need to preserve the exact format of URLs as they were originally requested.
   */
  @Test
  void customMappingsSourceReceivesRawUrlsWhenRecording() {
    startRecording(targetBaseUrl);

    // Test URLs with and without query parameters, including encoded characters
    String urlWithEncodedParams = "/resources?%24page=1&%24size=10"; // %24 is URL-encoded $
    String simpleUrl = "/resource/1";
    String urlWithQueryParams = "/inventory?query=blue";

    testClient.get(urlWithEncodedParams);
    testClient.get(simpleUrl);
    testClient.get(urlWithQueryParams);

    stopRecording();

    // Verify that the custom mappings source received the original raw URLs
    assertThat(
        customMappingsSource.getRawUrls(),
        is(List.of(urlWithEncodedParams, simpleUrl, urlWithQueryParams)));
  }

  /**
   * Test implementation of MappingsSource that delegates to JsonFileMappingsSource while capturing
   * the original raw URLs for verification purposes.
   */
  private static class CustomMappingsSource implements MappingsSource {

    private final MappingsSource underlyingSource;
    private final List<String> rawUrls = new ArrayList<>();

    private CustomMappingsSource(File fileRoot) {
      this.underlyingSource =
          new JsonFileMappingsSource(new SingleRootFileSource(fileRoot), new FilenameMaker());
    }

    @Override
    public void save(List<StubMapping> stubMappings) {
      // Capture original raw URLs for test verification
      stubMappings.forEach(
          stubMapping ->
              // Before WireMock 3.13.0 and #3008, this test would have passed with
              // stubMapping.getRequest().getUrl()
              rawUrls.add(stubMapping.getRequest().getRawUrl()));
      underlyingSource.save(stubMappings);
    }

    @Override
    public void save(StubMapping stubMapping) {
      underlyingSource.save(stubMapping);
    }

    @Override
    public void remove(UUID stubMappingId) {
      underlyingSource.remove(stubMappingId);
    }

    @Override
    public void removeAll() {
      underlyingSource.removeAll();
    }

    @Override
    public void setAll(List<StubMapping> stubMappings) {
      underlyingSource.setAll(stubMappings);
    }

    @Override
    public void loadMappingsInto(StubMappings stubMappings) {
      underlyingSource.loadMappingsInto(stubMappings);
    }

    List<String> getRawUrls() {
      return new ArrayList<>(rawUrls);
    }
  }
}
