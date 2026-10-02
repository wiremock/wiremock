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
package com.github.tomakehurst.wiremock.verification;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.channel.ChannelProvider;
import com.github.tomakehurst.wiremock.message.channel.InMemoryChannelProviderDriver;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LoggedMessageChannelTest {

  private static final UUID FIXED_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private static final String INITIATING_REQUEST_JSON =
      // language=json
      """
      {
        "url" : "/ws-echo",
        "method" : "GET"
      }""";

  private static final LoggedRequest INITIATING_REQUEST =
      Json.read(INITIATING_REQUEST_JSON, LoggedRequest.class);

  @Test
  void serialisesFixedChannel() {
    LoggedFixedChannel channel = new LoggedFixedChannel(FIXED_ID, true, "events", "orders");

    assertThat(
        Json.write(channel),
        jsonEquals(
            // language=json
            """
            {
              "type" : "fixed",
              "id" : "00000000-0000-0000-0000-000000000001",
              "open" : true,
              "providerName" : "events",
              "channelName" : "orders"
            }"""));
  }

  @Test
  void deserialisesFixedChannelFromJson() {
    String json =
        // language=json
        """
        {
          "id": "00000000-0000-0000-0000-000000000001",
          "type": "fixed",
          "open": true,
          "providerName": "events",
          "channelName": "orders"
        }
        """;

    LoggedMessageChannel channel = Json.read(json, LoggedMessageChannel.class);

    assertThat(channel, instanceOf(LoggedFixedChannel.class));
    LoggedFixedChannel fixed = (LoggedFixedChannel) channel;
    assertThat(fixed.getId(), is(FIXED_ID));
    assertThat(fixed.getType(), is(ChannelType.FIXED));
    assertThat(fixed.isOpen(), is(true));
    assertThat(fixed.getProviderName(), is("events"));
    assertThat(fixed.getChannelName(), is("orders"));
  }

  @Test
  void serialisesRequestInitiatedChannel() {
    LoggedRequestInitiatedChannel channel =
        new LoggedRequestInitiatedChannel(
            FIXED_ID, ChannelType.WEBSOCKET, INITIATING_REQUEST, true);

    assertThat(
        Json.write(channel),
        jsonEquals(
            // language=json
            """
            {
              "type" : "websocket",
              "id" : "00000000-0000-0000-0000-000000000001",
              "open" : true,
              "initiatingRequest" : {
                "url" : "/ws-echo",
                "method" : "GET"
              }
            }"""));
  }

  @Test
  void deserialisesRequestInitiatedChannelFromJson() {
    String json =
        // language=json
        """
        {
          "id": "00000000-0000-0000-0000-000000000001",
          "type": "websocket",
          "open": true,
          "initiatingRequest": {
            "url": "/ws-echo",
            "method": "GET"
          }
        }
        """;

    LoggedMessageChannel channel = Json.read(json, LoggedMessageChannel.class);

    assertThat(channel, instanceOf(LoggedRequestInitiatedChannel.class));
    LoggedRequestInitiatedChannel requestInitiated = (LoggedRequestInitiatedChannel) channel;
    assertThat(requestInitiated.getId(), is(FIXED_ID));
    assertThat(requestInitiated.getType(), is(ChannelType.WEBSOCKET));
    assertThat(requestInitiated.isOpen(), is(true));
    assertThat(requestInitiated.getInitiatingRequest(), notNullValue());
    assertThat(requestInitiated.getInitiatingRequest().getUrl(), is("/ws-echo"));
  }

  @Test
  void fixedChannelRoundTripsViaJson() {
    LoggedFixedChannel original =
        new LoggedFixedChannel(FIXED_ID, true, "my-provider", "my-channel");

    String json = Json.write(original);

    assertThat(
        json,
        jsonEquals(
            // language=json
            """
            {
              "type" : "fixed",
              "id" : "00000000-0000-0000-0000-000000000001",
              "open" : true,
              "providerName" : "my-provider",
              "channelName" : "my-channel"
            }"""));

    LoggedMessageChannel roundTripped = Json.read(json, LoggedMessageChannel.class);
    assertThat(roundTripped, instanceOf(LoggedFixedChannel.class));
    assertThat(((LoggedFixedChannel) roundTripped).getProviderName(), is("my-provider"));
    assertThat(((LoggedFixedChannel) roundTripped).getChannelName(), is("my-channel"));
  }

  @Test
  void requestInitiatedChannelRoundTripsViaJson() {
    LoggedRequestInitiatedChannel original =
        new LoggedRequestInitiatedChannel(
            FIXED_ID, ChannelType.WEBSOCKET, INITIATING_REQUEST, true);

    String json = Json.write(original);

    assertThat(
        json,
        jsonEquals(
            // language=json
            """
            {
              "type" : "websocket",
              "id" : "00000000-0000-0000-0000-000000000001",
              "open" : true,
              "initiatingRequest" : {
                "url" : "/ws-echo",
                "method" : "GET"
              }
            }"""));

    LoggedMessageChannel roundTripped = Json.read(json, LoggedMessageChannel.class);
    assertThat(roundTripped, instanceOf(LoggedRequestInitiatedChannel.class));
    assertThat(roundTripped.getId(), is(FIXED_ID));
    assertThat(roundTripped.getType(), is(ChannelType.WEBSOCKET));
    assertThat(
        ((LoggedRequestInitiatedChannel) roundTripped).getInitiatingRequest().getUrl(),
        is("/ws-echo"));
  }

  @Test
  void fixedChannelTransformProducesUpdatedCopy() {
    LoggedFixedChannel original = new LoggedFixedChannel(FIXED_ID, true, "events", "orders");

    LoggedFixedChannel transformed = original.transform(b -> b.channelName("notifications"));

    assertThat(transformed.getChannelName(), is("notifications"));
    assertThat(transformed.getProviderName(), is("events"));
    assertThat(original.getChannelName(), is("orders"));
  }

  @Test
  void requestInitiatedChannelTransformProducesUpdatedCopy() {
    LoggedRequestInitiatedChannel original =
        new LoggedRequestInitiatedChannel(
            FIXED_ID, ChannelType.WEBSOCKET, INITIATING_REQUEST, true);

    LoggedRequestInitiatedChannel transformed = original.transform(b -> b.open(false));

    assertThat(transformed.isOpen(), is(false));
    assertThat(transformed.getInitiatingRequest().getUrl(), is("/ws-echo"));
    assertThat(original.isOpen(), is(true));
  }

  @Test
  void createFromFixedChannelProducesLoggedFixedChannel() {
    com.github.tomakehurst.wiremock.message.FixedChannel channel =
        new com.github.tomakehurst.wiremock.message.FixedChannel(
            new InMemoryChannelProviderDriver(),
            new ChannelProvider("events", "in-memory", Collections.emptyMap()),
            "orders");

    LoggedMessageChannel logged = LoggedMessageChannel.createFrom(channel);

    assertThat(logged, instanceOf(LoggedFixedChannel.class));
    LoggedFixedChannel fixed = (LoggedFixedChannel) logged;
    assertThat(fixed.getId(), is(channel.getId()));
    assertThat(fixed.getProviderName(), is("events"));
    assertThat(fixed.getChannelName(), is("orders"));
    assertThat(fixed.isOpen(), is(true));
  }
}
