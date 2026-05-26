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
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.message.MessagePattern.messagePattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class FixedMessageChannelAcceptanceTest extends AcceptanceTestBase {

  @AfterEach
  void resetMessageState() {
    resetMessageStubs();
    resetMessageJournal();
  }

  @Test
  void httpRequestTriggerSendsMessageToInMemoryFixedChannel() {
    registerChannelProvider(channelProvider().named("events").withDriver("in-memory"));
    createFixedChannel(fixedChannel().onProvider("events").named("orders"));

    messageStubFor(
        message()
            .withName("Send order event on HTTP trigger")
            .triggeredByHttpRequest(
                newRequestPattern().withUrl(urlPathEqualTo("/api/orders")))
            .willTriggerActions(
                sendMessage().withBody("order-created").onChannel("events", "orders")));

    testClient.get("/api/orders");

    Optional<MessageServeEvent> event =
        waitForMessageEvent(
            messagePattern().withBody(equalTo("order-created")).build(), Duration.ofSeconds(5));

    assertThat(event.isPresent(), is(true));
    assertThat(event.get().getMessage().getBodyAsString(), is("order-created"));
  }
}
