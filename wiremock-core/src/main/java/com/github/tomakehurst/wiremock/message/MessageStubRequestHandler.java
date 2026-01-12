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

import com.github.tomakehurst.wiremock.common.entity.Entity;
import com.github.tomakehurst.wiremock.common.entity.EntityResolver;
import com.github.tomakehurst.wiremock.extension.MessageActionTransformer;
import com.github.tomakehurst.wiremock.verification.MessageJournal;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MessageStubRequestHandler {

  private final MessageStubMappings messageStubMappings;
  private final MessageChannels messageChannels;
  private final MessageJournal messageJournal;
  private final EntityResolver entityResolver;
  private final List<MessageActionTransformer> actionTransformers;

  public MessageStubRequestHandler(
      MessageStubMappings messageStubMappings,
      MessageChannels messageChannels,
      MessageJournal messageJournal,
      EntityResolver entityResolver,
      List<MessageActionTransformer> actionTransformers) {
    this.messageStubMappings = messageStubMappings;
    this.messageChannels = messageChannels;
    this.messageJournal = messageJournal;
    this.entityResolver = entityResolver;
    this.actionTransformers =
        actionTransformers != null ? actionTransformers : Collections.emptyList();
  }

  public void processTextMessage(MessageChannel channel, String text) {
    Message message = Message.builder().withTextBody(text).build();
    processMessage(channel, message);
  }

  public void processBinaryMessage(MessageChannel channel, byte[] data) {
    Message message = Message.builder().withBinaryBody(data).build();
    processMessage(channel, message);
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
    Entity entity = entityResolver.resolve(messageDefinition.getBody());
    return new Message(entity);
  }

  public MessageStubMappings getMessageStubMappings() {
    return messageStubMappings;
  }

  public MessageChannels getMessageChannels() {
    return messageChannels;
  }
}
