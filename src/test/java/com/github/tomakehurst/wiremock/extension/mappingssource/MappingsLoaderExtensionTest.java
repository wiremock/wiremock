/*
 * Copyright (C) 2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.google.common.io.Resources;
import java.io.File;
import java.net.URISyntaxException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MappingsLoaderExtensionTest {

  private WireMockConfiguration configuration;
  private WireMockServer wireMockServer;
  private WireMockTestClient testClient;

  @BeforeEach
  public void init() {
    configuration = wireMockConfig().dynamicPort();
  }

  @AfterEach
  public void stopWireMock() {
    wireMockServer.stop();
  }

  private void buildWireMock(Options options) {
    wireMockServer = new WireMockServer(options);
    wireMockServer.start();
    testClient = new WireMockTestClient(wireMockServer.port());
  }

  @Test
  public void mappingsLoadedFromJsonFilesWithMultipleMappingsSource() {
    FileSource filesRoot = new SingleRootFileSource(filePath("extension-test-request"));
    buildWireMock(
        configuration.extensions(new DummyMappingsLoaderExtension(filesRoot, new FilenameMaker())));

    WireMockResponse response = testClient.get("/extension/resource/1");
    assertThat(response.statusCode(), is(200));
  }

  public static String filePath(String path) {
    try {
      return new File(Resources.getResource(path).toURI()).getAbsolutePath();
    } catch (URISyntaxException e) {
      return throwUnchecked(e, String.class);
    }
  }
}
