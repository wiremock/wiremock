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
package com.github.tomakehurst.wiremock.message;

import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.entity.CompressionType;
import com.github.tomakehurst.wiremock.common.entity.EncodingType;
import com.github.tomakehurst.wiremock.common.entity.Entity;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.FormatType;
import com.github.tomakehurst.wiremock.common.entity.FullEntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.StringEntityDefinition;
import com.github.tomakehurst.wiremock.store.Stores;
import com.github.tomakehurst.wiremock.verification.MessageJournal;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MessageStubRequestHandler {

  private final MessageStubMappings messageStubMappings;
  private final MessageChannels messageChannels;
  private final MessageJournal messageJournal;
  private final Stores stores;

  public MessageStubRequestHandler(
      MessageStubMappings messageStubMappings,
      MessageChannels messageChannels,
      MessageJournal messageJournal,
      Stores stores) {
    this.messageStubMappings = messageStubMappings;
    this.messageChannels = messageChannels;
    this.messageJournal = messageJournal;
    this.stores = stores;
  }

  public void processMessage(MessageChannel channel, Message message) {
    Optional<MessageStubMapping> matchingStub =
        messageStubMappings.findMatchingStub(channel, message);
    if (matchingStub.isPresent()) {
      MessageStubMapping stub = matchingStub.get();
      executeActions(stub, channel, message);

      MessageServeEvent event = MessageServeEvent.receivedMatched(channel, message, stub);
      messageJournal.messageReceived(event);
    } else {
      MessageServeEvent event = MessageServeEvent.receivedUnmatched(channel, message);
      messageJournal.messageReceived(event);
    }
  }

  private void executeActions(
      MessageStubMapping stub, MessageChannel originatingChannel, Message incomingMessage) {
    for (MessageAction action : stub.getActions()) {
      executeAction(action, originatingChannel, incomingMessage);
    }
  }

  private void executeAction(
      MessageAction action, MessageChannel originatingChannel, Message incomingMessage) {
    if (action instanceof SendMessageAction) {
      executeSendMessageAction((SendMessageAction) action, originatingChannel);
    }
  }

  private void executeSendMessageAction(
      SendMessageAction action, MessageChannel originatingChannel) {
    MessageDefinition messageDefinition = new MessageDefinition(action.getBody());
    Message message = resolveToMessage(messageDefinition);
    if (action.isSendToOriginatingChannel()) {
      originatingChannel.sendMessage(message);
    } else if (action.getTargetChannelPattern() != null) {
      List<MessageChannel> matchingChannels =
          messageChannels.findByRequestPattern(
              action.getTargetChannelPattern(), Collections.emptyMap());
      for (MessageChannel channel : matchingChannels) {
        channel.sendMessage(message);
      }
    }
  }

  public Message resolveMessageDefinition(MessageDefinition messageDefinition) {
    return resolveToMessage(messageDefinition);
  }

  public Message resolveToMessage(MessageDefinition messageDefinition) {
    Entity entity = resolveEntity(messageDefinition.getBody(), stores);
    return new Message(entity);
  }

  public static Message resolveToMessage(MessageDefinition messageDefinition, Stores stores) {
    Entity entity = resolveEntity(messageDefinition.getBody(), stores);
    return new Message(entity);
  }

  private static Entity resolveEntity(EntityDefinition definition, Stores stores) {
    if (definition instanceof StringEntityDefinition) {
      String value = ((StringEntityDefinition) definition).getValue();
      byte[] bytes = value != null ? value.getBytes(StandardCharsets.UTF_8) : new byte[0];
      InputStreamSource streamSource = () -> new ByteArrayInputStream(bytes);
      return new Entity(EncodingType.TEXT, FormatType.TEXT, CompressionType.NONE, streamSource);
    }

    if (definition instanceof FullEntityDefinition) {
      FullEntityDefinition fullDef = (FullEntityDefinition) definition;
      String resolvedData = resolveFullEntityData(fullDef, stores);
      byte[] bytes =
          resolvedData != null ? resolvedData.getBytes(StandardCharsets.UTF_8) : new byte[0];
      InputStreamSource streamSource = () -> new ByteArrayInputStream(bytes);
      return new Entity(
          fullDef.getEncoding(), fullDef.getFormat(), fullDef.getCompression(), streamSource);
    }

    throw new UnsupportedOperationException(
        "Resolution of " + definition.getClass().getSimpleName() + " is not yet supported");
  }

  private static String resolveFullEntityData(FullEntityDefinition definition, Stores stores) {
    Object data = definition.getData();

    if (data instanceof String) {
      return (String) data;
    }

    if (data != null) {
      return Json.write(data);
    }

    String dataStore = definition.getDataStore();
    String dataRef = definition.getDataRef();
    if (dataStore != null && dataRef != null && stores != null) {
      return stores
          .getObjectStore(dataStore)
          .get(dataRef)
          .map(
              value -> {
                if (value instanceof String) {
                  return (String) value;
                }
                return Json.write(value);
              })
          .orElse(null);
    }

    return null;
  }

  public MessageStubMappings getMessageStubMappings() {
    return messageStubMappings;
  }

  public MessageChannels getMessageChannels() {
    return messageChannels;
  }
}
