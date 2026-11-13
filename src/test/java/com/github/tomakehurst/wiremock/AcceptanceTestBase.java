/*
 * Copyright (C) 2011-2026 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples.BASIC_ANY_METHOD;
import static com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples.BASIC_DELETE;
import static com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples.BASIC_GET;
import static com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples.BASIC_HEAD;
import static com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples.BASIC_OPTIONS;
import static com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples.BASIC_PATCH;
import static com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples.BASIC_POST;
import static com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples.BASIC_PUT;
import static com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples.BASIC_QUERY;
import static com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples.BASIC_TRACE;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.defaultTestFilesRoot;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.filePath;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import com.github.tomakehurst.wiremock.client.ExternalAdminClient;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.Stubbing;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.Arguments;

public class AcceptanceTestBase {

  protected static WireMockServer wireMockServer;
  protected static WireMockTestClient testClient;

  protected static Stubbing wm;

  protected static ExternalWireMock externalWireMock;
  private static String externalRootDirectory;

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
    if (externalWireMock != null) {
      ExternalWireMock external = externalWireMock;
      externalWireMock = null;
      external.stop();
    } else if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  public static void setupServerWithEmptyFileRoot() {
    setupServerWithRootDirectory(filePath("empty"));
  }

  public static File setupServerWithTempFileRoot() {
    File tempFileRoot = setupTempFileRoot();
    setupServerWithRootDirectory(tempFileRoot.getAbsolutePath());
    return tempFileRoot;
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
    setupServerWithRootDirectory(defaultTestFilesRoot());
  }

  private static void setupServerWithRootDirectory(String rootDirectory) {
    if (!ExternalWireMock.enabled()) {
      setupServer(wireMockConfig().withRootDirectory(rootDirectory));
      return;
    }

    externalWireMock = ExternalWireMock.start(rootDirectory);
    externalRootDirectory = rootDirectory;
    wireMockServer = null;
    testClient = new WireMockTestClient(externalWireMock.port());
    WireMock.configureFor(externalWireMock.client());
    wm = externalWireMock;
  }

  public static void setupServer(WireMockConfiguration options) {
    assumeFalse(
        ExternalWireMock.enabled(),
        "Custom in-process server options are not supported against an external WireMock");

    System.out.println(
        "Configuring WireMockServer with root directory: " + options.filesRoot().getPath());

    // SERVER_PORT + CLIENT PORT are here to support routing through an external proxy for e.g.
    // validation
    final String serverPort = System.getenv("SERVER_PORT");
    if (serverPort != null) {
      options.port(Integer.parseInt(serverPort));
    } else if (options.portNumber() == Options.DEFAULT_PORT) {
      options.dynamicPort();
    }

    options.bindAddress("127.0.0.1");

    wireMockServer = new WireMockServer(options);
    wireMockServer.start();
    testClient = new WireMockTestClient(wireMockServer.port());

    int clientPort =
        Optional.ofNullable(System.getenv("CLIENT_PORT"))
            .map(Integer::parseInt)
            .orElse(wireMockServer.port());

    WireMock wireMockClient = new WireMock(clientPort);
    WireMock.configureFor(wireMockClient);
    wm = wireMockServer;
  }

  protected static int serverPort() {
    return externalWireMock != null ? externalWireMock.port() : wireMockServer.port();
  }

  protected static Admin admin() {
    return externalWireMock != null
        ? ExternalAdminClient.forPort(externalWireMock.port())
        : wireMockServer;
  }

  protected static FileSource serverFilesRoot() {
    return externalWireMock != null
        ? new SingleRootFileSource(externalRootDirectory)
        : wireMockServer.getOptions().filesRoot();
  }

  protected static String serverBaseUrl() {
    return "http://localhost:" + serverPort();
  }

  @BeforeEach
  public void init() throws InterruptedException {
    WireMock.resetToDefault();
  }

  public static Stream<Arguments> provideStubMappingJson() {
    return Stream.of(
        Arguments.of(BASIC_GET),
        Arguments.of(BASIC_POST),
        Arguments.of(BASIC_PUT),
        Arguments.of(BASIC_DELETE),
        Arguments.of(BASIC_PATCH),
        Arguments.of(BASIC_HEAD),
        Arguments.of(BASIC_OPTIONS),
        Arguments.of(BASIC_TRACE),
        Arguments.of(BASIC_ANY_METHOD),
        Arguments.of(BASIC_QUERY));
  }
}
