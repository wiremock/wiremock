/*
 * Copyright (C) 2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.Message;
import com.github.tomakehurst.wiremock.message.MessageDefinition;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.message.MessageStubRequestHandler;
import com.github.tomakehurst.wiremock.message.SendMessageAction;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class MessageSerializationTest {

  private static Message message(String text) {
    return MessageStubRequestHandler.resolveToMessage(MessageDefinition.fromString(text));
  }

  @Test
  void messageStubMappingSerializesToJson() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withId(UUID.fromString("d8e8fca2-dc0f-11db-8314-0800200c9a66"))
            .withName("Test message stub")
            .withPriority(3)
            .withChannelPattern(newRequestPattern().withUrl("/test-channel").build())
            .withBody(equalTo("hello"))
            .triggersAction(SendMessageAction.toOriginatingChannel("world"))
            .build();

    String json = Json.write(stub);

    assertThat(
        json,
        jsonEquals(
            """
            {
              "id": "d8e8fca2-dc0f-11db-8314-0800200c9a66",
              "name": "Test message stub",
              "priority": 3,
              "channelPattern": {
                "url": "/test-channel",
                "method": "ANY"
              },
              "messagePattern": {
                "equalTo": "hello"
              },
              "actions": [
                {
                  "type": "send",
                  "body": "world",
                  "sendToOriginatingChannel": true
                }
              ]
            }
            """));
  }

  @Test
  void messageStubMappingDeserializesFromJson() {
    String json =
        """
        {
          "id": "d8e8fca2-dc0f-11db-8314-0800200c9a66",
          "name": "Deserialized stub",
          "priority": 5,
          "channelPattern": {
            "url": "/my-channel"
          },
          "messagePattern": {
            "matches": "hello.*"
          },
          "actions": [
            {
              "type": "send",
              "body": "response",
              "sendToOriginatingChannel": true
            }
          ]
        }
        """;

    MessageStubMapping stub = Json.read(json, MessageStubMapping.class);

    assertThat(stub.getId().toString(), is("d8e8fca2-dc0f-11db-8314-0800200c9a66"));
    assertThat(stub.getName(), is("Deserialized stub"));
    assertThat(stub.getPriority(), is(5));
    assertThat(stub.getChannelPattern().getUrl(), is("/my-channel"));
    assertThat(stub.getMessagePattern(), notNullValue());
    assertThat(stub.getActions().size(), is(1));
    assertThat(stub.getActions().get(0), is(SendMessageAction.toOriginatingChannel("response")));
  }

  @Test
  void messageStubMappingRoundTrips() {
    MessageStubMapping original =
        MessageStubMapping.builder()
            .withName("Round trip stub")
            .withChannelPattern(newRequestPattern().withUrl("/round-trip").build())
            .withBody(matching("test-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("response"))
            .build();

    String json = Json.write(original);
    MessageStubMapping deserialized = Json.read(json, MessageStubMapping.class);

    assertThat(deserialized.getId(), is(original.getId()));
    assertThat(deserialized.getName(), is(original.getName()));
    assertThat(deserialized.getChannelPattern().getUrl(), is("/round-trip"));
    assertThat(deserialized.getActions().size(), is(1));
  }

  @Test
  void messageServeEventSerializesToJson() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withId(UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
            .withName("Matched stub")
            .withBody(equalTo("test"))
            .triggersAction(SendMessageAction.toOriginatingChannel("response"))
            .build();

    MessageServeEvent event =
        new MessageServeEvent.Builder()
            .withId(UUID.fromString("eeeeeeee-ffff-0000-1111-222222222222"))
            .withEventType(MessageServeEvent.EventType.RECEIVED)
            .withChannelType(ChannelType.WEBSOCKET)
            .withChannelId(UUID.fromString("11111111-2222-3333-4444-555555555555"))
            .withMessage(message("test message"))
            .withStubMapping(stub)
            .withWasMatched(true)
            .withTimestamp(java.time.Instant.parse("2025-01-15T10:30:00Z"))
            .build();

    String json = Json.write(event);

    assertThat(
        json,
        jsonEquals(
            """
            {
              "id": "eeeeeeee-ffff-0000-1111-222222222222",
              "eventType": "RECEIVED",
              "channelType": "WEBSOCKET",
              "channelId": "11111111-2222-3333-4444-555555555555",
              "message": "test message",
              "wasMatched": true,
              "timestamp": "2025-01-15T10:30:00Z",
              "stubMapping": {
                "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                "name": "Matched stub",
                "messagePattern": {
                  "equalTo": "test"
                },
                "actions": [
                  {
                    "type": "send",
                    "body": "response",
                    "sendToOriginatingChannel": true
                  }
                ]
              }
            }
            """));
  }

  @Test
  void messageServeEventDeserializesFromJson() {
    String json =
        """
        {
          "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
          "eventType": "RECEIVED",
          "channelType": "WEBSOCKET",
          "channelId": "11111111-2222-3333-4444-555555555555",
          "message": "hello world",
          "wasMatched": false,
          "timestamp": "2025-01-15T10:30:00Z"
        }
        """;

    MessageServeEvent event = Json.read(json, MessageServeEvent.class);

    assertThat(event.getId().toString(), is("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
    assertThat(event.getEventType(), is(MessageServeEvent.EventType.RECEIVED));
    assertThat(event.getChannelType(), is(ChannelType.WEBSOCKET));
    assertThat(event.getChannelId().toString(), is("11111111-2222-3333-4444-555555555555"));
    assertThat(event.getMessage().getBodyAsString(), is("hello world"));
    assertThat(event.getWasMatched(), is(false));
    assertThat(event.getTimestamp(), notNullValue());
  }

  @Test
  void messageServeEventRoundTrips() {
    MessageServeEvent original =
        MessageServeEvent.receivedUnmatched(
            ChannelType.WEBSOCKET,
            UUID.fromString("99999999-8888-7777-6666-555555555555"),
            mockRequest().url("/unmatched-channel"),
            message("unmatched message"));

    String json = Json.write(original);
    MessageServeEvent deserialized = Json.read(json, MessageServeEvent.class);

    assertThat(deserialized.getId(), is(original.getId()));
    assertThat(deserialized.getEventType(), is(MessageServeEvent.EventType.RECEIVED));
    assertThat(deserialized.getChannelType(), is(ChannelType.WEBSOCKET));
    assertThat(deserialized.getMessage().getBodyAsString(), is("unmatched message"));
    assertThat(deserialized.getWasMatched(), is(false));
  }

  @Test
  void messageStubMappingWithBroadcastActionSerializesCorrectly() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withId(UUID.fromString("bbbbbbbb-cccc-dddd-eeee-ffffffffffff"))
            .withName("Broadcast stub")
            .withChannelPattern(newRequestPattern().withUrl("/source").build())
            .withBody(equalTo("broadcast"))
            .triggersAction(
                SendMessageAction.toMatchingChannels(
                    "broadcasted", newRequestPattern().withUrl(urlEqualTo("/target")).build()))
            .build();

    String json = Json.write(stub);

    assertThat(
        json,
        jsonEquals(
            """
            {
              "id": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff",
              "name": "Broadcast stub",
              "channelPattern": {
                "url": "/source",
                "method": "ANY"
              },
              "messagePattern": {
                "equalTo": "broadcast"
              },
              "actions": [
                {
                  "type": "send",
                  "body": "broadcasted",
                  "targetChannelPattern": {
                    "url": "/target",
                    "method": "ANY"
                  },
                  "sendToOriginatingChannel": false
                }
              ]
            }
            """));
  }

  @Test
  void messageStubMappingWithBroadcastActionDeserializesCorrectly() {
    String json =
        """
        {
          "name": "Broadcast deserialized",
          "channelPattern": {
            "url": "/source"
          },
          "messagePattern": {
            "equalTo": "trigger"
          },
          "actions": [
            {
              "type": "send",
              "body": "broadcast-message",
              "targetChannelPattern": {
                "url": "/target-channel"
              },
              "sendToOriginatingChannel": false
            }
          ]
        }
        """;

    MessageStubMapping stub = Json.read(json, MessageStubMapping.class);

    assertThat(stub.getName(), is("Broadcast deserialized"));
    assertThat(stub.getActions().size(), is(1));

    SendMessageAction action = (SendMessageAction) stub.getActions().get(0);
    assertThat(action.getBody().getData(), is("broadcast-message"));
    assertThat(action.isSendToOriginatingChannel(), is(false));
    assertThat(action.getTargetChannelPattern().getUrl(), is("/target-channel"));
  }

  @Test
  void sentMessageServeEventSerializesCorrectly() {
    MessageServeEvent event =
        new MessageServeEvent.Builder()
            .withId(UUID.fromString("22222222-3333-4444-5555-666666666666"))
            .withEventType(MessageServeEvent.EventType.SENT)
            .withChannelType(ChannelType.WEBSOCKET)
            .withChannelId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))
            .withMessage(message("sent message"))
            .withWasMatched(true)
            .withTimestamp(java.time.Instant.parse("2025-01-15T12:00:00Z"))
            .build();

    String json = Json.write(event);

    assertThat(
        json,
        jsonEquals(
            """
            {
              "id": "22222222-3333-4444-5555-666666666666",
              "eventType": "SENT",
              "channelType": "WEBSOCKET",
              "channelId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
              "message": "sent message",
              "wasMatched": true,
              "timestamp": "2025-01-15T12:00:00Z"
            }
            """));
  }

  @Test
  void sentMessageServeEventDeserializesCorrectly() {
    String json =
        """
        {
          "id": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff",
          "eventType": "SENT",
          "channelType": "WEBSOCKET",
          "channelId": "22222222-3333-4444-5555-666666666666",
          "message": "outgoing message",
          "wasMatched": true,
          "timestamp": "2025-01-15T12:00:00Z"
        }
        """;

    MessageServeEvent event = Json.read(json, MessageServeEvent.class);

    assertThat(event.getEventType(), is(MessageServeEvent.EventType.SENT));
    assertThat(event.isSent(), is(true));
    assertThat(event.isReceived(), is(false));
    assertThat(event.getMessage().getBodyAsString(), is("outgoing message"));
  }
}
