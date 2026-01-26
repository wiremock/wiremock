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
package com.github.tomakehurst.wiremock.message;

import static java.util.Base64.*;

import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.common.entity.BinaryEntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.CompressionType;
import com.github.tomakehurst.wiremock.common.entity.EncodingType;
import com.github.tomakehurst.wiremock.common.entity.Entity;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.FormatType;
import com.github.tomakehurst.wiremock.common.entity.StringEntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.TextEntityDefinition;
import com.github.tomakehurst.wiremock.extension.MessageActionTransformer;
import com.github.tomakehurst.wiremock.store.BlobStore;
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
  private final List<MessageActionTransformer> actionTransformers;

  public MessageStubRequestHandler(
      MessageStubMappings messageStubMappings,
      MessageChannels messageChannels,
      MessageJournal messageJournal,
      Stores stores,
      List<MessageActionTransformer> actionTransformers) {
    this.messageStubMappings = messageStubMappings;
    this.messageChannels = messageChannels;
    this.messageJournal = messageJournal;
    this.stores = stores;
    this.actionTransformers =
        actionTransformers != null ? actionTransformers : Collections.emptyList();
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
    MessageActionContext context =
        MessageActionContext.forIncomingMessage(stub, originatingChannel, incomingMessage);
    for (MessageAction action : stub.getActions()) {
      MessageAction transformedAction = applyTransformations(action, context);
      executeAction(transformedAction, originatingChannel, incomingMessage);
    }
  }

  private MessageAction applyTransformations(MessageAction action, MessageActionContext context) {
    MessageAction result = action;
    for (MessageActionTransformer transformer : actionTransformers) {
      if (transformer.applyGlobally() || action.hasTransformer(transformer)) {
        result = transformer.transform(result, context);
      }
    }
    return result;
  }

  private void executeAction(
      MessageAction action, MessageChannel originatingChannel, Message incomingMessage) {
    if (action instanceof SendMessageAction sendAction) {
      executeSendMessageAction(sendAction, originatingChannel);
    }
  }

  private void executeSendMessageAction(
      SendMessageAction action, MessageChannel originatingChannel) {
    Message message = resolveToMessage(action.getMessage());
    ChannelTarget target = action.getChannelTarget();

    if (target instanceof OriginatingChannelTarget) {
      originatingChannel.sendMessage(message);
    } else if (target instanceof RequestInitiatedChannelTarget requestTarget) {
      List<RequestInitiatedMessageChannel> matchingChannels;
      if (requestTarget.getChannelType() != null) {
        matchingChannels =
            messageChannels.findByTypeAndRequestPattern(
                requestTarget.getChannelType(),
                requestTarget.getRequestPattern(),
                Collections.emptyMap());
      } else {
        matchingChannels =
            messageChannels.findByRequestPattern(
                requestTarget.getRequestPattern(), Collections.emptyMap());
      }
      for (RequestInitiatedMessageChannel channel : matchingChannels) {
        channel.sendMessage(message);
      }
    }
  }

  public Message resolveToMessage(MessageDefinition messageDefinition) {
    return resolveToMessage(messageDefinition, stores);
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

    if (definition instanceof BinaryEntityDefinition binaryDef) {
      byte[] bytes = resolveBinaryEntityData(binaryDef, stores);
      InputStreamSource streamSource = () -> new ByteArrayInputStream(bytes);
      return new Entity(
          EncodingType.BINARY, FormatType.BASE64, binaryDef.getCompression(), streamSource);
    }

    if (definition instanceof TextEntityDefinition textDef) {
      String resolvedData = resolveTextEntityData(textDef, stores);
      byte[] bytes =
          resolvedData != null ? resolvedData.getBytes(StandardCharsets.UTF_8) : new byte[0];
      InputStreamSource streamSource = () -> new ByteArrayInputStream(bytes);
      return new Entity(
          EncodingType.TEXT, textDef.getFormat(), textDef.getCompression(), streamSource);
    }

    throw new UnsupportedOperationException(
        "Resolution of " + definition.getClass().getSimpleName() + " is not yet supported");
  }

  private static String resolveTextEntityData(TextEntityDefinition definition, Stores stores) {
    Object data = definition.getData();

    if (data instanceof String s) {
      return s;
    }

    if (data != null) {
      return Json.write(data);
    }

    String filePath = definition.getFilePath();
    if (filePath != null && stores != null) {
      BlobStore filesBlobStore = stores.getFilesBlobStore();
      return filesBlobStore.get(filePath).map(Strings::stringFromBytes).orElse(null);
    }

    String dataStore = definition.getDataStore();
    String dataRef = definition.getDataRef();
    if (dataStore != null && dataRef != null && stores != null) {
      return stores
          .getObjectStore(dataStore)
          .get(dataRef)
          .map(
              value -> {
                if (value instanceof String s) {
                  return s;
                }
                return Json.write(value);
              })
          .orElse(null);
    }

    return null;
  }

  private static byte[] resolveBinaryEntityData(BinaryEntityDefinition definition, Stores stores) {
    byte[] data = definition.getDataAsBytes();
    if (data != null) {
      return data;
    }

    String filePath = definition.getFilePath();
    if (filePath != null && stores != null) {
      BlobStore filesBlobStore = stores.getFilesBlobStore();
      return filesBlobStore.get(filePath).orElse(new byte[0]);
    }

    String dataStore = definition.getDataStore();
    String dataRef = definition.getDataRef();
    if (dataStore != null && dataRef != null && stores != null) {
      return stores
          .getObjectStore(dataStore)
          .get(dataRef)
          .map(
              value -> {
                if (value instanceof byte[] bytes) {
                  return bytes;
                }
                if (value instanceof String s) {
                  return getDecoder().decode(s);
                }
                return new byte[0];
              })
          .orElse(new byte[0]);
    }

    return new byte[0];
  }

  public MessageStubMappings getMessageStubMappings() {
    return messageStubMappings;
  }

  public MessageChannels getMessageChannels() {
    return messageChannels;
  }
}
