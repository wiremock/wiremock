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
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.message.MessagePattern.messagePattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.github.tomakehurst.wiremock.message.Message;
import com.github.tomakehurst.wiremock.message.channel.InMemoryChannelProviderDriver;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the built-in {@link InMemoryChannelProviderDriver} routes inbound messages through
 * WireMock's stub-matching pipeline when its {@code receive()} method is called directly.
 */
public class InMemoryChannelDriverReceiveAcceptanceTest {

  static final String PROVIDER = "events";
  static final String CHANNEL = "orders";

  static WireMockServer wireMockServer;
  static WireMockTestClient testClient;

  /**
   * A directly-accessible instance of the in-memory driver registered as a custom extension so that
   * tests can call {@code driver.receive()} to simulate inbound messages.
   */
  static TestInMemoryDriver driver;

  @BeforeAll
  static void startServer() {
    driver = new TestInMemoryDriver();
    wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().extensions(driver));
    wireMockServer.start();
    testClient = new WireMockTestClient(wireMockServer.port());
    configureFor(wireMockServer.port());

    registerChannelProvider(channelProvider().named(PROVIDER).withDriver(TestInMemoryDriver.TYPE));
    createFixedChannel(fixedChannel().onProvider(PROVIDER).named(CHANNEL));
  }

  @AfterAll
  static void stopServer() {
    wireMockServer.stop();
  }

  @AfterEach
  void resetPerTest() {
    resetMessageStubs();
    resetMessageJournal();
  }

  @Test
  void deletedChannelSinkIsRemovedFromDriver() {
    UUID tempId = createFixedChannel(fixedChannel().onProvider(PROVIDER).named("temp"));
    removeMessageChannel(tempId);

    Assertions.assertThrows(
        IllegalStateException.class,
        () -> driver.receive(PROVIDER, "temp", Message.builder().withTextBody("test").build()));
  }

  @Test
  void inboundMessageViaDriverReceiveTriggersSend() {
    messageStubFor(
        message()
            .withName("Echo via in-memory driver receive")
            .triggeredByMessageOnChannel(PROVIDER, CHANNEL)
            .withBody(equalTo("ping"))
            .willTriggerActions(sendMessage().withBody("pong").onChannel(PROVIDER, CHANNEL)));

    driver.receive(PROVIDER, CHANNEL, Message.builder().withTextBody("ping").build());

    Optional<MessageServeEvent> event =
        waitForMessageEvent(
            messagePattern().withBody(equalTo("pong")).build(), Duration.ofSeconds(5));

    assertThat(event.isPresent(), is(true));
    assertThat(event.get().getMessage().getBodyAsString(), is("pong"));
  }

  @Test
  void unmatchedInboundViaDriverReceiveIsRecordedAsUnmatched() {
    driver.receive(PROVIDER, CHANNEL, Message.builder().withTextBody("no-stub-matches").build());

    Optional<MessageServeEvent> event =
        waitForMessageEvent(
            messagePattern().withBody(equalTo("no-stub-matches")).build(), Duration.ofSeconds(5));

    assertThat(event.isPresent(), is(true));
    assertThat(event.get().getWasMatched(), is(false));
    assertThat(event.get().getChannel(), notNullValue());
  }

  @Test
  void matchedInboundViaDriverReceiveHasStubMappingAttached() {
    var stub =
        messageStubFor(
            message()
                .withName("Stub for mapping test")
                .triggeredByMessageOnChannel(PROVIDER, CHANNEL)
                .withBody(equalTo("check-stub"))
                .willTriggerActions(sendMessage().withBody("reply").onChannel(PROVIDER, CHANNEL)));

    driver.receive(PROVIDER, CHANNEL, Message.builder().withTextBody("check-stub").build());

    Optional<MessageServeEvent> event =
        waitForMessageEvent(
            messagePattern().withBody(equalTo("check-stub")).build(), Duration.ofSeconds(5));

    assertThat(event.isPresent(), is(true));
    assertThat(event.get().getWasMatched(), is(true));
    assertThat(event.get().getStubMapping(), notNullValue());
    assertThat(event.get().getStubMapping().getId(), is(stub.getId()));
  }

  /**
   * An {@link InMemoryChannelProviderDriver} that also implements {@link
   * com.github.tomakehurst.wiremock.message.channel.CustomChannelProviderDriver} so it is picked up
   * by WireMock's extension loading and registered as a driver, while also being directly
   * accessible from test code.
   */
  public static class TestInMemoryDriver extends InMemoryChannelProviderDriver
      implements com.github.tomakehurst.wiremock.message.channel.CustomChannelProviderDriver {

    public static final String TYPE = "test-in-memory";

    @Override
    public String getType() {
      return TYPE;
    }

    @Override
    public String getName() {
      return TYPE;
    }
  }
}
