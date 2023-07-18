/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.ExtensionFactoryUtils.buildExtension;
import static com.github.tomakehurst.wiremock.testsupport.ExtensionFactoryUtils.buildTemplateTransformer;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.defaultTestFilesRoot;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.filePath;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.Stubbing;
import com.github.tomakehurst.wiremock.testsupport.MockWireMockServices;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class AcceptanceTestBase {

  protected static WireMockServer wireMockServer;
  protected static WireMockTestClient testClient;

  protected static Stubbing wm;

  @BeforeAll
  public static void setupServer() {
    setupServerWithEmptyFileRoot();

    // We assert English XML parser error messages in some tests. So we set our default locale to
    // English to make
    // those tests succeed even for users with non-English default locales.
    Locale.setDefault(Locale.ENGLISH);
  }

  @AfterAll
  public static void serverShutdown() {
    wireMockServer.stop();
  }

  public static void setupServerWithEmptyFileRoot() {
    setupServer(wireMockConfig().withRootDirectory(filePath("empty")));
  }

  public static void setupServerWithTempFileRoot() {
    setupServer(
        wireMockConfig()
            .withRootDirectory(setupTempFileRoot().getAbsolutePath())
            // TODO:: It should be as default for dynamic scenarios
            .extensions(
                buildExtension(
                    new MockWireMockServices(),
                    services -> List.of(buildTemplateTransformer(false)))));
  }

  public static File setupTempFileRoot() {
    try {
      File root = Files.createTempDirectory("wiremock").toFile();
      new File(root, MAPPINGS_ROOT).mkdirs();
      new File(root, FILES_ROOT).mkdirs();
      return root;
    } catch (IOException e) {
      return throwUnchecked(e, File.class);
    }
  }

  public static void setupServerWithMappingsInFileRoot() {
    setupServer(wireMockConfig().withRootDirectory(defaultTestFilesRoot()));
  }

  public static void setupServer(WireMockConfiguration options) {
    System.out.println(
        "Configuring WireMockServer with root directory: " + options.filesRoot().getPath());
    if (options.portNumber() == Options.DEFAULT_PORT) {
      options.dynamicPort();
    }

    wireMockServer = new WireMockServer(options);
    wireMockServer.start();
    testClient = new WireMockTestClient(wireMockServer.port());
    WireMock.configureFor(wireMockServer.port());
    wm = wireMockServer;
  }

  @BeforeEach
  public void init() throws InterruptedException {
    WireMock.resetToDefault();
  }
}
