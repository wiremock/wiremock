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

import static com.github.tomakehurst.wiremock.extension.MessageServeEventListener.MessagePhase.AFTER_COMPLETE;
import static com.github.tomakehurst.wiremock.extension.MessageServeEventListener.MessagePhase.AFTER_MATCH;
import static com.github.tomakehurst.wiremock.extension.MessageServeEventListener.MessagePhase.BEFORE_MATCH;
import static com.github.tomakehurst.wiremock.extension.MessageServeEventListener.MessagePhase.MESSAGE_SENT;
import static com.github.tomakehurst.wiremock.extension.MessageServeEventListenerUtils.triggerListeners;

import com.github.tomakehurst.wiremock.common.entity.Entity;
import com.github.tomakehurst.wiremock.extension.MessageActionTransformer;
import com.github.tomakehurst.wiremock.extension.MessageServeEventListener;
import com.github.tomakehurst.wiremock.extension.MessageServeEventListenerDefinition;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.store.Stores;
import com.github.tomakehurst.wiremock.verification.MessageJournal;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MessageStubRequestHandler {

  private final MessageStubMappings messageStubMappings;
  private final MessageChannels messageChannels;
  private final MessageJournal messageJournal;
  private final Stores stores;
  private final List<MessageActionTransformer> actionTransformers;
  private final Map<String, RequestMatcherExtension> customMatchers;
  private final Map<String, MessageServeEventListener> serveEventListeners;

  public MessageStubRequestHandler(
      MessageStubMappings messageStubMappings,
      MessageChannels messageChannels,
      MessageJournal messageJournal,
      Stores stores,
      List<MessageActionTransformer> actionTransformers,
      Map<String, RequestMatcherExtension> customMatchers) {
    this(
        messageStubMappings,
        messageChannels,
        messageJournal,
        stores,
        actionTransformers,
        customMatchers,
        Collections.emptyMap());
  }

  public MessageStubRequestHandler(
      MessageStubMappings messageStubMappings,
      MessageChannels messageChannels,
      MessageJournal messageJournal,
      Stores stores,
      List<MessageActionTransformer> actionTransformers,
      Map<String, RequestMatcherExtension> customMatchers,
      Map<String, MessageServeEventListener> serveEventListeners) {
    this.messageStubMappings = messageStubMappings;
    this.messageChannels = messageChannels;
    this.messageJournal = messageJournal;
    this.stores = stores;
    this.actionTransformers =
        actionTransformers != null ? actionTransformers : Collections.emptyList();
    this.customMatchers = customMatchers != null ? customMatchers : Collections.emptyMap();
    this.serveEventListeners =
        serveEventListeners != null ? serveEventListeners : Collections.emptyMap();
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
    MessageServeEvent initialEvent = MessageServeEvent.receivedUnmatched(channel, message);
    triggerListeners(serveEventListeners, BEFORE_MATCH, initialEvent);

    Optional<MessageStubMapping> matchingStub =
        messageStubMappings.findMatchingStub(channel, message);
    MessageServeEvent event =
        matchingStub
            .map(
                stub ->
                    initialEvent.transform(
                        builder -> builder.withStubMapping(stub).withWasMatched(true)))
            .orElse(initialEvent);
    triggerListeners(serveEventListeners, AFTER_MATCH, event);

    matchingStub.ifPresent(stub -> executeActions(stub, channel, message));

    messageJournal.messageReceived(event);
    triggerListeners(serveEventListeners, AFTER_COMPLETE, event);
  }

  private void executeActions(
      MessageStubMapping stub, MessageChannel originatingChannel, Message incomingMessage) {
    MessageActionContext context =
        MessageActionContext.forIncomingMessage(stub, originatingChannel, incomingMessage);
    for (MessageAction action : stub.getActions()) {
      MessageAction transformedAction = applyTransformations(action, context);
      executeAction(transformedAction, originatingChannel, stub.getServeEventListeners());
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
      MessageAction action,
      MessageChannel originatingChannel,
      List<MessageServeEventListenerDefinition> listenerDefinitions) {
    if (action instanceof SendMessageAction sendAction) {
      executeSendMessageAction(sendAction, originatingChannel, listenerDefinitions);
    }
  }

  private void executeSendMessageAction(
      SendMessageAction action,
      MessageChannel originatingChannel,
      List<MessageServeEventListenerDefinition> listenerDefinitions) {
    Message message = resolveToMessage(action.getMessage());
    ChannelTarget target = action.getChannelTarget();

    if (target instanceof OriginatingChannelTarget) {
      sendAndRecord(originatingChannel, message, listenerDefinitions);
    } else if (target instanceof FixedChannelTarget fixedTarget) {
      FixedChannel outboundChannel =
          messageChannels.requireFixed(fixedTarget.getProviderName(), fixedTarget.getChannelName());
      sendAndRecord(outboundChannel, message, listenerDefinitions);
    } else if (target instanceof RequestInitiatedChannelTarget requestTarget) {
      List<RequestInitiatedMessageChannel> matchingChannels;
      if (requestTarget.getChannelType() != null) {
        matchingChannels =
            messageChannels.findByTypeAndRequestPattern(
                requestTarget.getChannelType(), requestTarget.getRequestPattern(), customMatchers);
      } else {
        matchingChannels =
            messageChannels.findByRequestPattern(requestTarget.getRequestPattern(), customMatchers);
      }
      for (RequestInitiatedMessageChannel channel : matchingChannels) {
        sendAndRecord(channel, message, listenerDefinitions);
      }
    }
  }

  private void sendAndRecord(
      MessageChannel channel,
      Message message,
      List<MessageServeEventListenerDefinition> listenerDefinitions) {
    channel.sendMessage(message);
    MessageServeEvent sentEvent = MessageServeEvent.sent(channel, message);
    messageJournal.messageReceived(sentEvent);
    triggerListeners(serveEventListeners, MESSAGE_SENT, sentEvent, listenerDefinitions);
  }

  private Message resolveToMessage(MessageDefinition messageDefinition) {
    Entity entity = messageDefinition.getBody().resolve(stores);
    return new Message(entity);
  }

  public MessageStubMappings getMessageStubMappings() {
    return messageStubMappings;
  }

  public MessageChannels getMessageChannels() {
    return messageChannels;
  }
}
