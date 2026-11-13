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

import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.MessageStubMappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.Stubbing;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.message.MessagePattern;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Runs WireMock as a separate process (the standalone JVM jar or the GraalVM native binary) so the
 * acceptance tests can be pointed at a packaged distribution. Enabled by setting the {@code
 * wiremock.acceptance.externalCommand} system property to the launch command.
 */
public class ExternalWireMock implements Stubbing {

  public static final String COMMAND_PROPERTY = "wiremock.acceptance.externalCommand";

  private static final Pattern MISSING_REGISTRATION =
      Pattern.compile("Missing\\w*RegistrationError|MissingRegistrationReporting");

  private final Process process;
  private final Path log;
  private final int port;
  private final WireMock client;

  public static boolean enabled() {
    return externalCommand() != null;
  }

  private static String externalCommand() {
    String command = System.getProperty(COMMAND_PROPERTY);
    return command == null || command.isBlank() ? null : command;
  }

  public static ExternalWireMock start(String rootDirectory, String... extraArgs) {
    List<String> args =
        new ArrayList<>(List.of("--root-dir", new File(rootDirectory).getAbsolutePath()));
    args.addAll(List.of(extraArgs));
    return launch(args);
  }

  public static ExternalWireMock launch(List<String> args) {
    for (int attempt = 1; ; attempt++) {
      try {
        return new ExternalWireMock(args);
      } catch (PortRace e) {
        if (attempt == 3) {
          throw e;
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private ExternalWireMock(List<String> args) throws IOException {
    List<String> serverArgs = new ArrayList<>(args);
    int portIndex = serverArgs.indexOf("--port");
    if (portIndex < 0) {
      serverArgs.addAll(List.of("--port", "0"));
      portIndex = serverArgs.size() - 2;
    }
    if (serverArgs.get(portIndex + 1).equals("0")) {
      serverArgs.set(portIndex + 1, String.valueOf(freePort()));
    }
    port = Integer.parseInt(serverArgs.get(portIndex + 1));
    if (!serverArgs.contains("--bind-address")) {
      serverArgs.addAll(List.of("--bind-address", "127.0.0.1"));
    }
    if (!serverArgs.contains("--disable-banner")) {
      serverArgs.add("--disable-banner");
    }

    log = Files.createTempFile("wiremock-external-", ".log");
    // ponytail: whitespace split, so the command path must not contain spaces
    List<String> command = new ArrayList<>(List.of(externalCommand().trim().split("\\s+")));
    command.addAll(serverArgs);
    process =
        new ProcessBuilder(command)
            .redirectErrorStream(true)
            .redirectOutput(log.toFile())
            .start();
    client = WireMock.create().host("127.0.0.1").port(port).build();
    awaitHealthy();
  }

  private static class PortRace extends IllegalStateException {
    PortRace(String message) {
      super(message);
    }
  }

  private static int freePort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }

  private void awaitHealthy() throws IOException {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
    while (System.nanoTime() < deadline) {
      if (!process.isAlive()) {
        String message = "External WireMock exited with code " + process.exitValue() + ":\n" + output();
        throw output().contains("Failed to bind") ? new PortRace(message) : new IllegalStateException(message);
      }
      try {
        HttpURLConnection connection =
            (HttpURLConnection)
                URI.create("http://127.0.0.1:" + port + "/__admin/health").toURL().openConnection();
        if (connection.getResponseCode() == 200) {
          return;
        }
      } catch (IOException ignored) {
      }
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(e);
      }
    }
    stop();
    throw new IllegalStateException("External WireMock did not become healthy:\n" + output());
  }

  public int port() {
    return port;
  }

  public boolean isRunning() {
    return process.isAlive();
  }

  public WireMock client() {
    return client;
  }

  public String output() {
    try {
      return Files.readString(log);
    } catch (IOException e) {
      return "<unable to read " + log + ": " + e + ">";
    }
  }

  public void stop() {
    process.destroy();
    try {
      if (!process.waitFor(30, TimeUnit.SECONDS)) {
        process.destroyForcibly().waitFor();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    String output = output();
    if (MISSING_REGISTRATION.matcher(output).find()) {
      throw new AssertionError(
          "External WireMock reported missing native-image metadata (see " + log + "):\n" + output);
    }
  }

  @Override
  public StubMapping givenThat(MappingBuilder mappingBuilder) {
    return client.register(mappingBuilder);
  }

  @Override
  public StubMapping stubFor(MappingBuilder mappingBuilder) {
    return givenThat(mappingBuilder);
  }

  @Override
  public void editStub(MappingBuilder mappingBuilder) {
    client.editStubMapping(mappingBuilder);
  }

  @Override
  public void removeStub(MappingBuilder mappingBuilder) {
    client.removeStubMapping(mappingBuilder);
  }

  @Override
  public void removeStub(StubMapping stubMapping) {
    client.removeStubMapping(stubMapping);
  }

  @Override
  public void removeStub(UUID id) {
    client.removeStubMapping(id);
  }

  @Override
  public List<StubMapping> getStubMappings() {
    return client.allStubMappings().getMappings();
  }

  @Override
  public StubMapping getSingleStubMapping(UUID id) {
    return client.getStubMapping(id).getItem();
  }

  @Override
  public List<StubMapping> findStubMappingsByMetadata(StringValuePattern pattern) {
    return client.findAllStubsByMetadata(pattern);
  }

  @Override
  public void removeStubMappingsByMetadata(StringValuePattern pattern) {
    client.removeStubsByMetadataPattern(pattern);
  }

  @Override
  public void verify(RequestPatternBuilder requestPatternBuilder) {
    client.verifyThat(requestPatternBuilder);
  }

  @Override
  public void verify(int count, RequestPatternBuilder requestPatternBuilder) {
    client.verifyThat(count, requestPatternBuilder);
  }

  @Override
  public void verify(
      CountMatchingStrategy countMatchingStrategy, RequestPatternBuilder requestPatternBuilder) {
    client.verifyThat(countMatchingStrategy, requestPatternBuilder);
  }

  @Override
  public List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder) {
    return client.find(requestPatternBuilder);
  }

  @Override
  public List<ServeEvent> getAllServeEvents() {
    return client.getServeEvents();
  }

  @Override
  public void setGlobalFixedDelay(int milliseconds) {
    client.setGlobalFixedDelayVariable(milliseconds);
  }

  @Override
  public List<LoggedRequest> findAllUnmatchedRequests() {
    return client.findAllUnmatchedRequests();
  }

  @Override
  public List<NearMiss> findNearMissesForAllUnmatchedRequests() {
    return client.findNearMissesForAllUnmatchedRequests();
  }

  @Override
  public List<NearMiss> findNearMissesFor(LoggedRequest loggedRequest) {
    return client.findTopNearMissesFor(loggedRequest);
  }

  @Override
  public List<NearMiss> findAllNearMissesFor(RequestPatternBuilder requestPatternBuilder) {
    return client.findAllNearMissesFor(requestPatternBuilder);
  }

  @Override
  public MessageStubMapping messageStubFor(MessageStubMappingBuilder builder) {
    return client.registerMessageStub(builder);
  }

  @Override
  public MessageStubMapping messageStubFor(MessageStubMapping messageStubMapping) {
    return client.registerMessageStub(messageStubMapping);
  }

  @Override
  public void removeMessageStub(UUID id) {
    client.deleteMessageStub(id);
  }

  @Override
  public List<MessageStubMapping> getMessageStubMappingsList() {
    return new ArrayList<>(client.allMessageStubMappings().getMessageMappings());
  }

  @Override
  public void resetMessageStubs() {
    client.resetAllMessageStubs();
  }

  @Override
  public List<MessageServeEvent> getAllMessageServeEvents() {
    return client.getMessageServeEvents();
  }

  @Override
  public List<MessageServeEvent> findAllMessageEvents(MessagePattern pattern) {
    return client.findMessageEvents(pattern);
  }

  @Override
  public void verifyMessageEvent(MessagePattern pattern) {
    client.verifyThatMessageEvent(pattern);
  }

  @Override
  public void verifyMessageEvent(int expectedCount, MessagePattern pattern) {
    client.verifyThatMessageEvent(expectedCount, pattern);
  }

  @Override
  public void verifyMessageEvent(CountMatchingStrategy expectedCount, MessagePattern pattern) {
    client.verifyThatMessageEvent(expectedCount, pattern);
  }

  @Override
  public Optional<MessageServeEvent> waitForMessageEvent(MessagePattern pattern, Duration maxWait) {
    return client.waitForMessage(pattern, maxWait);
  }

  @Override
  public List<MessageServeEvent> waitForMessageEvents(
      MessagePattern pattern, int count, Duration maxWait) {
    return client.waitForMessages(pattern, count, maxWait);
  }

  @Override
  public void resetMessageJournal() {
    client.resetMessages();
  }
}
