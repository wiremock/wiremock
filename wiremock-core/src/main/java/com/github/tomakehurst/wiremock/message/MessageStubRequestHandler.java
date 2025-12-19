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
import com.github.tomakehurst.wiremock.common.entity.CompressionType;
import com.github.tomakehurst.wiremock.common.entity.EncodingType;
import com.github.tomakehurst.wiremock.common.entity.Entity;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.FormatType;
import com.github.tomakehurst.wiremock.common.entity.StringEntityDefinition;
import com.github.tomakehurst.wiremock.verification.MessageJournal;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class MessageStubRequestHandler {

  private final MessageStubMappings messageStubMappings;
  private final MessageChannels messageChannels;
  private final MessageJournal messageJournal;

  public MessageStubRequestHandler(
      MessageStubMappings messageStubMappings,
      MessageChannels messageChannels,
      MessageJournal messageJournal) {
    this.messageStubMappings = messageStubMappings;
    this.messageChannels = messageChannels;
    this.messageJournal = messageJournal;
  }

  public boolean processMessage(MessageChannel channel, String message) {
    Optional<MessageStubMapping> matchingStub =
        messageStubMappings.findMatchingStub(channel, message);
    if (matchingStub.isPresent()) {
      MessageStubMapping stub = matchingStub.get();
      stub.executeActions(channel, messageChannels, message);

      MessageServeEvent event = MessageServeEvent.receivedMatched(channel, message, stub);
      messageJournal.messageReceived(event);
      return true;
    } else {
      MessageServeEvent event = MessageServeEvent.receivedUnmatched(channel, message);
      messageJournal.messageReceived(event);
    }
    return false;
  }

  public Message resolveMessageDefinition(MessageDefinition messageDefinition) {
    return resolveToMessage(messageDefinition);
  }

  public static Message resolveToMessage(MessageDefinition messageDefinition) {
    Entity entity = resolveEntity(messageDefinition.getBody());
    return new Message(entity);
  }

  private static Entity resolveEntity(EntityDefinition definition) {
    if (definition instanceof StringEntityDefinition) {
      String value = ((StringEntityDefinition) definition).getValue();
      byte[] bytes = value != null ? value.getBytes(StandardCharsets.UTF_8) : new byte[0];
      InputStreamSource streamSource = () -> new ByteArrayInputStream(bytes);
      return new Entity(EncodingType.TEXT, FormatType.TEXT, CompressionType.NONE, streamSource);
    }
    throw new UnsupportedOperationException(
        "Resolution of " + definition.getClass().getSimpleName() + " is not yet supported");
  }

  public MessageStubMappings getMessageStubMappings() {
    return messageStubMappings;
  }

  public MessageChannels getMessageChannels() {
    return messageChannels;
  }
}
