/*
 * Copyright (C) 2023-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.webhooks;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.standalone.WireMockServerRunner;
import com.github.tomakehurst.wiremock.testsupport.TestNotifier;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wiremock.webhooks.Webhooks;

class WebhooksRegistrationTest {
  private static final String MESSAGE =
      "Passing webhooks in extensions is no longer required and"
          + " may lead to compatibility issues in future";
  private WireMockServerRunner runner;
  private WireMockServer server;

  private final PrintStream stdOut = System.out;
  private ByteArrayOutputStream out;

  @BeforeEach
  public void recordCommandLineMessages() {
    startRecordingSystemOut();
  }

  @AfterEach
  public void resetPrintStream() {
    System.setOut(stdOut);
    stopServer();
  }

  private void startRecordingSystemOut() {
    out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
  }

  private void stopServer() {
    if (server != null && server.isRunning()) {
      server.stop();
    }
  }

  private void stopRunner() {
    if (runner != null && runner.isRunning()) {
      runner.stop();
    }
  }

  private String getSystemOutText() {
    return out.toString();
  }

  @Test
  void shouldLogMessageWhenWebhooksAreAddedViaClassName() {
    server =
        new WireMockServer(
            wireMockConfig().extensions("org.wiremock.webhooks.Webhooks").dynamicPort());
    server.start();
    assertThat(getSystemOutText(), containsString(MESSAGE));
  }

  @Test
  void shouldLogMessageWhenWebhooksAreAddedViaClass() {
    server = new WireMockServer(wireMockConfig().extensions(Webhooks.class).dynamicPort());
    server.start();
    assertThat(getSystemOutText(), containsString(MESSAGE));
  }

  @Test
  void shouldLogAMessageWhenWebhooksAreAddedViaCLI() {
    runner = new WireMockServerRunner();
    runner.run("--extensions", "org.wiremock.webhooks.Webhooks", "--port", "0");
    assertThat(getSystemOutText(), containsString(MESSAGE));
    stopRunner();
  }

  @Test
  void shouldNotLogAMessageWhenWebhooksAreNotAddedExplicitly() {
    server = new WireMockServer(wireMockConfig().dynamicPort());
    server.start();
    assertThat(getSystemOutText(), not(containsString(MESSAGE)));
  }
}
