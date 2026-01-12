/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.sendMessage;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.entity.BinaryEntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.CompressionType;
import com.github.tomakehurst.wiremock.common.entity.FormatType;
import com.github.tomakehurst.wiremock.common.entity.TextEntityDefinition;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.message.ChannelTarget;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.HttpRequestTrigger;
import com.github.tomakehurst.wiremock.message.HttpStubTrigger;
import com.github.tomakehurst.wiremock.message.IncomingMessageTrigger;
import com.github.tomakehurst.wiremock.message.Message;
import com.github.tomakehurst.wiremock.message.MessageDefinition;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.message.RequestInitiatedChannelTarget;
import com.github.tomakehurst.wiremock.message.SendMessageAction;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class MessageSerializationTest {

  private static Message message(String text) {
    return Message.builder().withTextBody(text).build();
  }

  @Test
  void messageStubMappingSerializesToJson() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withId(UUID.fromString("d8e8fca2-dc0f-11db-8314-0800200c9a66"))
            .withName("Test message stub")
            .withPriority(3)
            .onWebsocketChannelFromRequestMatching(newRequestPattern().withUrl("/test-channel"))
            .withBody(equalTo("hello"))
            .triggersAction(SendMessageAction.toOriginatingChannel("world"))
            .build();

    String json = Json.write(stub);

    assertThat(
        json,
        jsonEquals(
            // language=JSON
            """
            {
              "id": "d8e8fca2-dc0f-11db-8314-0800200c9a66",
              "name": "Test message stub",
              "priority": 3,
              "trigger": {
                "type": "message",
                "channel": {
                  "type": "websocket",
                  "initiatingRequestPattern": {
                    "url": "/test-channel",
                    "method": "ANY"
                  }
                },
                "message": {
                  "body": {
                    "equalTo": "hello"
                  }
                }
              },
              "actions": [
                {
                  "type": "send",
                  "message": {
                    "body": "world"
                  },
                  "channelTarget": {
                    "type": "originating"
                  }
                }
              ]
            }
            """));
  }

  @Test
  void messageStubMappingDeserializesFromJson() {
    String json =
        // language=JSON
        """
        {
          "id": "d8e8fca2-dc0f-11db-8314-0800200c9a66",
          "name": "Deserialized stub",
          "priority": 5,
          "trigger": {
            "channel": {
              "type": "websocket",
              "initiatingRequestPattern": {
                "url": "/my-channel"
              }
            },
            "message": {
              "body": {
                "matches": "hello.*"
              }
            }
          },
          "actions": [
            {
              "type": "send",
              "message": {
                "body": "response"
              },
              "channelTarget": {
                "type": "originating"
              }
            }
          ]
        }
        """;

    MessageStubMapping stub = Json.read(json, MessageStubMapping.class);

    assertThat(stub.getId().toString(), is("d8e8fca2-dc0f-11db-8314-0800200c9a66"));
    assertThat(stub.getName(), is("Deserialized stub"));
    assertThat(stub.getPriority(), is(5));

    assertThat(stub.getTrigger(), instanceOf(IncomingMessageTrigger.class));
    IncomingMessageTrigger trigger = (IncomingMessageTrigger) stub.getTrigger();
    assertThat(trigger.getInitiatingRequestPattern().getUrl(), is("/my-channel"));

    ContentPattern<?> bodyPattern = trigger.getBodyPattern();
    assertThat(bodyPattern, notNullValue());
    assertThat(bodyPattern, instanceOf(RegexPattern.class));
    assertThat(bodyPattern.getExpected(), is("hello.*"));

    RequestPattern initiatingRequestPattern = trigger.getInitiatingRequestPattern();
    assertThat(initiatingRequestPattern, notNullValue());
    assertThat(initiatingRequestPattern, is(any(urlEqualTo("/my-channel")).build().getRequest()));

    assertThat(stub.getActions().size(), is(1));
    assertThat(stub.getActions().get(0), is(SendMessageAction.toOriginatingChannel("response")));
  }

  @Test
  void messageStubMappingRoundTrips() {
    MessageStubMapping original =
        MessageStubMapping.builder()
            .withName("Round trip stub")
            .onWebsocketChannelFromRequestMatching(newRequestPattern().withUrl("/round-trip"))
            .withBody(matching("test-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("response"))
            .build();

    String json = Json.write(original);
    MessageStubMapping deserialized = Json.read(json, MessageStubMapping.class);

    assertThat(deserialized.getId(), is(original.getId()));
    assertThat(deserialized.getName(), is(original.getName()));

    assertThat(deserialized.getTrigger(), instanceOf(IncomingMessageTrigger.class));
    IncomingMessageTrigger trigger = (IncomingMessageTrigger) deserialized.getTrigger();
    assertThat(trigger.getInitiatingRequestPattern().getUrl(), is("/round-trip"));
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
            // language=JSON
            """
            {
              "id": "eeeeeeee-ffff-0000-1111-222222222222",
              "eventType": "received",
              "channelType": "websocket",
              "channelId": "11111111-2222-3333-4444-555555555555",
              "message": "test message",
              "wasMatched": true,
              "timestamp": "2025-01-15T10:30:00Z",
              "stubMapping": {
                "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                "name": "Matched stub",
                "trigger": {
                  "type": "message",
                  "message": {
                    "body": {
                      "equalTo": "test"
                    }
                  }
                },
                "actions": [
                  {
                    "type": "send",
                    "message": {
                      "body": "response"
                    },
                    "channelTarget": {
                      "type": "originating"
                    }
                  }
                ]
              }
            }
            """));
  }

  @Test
  void messageServeEventDeserializesFromJson() {
    String json =
        // language=JSON
        """
        {
          "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
          "eventType": "received",
          "channelType": "websocket",
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
  void minimalMessageStubDoesNotContainEmptyElementsWhenSerialized() {
    MessageStubMapping stub =
        WireMock.message()
            .withId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))
            .onWebsocketChannelFromRequestMatching("/trigger")
            .willTriggerActions(sendMessage("response").onOriginatingChannel());

    String json = Json.write(stub);

    assertThat(
        "JSON: " + json,
        json,
        jsonEquals(
            // language=JSON
            """
                    {
                      "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                      "trigger": {
                        "type": "message",
                        "channel": {
                          "type": "websocket",
                          "initiatingRequestPattern": {
                            "urlPath": "/trigger",
                            "method" : "ANY"
                          }
                        }
                      },
                      "actions": [
                        {
                          "type": "send",
                          "message": {
                            "body": {
                              "data": "response"
                            }
                          },
                          "channelTarget": {
                            "type": "originating"
                          }
                        }
                      ]
                    }
                    """));
  }

  @Test
  void messageStubMappingWithBroadcastActionSerializesCorrectly() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withId(UUID.fromString("bbbbbbbb-cccc-dddd-eeee-ffffffffffff"))
            .withName("Broadcast stub")
            .onWebsocketChannelFromRequestMatching(newRequestPattern().withUrl("/source"))
            .withBody(equalTo("broadcast"))
            .triggersAction(
                SendMessageAction.toMatchingChannels(
                    "broadcasted", newRequestPattern().withUrl(urlEqualTo("/target")).build()))
            .build();

    String json = Json.write(stub);

    assertThat(
        json,
        jsonEquals(
            // language=JSON
            """
            {
              "id": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff",
              "name": "Broadcast stub",
              "trigger": {
                "type": "message",
                "channel": {
                  "type": "websocket",
                  "initiatingRequestPattern": {
                    "url": "/source",
                    "method": "ANY"
                  }
                },
                "message": {
                  "body": {
                    "equalTo": "broadcast"
                  }
                }
              },
              "actions": [
                {
                  "type": "send",
                  "message": {
                    "body": "broadcasted"
                  },
                  "channelTarget": {
                    "type": "request-initiated",
                    "requestPattern": {
                      "url": "/target",
                      "method": "ANY"
                    }
                  }
                }
              ]
            }
            """));
  }

  @Test
  void messageStubMappingWithBroadcastActionDeserializesCorrectly() {
    String json =
        // language=JSON
        """
        {
          "name": "Broadcast deserialized",
          "trigger": {
            "channel": {
              "type": "websocket",
              "initiatingRequestPattern": {
                "url": "/source"
              }
            },
            "message": {
              "body": {
                "equalTo": "trigger"
              }
            }
          },
          "actions": [
            {
              "type": "send",
              "message": {
                "body": "broadcast-message"
              },
              "channelTarget": {
                "type": "request-initiated",
                "requestPattern": {
                  "url": "/target-channel"
                }
              }
            }
          ]
        }
        """;

    MessageStubMapping stub = Json.read(json, MessageStubMapping.class);

    assertThat(stub.getName(), is("Broadcast deserialized"));
    assertThat(stub.getActions().size(), is(1));

    SendMessageAction action = (SendMessageAction) stub.getActions().get(0);
    assertThat(action.getBody().getData(), is("broadcast-message"));
    ChannelTarget target = action.getChannelTarget();
    assertThat(target, instanceOf(RequestInitiatedChannelTarget.class));
    RequestInitiatedChannelTarget requestTarget = (RequestInitiatedChannelTarget) target;
    assertThat(requestTarget.getRequestPattern().getUrl(), is("/target-channel"));
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
            // language=JSON
            """
            {
              "id": "22222222-3333-4444-5555-666666666666",
              "eventType": "sent",
              "channelType": "websocket",
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
          "eventType": "sent",
          "channelType": "websocket",
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

  // TextEntityDefinition serialisation tests

  @Test
  void textEntityDefinitionWithStringDataSerializesToJson() {
    TextEntityDefinition entityDef =
        new TextEntityDefinition(
            FormatType.JSON, CompressionType.NONE, null, null, "hello world", null);

    String json = Json.write(entityDef);

    // Default values (format=json, compression=none) should not be serialized
    assertThat(
        json,
        jsonEquals(
            // language=JSON
            """
            {
              "data": "hello world"
            }
            """));
  }

  @Test
  void textEntityDefinitionWithObjectDataSerializesToJson() {
    Map<String, Object> objectData = Map.of("name", "John", "age", 30);
    TextEntityDefinition entityDef =
        new TextEntityDefinition(
            FormatType.JSON, CompressionType.NONE, null, null, objectData, null);

    String json = Json.write(entityDef);

    // Default values (format=json, compression=none) should not be serialized
    assertThat(
        json,
        jsonEquals(
            // language=JSON
            """
            {
              "data": {
                "name": "John",
                "age": 30
              }
            }
            """));
  }

  @Test
  void textEntityDefinitionWithDataStoreRefSerializesToJson() {
    // Using non-default format (TEXT instead of JSON) to test that it is serialized
    TextEntityDefinition entityDef =
        new TextEntityDefinition(
            FormatType.TEXT, CompressionType.NONE, "myStore", "myKey", null, null);

    String json = Json.write(entityDef);

    // compression=none is default, so not serialized
    // format=text is NOT the default (json is), so it IS serialized
    assertThat(
        json,
        jsonEquals(
            // language=JSON
            """
            {
              "format": "text",
              "dataStore": "myStore",
              "dataRef": "myKey"
            }
            """));
  }

  @Test
  void textEntityDefinitionWithDataStoreRoundTripsViaMessageStubMapping() {
    TextEntityDefinition original =
        new TextEntityDefinition(
            FormatType.JSON, CompressionType.GZIP, "myStore", "myKey", null, null);

    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Round trip stub")
            .withBody(equalTo("trigger"))
            .triggersAction(SendMessageAction.toOriginatingChannel(original))
            .build();

    String json = Json.write(stub);
    MessageStubMapping deserialized = Json.read(json, MessageStubMapping.class);

    SendMessageAction action = (SendMessageAction) deserialized.getActions().get(0);
    assertThat(action.getBody(), instanceOf(TextEntityDefinition.class));
    assertThat(action.getBody(), is(original));
  }

  @Test
  void messageStubMappingWithTextEntityDefinitionSerializesCorrectly() {
    TextEntityDefinition entityDef =
        new TextEntityDefinition(
            FormatType.JSON, CompressionType.NONE, null, null, Map.of("key", "value"), null);

    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withId(UUID.fromString("cccccccc-dddd-eeee-ffff-000000000000"))
            .withName("Text entity stub")
            .withBody(equalTo("trigger"))
            .triggersAction(SendMessageAction.toOriginatingChannel(entityDef))
            .build();

    String json = Json.write(stub);

    // Default values (format=json, compression=none) should not be serialized
    assertThat(
        json,
        jsonEquals(
            // language=JSON
            """
            {
              "id": "cccccccc-dddd-eeee-ffff-000000000000",
              "name": "Text entity stub",
              "trigger": {
                "type": "message",
                "message": {
                  "body": {
                    "equalTo": "trigger"
                  }
                }
              },
              "actions": [
                {
                  "type": "send",
                  "message": {
                    "body": {
                      "data": {
                        "key": "value"
                      }
                    }
                  },
                  "channelTarget": {
                    "type": "originating"
                  }
                }
              ]
            }
            """));
  }

  @Test
  void messageStubMappingWithTextEntityDefinitionDeserializesCorrectly() {
    String json =
        // language=JSON
        """
        {
          "name": "Text entity deserialized",
          "trigger": {
            "message": {
              "body": {
                "equalTo": "trigger"
              }
            }
          },
          "actions": [
            {
              "type": "send",
              "message": {
                "body": {
                  "dataStore": "testStore",
                  "dataRef": "testKey"
                }
              },
              "channelTarget": {
                "type": "originating"
              }
            }
          ]
        }
        """;

    MessageStubMapping stub = Json.read(json, MessageStubMapping.class);

    assertThat(stub.getName(), is("Text entity deserialized"));
    assertThat(stub.getActions().size(), is(1));

    SendMessageAction action = (SendMessageAction) stub.getActions().get(0);
    assertThat(action.getBody(), is(notNullValue()));
    assertThat(action.getBody() instanceof TextEntityDefinition, is(true));

    TextEntityDefinition entityDef = (TextEntityDefinition) action.getBody();
    assertThat(entityDef.getDataStore(), is("testStore"));
    assertThat(entityDef.getDataRef(), is("testKey"));
  }

  // BinaryEntityDefinition serialisation tests

  @Test
  void messageStubMappingWithBinaryMatchingAndBinaryResponseSerializesCorrectly() {
    byte[] matchBytes = new byte[] {0x01, 0x02, 0x03};
    byte[] responseBytes = new byte[] {0x0A, 0x0B, 0x0C, 0x0D};

    String matchBase64 = java.util.Base64.getEncoder().encodeToString(matchBytes);
    String responseBase64 = java.util.Base64.getEncoder().encodeToString(responseBytes);

    BinaryEntityDefinition binaryBody =
        BinaryEntityDefinition.aBinaryMessage().withBody(responseBytes).build();

    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))
            .withName("Binary stub")
            .withBody(binaryEqualTo(matchBytes))
            .triggersAction(SendMessageAction.toOriginatingChannel(binaryBody))
            .build();

    String json = Json.write(stub);

    assertThat(
        json,
        jsonEquals(
            // language=JSON
            """
            {
              "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
              "name": "Binary stub",
              "trigger": {
                "type": "message",
                "message": {
                  "body": {
                    "binaryEqualTo": "%s"
                  }
                }
              },
              "actions": [
                {
                  "type": "send",
                  "message": {
                    "body": {
                      "encoding": "binary",
                      "data": "%s"
                    }
                  },
                  "channelTarget": {
                    "type": "originating"
                  }
                }
              ]
            }
            """
                .formatted(matchBase64, responseBase64)));
  }

  @Test
  void messageStubMappingWithBinaryMatchingAndBinaryResponseDeserializesCorrectly() {
    byte[] matchBytes = new byte[] {0x01, 0x02, 0x03};
    byte[] responseBytes = new byte[] {0x0A, 0x0B, 0x0C, 0x0D};

    String matchBase64 = java.util.Base64.getEncoder().encodeToString(matchBytes);
    String responseBase64 = java.util.Base64.getEncoder().encodeToString(responseBytes);

    String json =
        // language=JSON
        """
        {
          "name": "Binary deserialized",
          "trigger": {
            "message": {
              "body": {
                "binaryEqualTo": "%s"
              }
            }
          },
          "actions": [
            {
              "type": "send",
              "message": {
                "body": {
                  "encoding": "binary",
                  "data": "%s"
                }
              },
              "channelTarget": {
                "type": "originating"
              }
            }
          ]
        }
        """
            .formatted(matchBase64, responseBase64);

    MessageStubMapping stub = Json.read(json, MessageStubMapping.class);

    assertThat(stub.getName(), is("Binary deserialized"));
    assertThat(stub.getActions().size(), is(1));

    SendMessageAction action = (SendMessageAction) stub.getActions().get(0);
    assertThat(action.getBody(), is(notNullValue()));
    assertThat(action.getBody() instanceof BinaryEntityDefinition, is(true));

    BinaryEntityDefinition entityDef = (BinaryEntityDefinition) action.getBody();
    assertThat(entityDef.getData(), is(responseBase64));
    assertThat(entityDef.getDataAsBytes(), is(responseBytes));
  }

  @Test
  void binaryEntityDefinitionSerializesToJson() {
    byte[] data = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05};
    String base64Data = java.util.Base64.getEncoder().encodeToString(data);

    BinaryEntityDefinition entityDef =
        BinaryEntityDefinition.aBinaryMessage().withBody(data).build();

    String json = Json.write(entityDef);

    assertThat(
        json,
        jsonEquals(
            // language=JSON
            """
            {
              "encoding": "binary",
              "data": "%s"
            }
            """
                .formatted(base64Data)));
  }

  @Test
  void binaryEntityDefinitionRoundTripsViaMessageStubMapping() {
    byte[] data = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05};

    BinaryEntityDefinition original =
        BinaryEntityDefinition.aBinaryMessage().withBody(data).build();

    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Binary round trip stub")
            .withBody(equalTo("trigger"))
            .triggersAction(SendMessageAction.toOriginatingChannel(original))
            .build();

    String json = Json.write(stub);
    MessageStubMapping deserialized = Json.read(json, MessageStubMapping.class);

    SendMessageAction action = (SendMessageAction) deserialized.getActions().get(0);
    assertThat(action.getBody(), instanceOf(BinaryEntityDefinition.class));
    assertThat(action.getBody(), is(original));
  }

  // HttpStubTrigger serialization tests

  @Test
  void messageStubMappingWithHttpStubTriggerSerializesToJson() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))
            .withName("HTTP stub triggered message")
            .triggeredByHttpStub("11111111-2222-3333-4444-555555555555")
            .triggersAction(
                SendMessageAction.toMatchingChannels(
                    "triggered message", newRequestPattern().withUrl("/ws-channel").build()))
            .build();

    String json = Json.write(stub);

    assertThat(
        json,
        jsonEquals(
            // language=JSON
            """
            {
              "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
              "name": "HTTP stub triggered message",
              "trigger": {
                "type": "http-stub",
                "stubId": "11111111-2222-3333-4444-555555555555"
              },
              "actions": [
                {
                  "type": "send",
                  "message": {
                    "body": "triggered message"
                  },
                  "channelTarget": {
                    "type": "request-initiated",
                    "requestPattern": {
                      "url": "/ws-channel",
                      "method": "ANY"
                    }
                  }
                }
              ]
            }
            """));
  }

  @Test
  void messageStubMappingWithHttpStubTriggerDeserializesFromJson() {
    String json =
        // language=JSON
        """
        {
          "id": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff",
          "name": "Deserialized HTTP stub trigger",
          "trigger": {
            "type": "http-stub",
            "stubId": "22222222-3333-4444-5555-666666666666"
          },
          "actions": [
            {
              "type": "send",
              "message": {
                "body": "response"
              },
              "channelTarget": {
                "type": "request-initiated",
                "requestPattern": {
                  "url": "/target"
                }
              }
            }
          ]
        }
        """;

    MessageStubMapping stub = Json.read(json, MessageStubMapping.class);

    assertThat(stub.getId().toString(), is("bbbbbbbb-cccc-dddd-eeee-ffffffffffff"));
    assertThat(stub.getName(), is("Deserialized HTTP stub trigger"));
    assertThat(stub.getTrigger(), instanceOf(HttpStubTrigger.class));
    HttpStubTrigger trigger = (HttpStubTrigger) stub.getTrigger();
    assertThat(trigger.getStubId().toString(), is("22222222-3333-4444-5555-666666666666"));
    assertThat(stub.getActions().size(), is(1));
  }

  @Test
  void messageStubMappingWithHttpStubTriggerRoundTrips() {
    MessageStubMapping original =
        MessageStubMapping.builder()
            .withName("Round trip HTTP stub trigger")
            .triggeredByHttpStub(UUID.fromString("33333333-4444-5555-6666-777777777777"))
            .triggersAction(
                SendMessageAction.toMatchingChannels(
                    "round trip message", newRequestPattern().withUrl("/round-trip").build()))
            .build();

    String json = Json.write(original);
    MessageStubMapping deserialized = Json.read(json, MessageStubMapping.class);

    assertThat(deserialized.getId(), is(original.getId()));
    assertThat(deserialized.getName(), is(original.getName()));
    assertThat(deserialized.getTrigger(), instanceOf(HttpStubTrigger.class));
    HttpStubTrigger trigger = (HttpStubTrigger) deserialized.getTrigger();
    assertThat(trigger.getStubId().toString(), is("33333333-4444-5555-6666-777777777777"));
  }

  // HttpRequestTrigger serialization tests

  @Test
  void messageStubMappingWithHttpRequestTriggerSerializesToJson() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withId(UUID.fromString("cccccccc-dddd-eeee-ffff-000000000000"))
            .withName("HTTP request triggered message")
            .triggeredByHttpRequest(newRequestPattern().withUrl("/api/trigger").build())
            .triggersAction(
                SendMessageAction.toMatchingChannels(
                    "request triggered", newRequestPattern().withUrl("/ws-notify").build()))
            .build();

    String json = Json.write(stub);

    assertThat(
        json,
        jsonEquals(
            // language=JSON
            """
            {
              "id": "cccccccc-dddd-eeee-ffff-000000000000",
              "name": "HTTP request triggered message",
              "trigger": {
                "type": "http-request",
                "requestPattern": {
                  "url": "/api/trigger",
                  "method": "ANY"
                }
              },
              "actions": [
                {
                  "type": "send",
                  "message": {
                    "body": "request triggered"
                  },
                  "channelTarget": {
                    "type": "request-initiated",
                    "requestPattern": {
                      "url": "/ws-notify",
                      "method": "ANY"
                    }
                  }
                }
              ]
            }
            """));
  }

  @Test
  void messageStubMappingWithHttpRequestTriggerDeserializesFromJson() {
    String json =
        // language=JSON
        """
        {
          "id": "dddddddd-eeee-ffff-0000-111111111111",
          "name": "Deserialized HTTP request trigger",
          "trigger": {
            "type": "http-request",
            "requestPattern": {
              "url": "/api/events",
              "method": "POST"
            }
          },
          "actions": [
            {
              "type": "send",
              "message": {
                "body": "event received"
              },
              "channelTarget": {
                "type": "request-initiated",
                "requestPattern": {
                  "url": "/events-channel"
                }
              }
            }
          ]
        }
        """;

    MessageStubMapping stub = Json.read(json, MessageStubMapping.class);

    assertThat(stub.getId().toString(), is("dddddddd-eeee-ffff-0000-111111111111"));
    assertThat(stub.getName(), is("Deserialized HTTP request trigger"));
    assertThat(stub.getTrigger(), instanceOf(HttpRequestTrigger.class));
    HttpRequestTrigger trigger = (HttpRequestTrigger) stub.getTrigger();
    assertThat(trigger.getRequestPattern().getUrl(), is("/api/events"));
    assertThat(stub.getActions().size(), is(1));
  }

  @Test
  void messageStubMappingWithHttpRequestTriggerRoundTrips() {
    MessageStubMapping original =
        MessageStubMapping.builder()
            .withName("Round trip HTTP request trigger")
            .triggeredByHttpRequest(newRequestPattern().withUrl("/api/round-trip").build())
            .triggersAction(
                SendMessageAction.toMatchingChannels(
                    "round trip", newRequestPattern().withUrl("/ws-round-trip").build()))
            .build();

    String json = Json.write(original);
    MessageStubMapping deserialized = Json.read(json, MessageStubMapping.class);

    assertThat(deserialized.getId(), is(original.getId()));
    assertThat(deserialized.getName(), is(original.getName()));
    assertThat(deserialized.getTrigger(), instanceOf(HttpRequestTrigger.class));
    HttpRequestTrigger trigger = (HttpRequestTrigger) deserialized.getTrigger();
    assertThat(trigger.getRequestPattern().getUrl(), is("/api/round-trip"));
  }

  // RequestInitiatedChannelTarget serialization tests

  @Test
  void requestInitiatedChannelTargetWithChannelTypeSerializesToJson() {
    RequestInitiatedChannelTarget target =
        RequestInitiatedChannelTarget.forTypeAndPattern(
            ChannelType.WEBSOCKET, newRequestPattern().withUrl("/ws-target").build());

    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withId(UUID.fromString("eeeeeeee-ffff-0000-1111-222222222222"))
            .withName("Request-initiated with channel type")
            .withBody(equalTo("trigger"))
            .triggersAction(
                new SendMessageAction(
                    MessageDefinition.fromString("targeted message"), target, null, null))
            .build();

    String json = Json.write(stub);

    assertThat(
        json,
        jsonEquals(
            // language=JSON
            """
            {
              "id": "eeeeeeee-ffff-0000-1111-222222222222",
              "name": "Request-initiated with channel type",
              "trigger": {
                "type": "message",
                "message": {
                  "body": {
                    "equalTo": "trigger"
                  }
                }
              },
              "actions": [
                {
                  "type": "send",
                  "message": {
                    "body": "targeted message"
                  },
                  "channelTarget": {
                    "type": "request-initiated",
                    "channelType": "websocket",
                    "requestPattern": {
                      "url": "/ws-target",
                      "method": "ANY"
                    }
                  }
                }
              ]
            }
            """));
  }

  @Test
  void requestInitiatedChannelTargetWithChannelTypeDeserializesFromJson() {
    String json =
        // language=JSON
        """
        {
          "name": "Deserialized request-initiated with channel type",
          "trigger": {
            "message": {
              "body": {
                "equalTo": "trigger"
              }
            }
          },
          "actions": [
            {
              "type": "send",
              "message": {
                "body": "response"
              },
              "channelTarget": {
                "type": "request-initiated",
                "channelType": "websocket",
                "requestPattern": {
                  "url": "/ws-channel"
                }
              }
            }
          ]
        }
        """;

    MessageStubMapping stub = Json.read(json, MessageStubMapping.class);

    assertThat(stub.getName(), is("Deserialized request-initiated with channel type"));
    assertThat(stub.getActions().size(), is(1));

    SendMessageAction action = (SendMessageAction) stub.getActions().get(0);
    ChannelTarget target = action.getChannelTarget();
    assertThat(target, instanceOf(RequestInitiatedChannelTarget.class));

    RequestInitiatedChannelTarget requestTarget = (RequestInitiatedChannelTarget) target;
    assertThat(requestTarget.getChannelType(), is(ChannelType.WEBSOCKET));
    assertThat(requestTarget.getRequestPattern().getUrl(), is("/ws-channel"));
  }

  @Test
  void requestInitiatedChannelTargetWithoutChannelTypeDeserializesFromJson() {
    String json =
        // language=JSON
        """
        {
          "name": "Request-initiated without channel type",
          "trigger": {
            "message": {
              "body": {
                "equalTo": "trigger"
              }
            }
          },
          "actions": [
            {
              "type": "send",
              "message": {
                "body": "response"
              },
              "channelTarget": {
                "type": "request-initiated",
                "requestPattern": {
                  "urlPath": "/target-path"
                }
              }
            }
          ]
        }
        """;

    MessageStubMapping stub = Json.read(json, MessageStubMapping.class);

    SendMessageAction action = (SendMessageAction) stub.getActions().get(0);
    RequestInitiatedChannelTarget requestTarget =
        (RequestInitiatedChannelTarget) action.getChannelTarget();
    assertThat(requestTarget.getChannelType(), is((ChannelType) null));
    assertThat(requestTarget.getRequestPattern().getUrlPath(), is("/target-path"));
  }

  @Test
  void requestInitiatedChannelTargetRoundTrips() {
    RequestInitiatedChannelTarget original =
        RequestInitiatedChannelTarget.forTypeAndPattern(
            ChannelType.WEBSOCKET, newRequestPattern().withUrl("/round-trip-target").build());

    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Round trip request-initiated")
            .withBody(equalTo("trigger"))
            .triggersAction(
                new SendMessageAction(
                    MessageDefinition.fromString("message"), original, null, null))
            .build();

    String json = Json.write(stub);
    MessageStubMapping deserialized = Json.read(json, MessageStubMapping.class);

    SendMessageAction action = (SendMessageAction) deserialized.getActions().get(0);
    RequestInitiatedChannelTarget deserializedTarget =
        (RequestInitiatedChannelTarget) action.getChannelTarget();

    assertThat(deserializedTarget.getChannelType(), is(ChannelType.WEBSOCKET));
    assertThat(deserializedTarget.getRequestPattern().getUrl(), is("/round-trip-target"));
  }
}
