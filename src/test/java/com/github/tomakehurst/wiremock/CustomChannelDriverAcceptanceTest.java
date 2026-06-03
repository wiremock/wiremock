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

import static com.github.tomakehurst.wiremock.client.WireMock.channelProvider;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.createFixedChannel;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.fixedChannel;
import static com.github.tomakehurst.wiremock.client.WireMock.message;
import static com.github.tomakehurst.wiremock.client.WireMock.messageStubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.registerChannelProvider;
import static com.github.tomakehurst.wiremock.client.WireMock.resetMessageJournal;
import static com.github.tomakehurst.wiremock.client.WireMock.resetMessageStubs;
import static com.github.tomakehurst.wiremock.client.WireMock.sendMessage;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.waitAtMost;

import com.github.tomakehurst.wiremock.message.Message;
import com.github.tomakehurst.wiremock.message.channel.ChannelProvider;
import com.github.tomakehurst.wiremock.message.channel.CustomChannelProviderDriver;
import com.github.tomakehurst.wiremock.message.channel.InboundMessageSink;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CustomChannelDriverAcceptanceTest {

  private static WireMockServer wireMockServer;
  private static WireMockTestClient testClient;
  private static TestChannelProviderDriver driver;

  @BeforeAll
  static void startServer() {
    driver = new TestChannelProviderDriver();
    wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().extensions(driver));
    wireMockServer.start();
    testClient = new WireMockTestClient(wireMockServer.port());
    configureFor(wireMockServer.port());

    registerChannelProvider(channelProvider().named("custom-events").withDriver("test"));
    createFixedChannel(fixedChannel().onProvider("custom-events").named("orders"));
  }

  @AfterAll
  static void stopServer() {
    wireMockServer.stop();
  }

  @AfterEach
  void resetPerTest() {
    resetMessageStubs();
    resetMessageJournal();
    driver.reset();
  }

  @Test
  void stubActionSendsMessageViaCustomDriver() {
    messageStubFor(
        message()
            .withName("HTTP trigger via custom driver")
            .triggeredByHttpRequest(newRequestPattern().withUrl(urlPathEqualTo("/api/orders")))
            .willTriggerActions(
                sendMessage().withBody("order-placed").onChannel("custom-events", "orders")));

    testClient.get("/api/orders");

    waitAtMost(5, SECONDS).until(() -> driver.getMessages("orders").contains("order-placed"));
  }

  @Test
  void inboundMessageViaCustomDriverTriggersSend() {
    messageStubFor(
        message()
            .withName("Custom driver triggered echo")
            .triggeredByMessageOnChannel("custom-events", "orders")
            .withBody(equalTo("ping"))
            .willTriggerActions(
                sendMessage().withBody("pong").onChannel("custom-events", "orders")));

    driver.receive("orders", "ping");

    waitAtMost(5, SECONDS).until(() -> driver.getMessages("orders").contains("pong"));
  }

  public static class TestChannelProviderDriver implements CustomChannelProviderDriver {

    private final Map<String, InboundMessageSink> sinks = new ConcurrentHashMap<>();
    private final Map<String, List<String>> sentMessages = new ConcurrentHashMap<>();

    @Override
    public String getName() {
      return getType();
    }

    @Override
    public String getType() {
      return "test";
    }

    @Override
    public void createChannel(
        ChannelProvider provider, String channelName, InboundMessageSink sink) {
      sinks.put(channelName, sink);
    }

    @Override
    public void send(ChannelProvider provider, String channelName, Message message) {
      sentMessages
          .computeIfAbsent(channelName, k -> new CopyOnWriteArrayList<>())
          .add(message.getBodyAsString());
    }

    public void receive(String channelName, String body) {
      InboundMessageSink sink = sinks.get(channelName);
      sink.receive(Message.builder().withTextBody(body).build());
    }

    public List<String> getMessages(String channelName) {
      return sentMessages.getOrDefault(channelName, Collections.emptyList());
    }

    public void reset() {
      sentMessages.clear();
    }
  }
}
