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
package com.github.tomakehurst.wiremock.standalone;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MESSAGE_MAPPINGS_ROOT;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static java.lang.System.out;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FatalStartupException;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.Version;
import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;
import java.util.Set;

public class WireMockServerRunner {

  private static final String BANNER =
      "\n"
          + "\u001B[34m██     ██ ██ ██████  ███████ \u001B[33m███    ███  ██████   ██████ ██   ██ \n"
          + "\u001B[34m██     ██ ██ ██   ██ ██      \u001B[33m████  ████ ██    ██ ██      ██  ██  \n"
          + "\u001B[34m██  █  ██ ██ ██████  █████   \u001B[33m██ ████ ██ ██    ██ ██      █████   \n"
          + "\u001B[34m██ ███ ██ ██ ██   ██ ██      \u001B[33m██  ██  ██ ██    ██ ██      ██  ██  \n"
          + "\u001B[34m ███ ███  ██ ██   ██ ███████ \u001B[33m██      ██  ██████   ██████ ██   ██ \n"
          + "\n\u001B[0m"
          + "----------------------------------------------------------------\n"
          + "|               Cloud: https://wiremock.io/cloud               |\n"
          + "|                                                              |\n"
          + "|               Slack: https://slack.wiremock.org              |\n"
          + "----------------------------------------------------------------";

  private WireMockServer wireMockServer;
  private Thread shutdownHook;

  public void run(String... args) {
    CommandLineOptions options = new CommandLineOptions(args);
    if (options.help()) {
      out.println(options.helpText());
      return;
    }
    if (options.version()) {
      out.println(Version.getCurrentVersion());
      return;
    }

    FileSource fileSource = options.filesRoot();
    fileSource.createIfNecessary();
    FileSource filesFileSource = fileSource.child(FILES_ROOT);
    filesFileSource.createIfNecessary();
    FileSource mappingsFileSource = fileSource.child(MAPPINGS_ROOT);
    mappingsFileSource.createIfNecessary();
    FileSource messageMappingsFileSource = fileSource.child(MESSAGE_MAPPINGS_ROOT);
    messageMappingsFileSource.createIfNecessary();

    wireMockServer = new WireMockServer(options);

    if (options.recordMappingsEnabled()) {
      startRecordingWithOptions(options);
    }

    if (options.specifiesProxyUrl()) {
      addProxyMapping(options.proxyUrl());
    }

    try {
      wireMockServer.start();

      // Add shutdown hook to snapshot recordings before JVM exits
      if (options.recordMappingsEnabled()) {
        shutdownHook = new Thread(this::stopRecordingIfNecessary);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
      }
      boolean https = options.httpsSettings().enabled();

      if (!options.getHttpDisabled()) {
        options.setActualHttpPort(wireMockServer.port());
      }

      if (https) {
        options.setActualHttpsPort(wireMockServer.httpsPort());
      }

      if (!options.bannerDisabled()) {
        out.println(BANNER);
        out.println();
      } else {
        out.println();
        out.println("The WireMock server is started .....");
      }
      out.println(options);

      final Set<String> loadedExtensionNames = wireMockServer.getLoadedExtensionNames();
      if (!loadedExtensionNames.isEmpty()) {
        out.println("extensions:                   " + String.join(",", loadedExtensionNames));
      }

    } catch (FatalStartupException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }

  private void startRecordingWithOptions(CommandLineOptions options) {
    RecordSpecBuilder recordSpecBuilder =
        new RecordSpecBuilder()
            .makeStubsPersistent(true)
            .ignoreRepeatRequests()
            .extractBinaryBodiesOver(0)
            .extractTextBodiesOver(0);

    List<CaseInsensitiveKey> matchingHeaders = options.matchingHeaders();
    for (CaseInsensitiveKey header : matchingHeaders) {
      recordSpecBuilder.captureHeader(header.value());
    }

    wireMockServer.startRecording(recordSpecBuilder);
  }

  private void addProxyMapping(final String baseUrl) {
    wireMockServer.loadMappingsUsing(
        stubMappings -> {
          RequestPattern requestPattern = newRequestPattern(ANY, anyUrl()).build();
          ResponseDefinition responseDef = responseDefinition().proxiedFrom(baseUrl).build();

          StubMapping proxyBasedMapping =
              StubMapping.builder()
                  .setRequest(requestPattern)
                  .setResponse(responseDef)
                  .setPriority(10)
                  .build();

          stubMappings.addMapping(proxyBasedMapping);
        });
  }

  public void stop() {
    if (wireMockServer != null) {
      if (wireMockServer.getRecordingStatus().getStatus() == RecordingStatus.Recording) {
        stopRecordingIfNecessary();

        // Remove shutdown hook to prevent double snapshotting
        if (shutdownHook != null) {
          try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
          } catch (IllegalStateException e) {
          }
        }
      }
      wireMockServer.stop();
    }
  }

  private void stopRecordingIfNecessary() {
    if (wireMockServer.getRecordingStatus().getStatus() == RecordingStatus.Recording) {
      wireMockServer.stopRecording();
    }
  }

  public boolean isRunning() {
    if (wireMockServer == null) {
      return false;
    } else {
      return wireMockServer.isRunning();
    }
  }

  public int port() {
    return wireMockServer.port();
  }
}
