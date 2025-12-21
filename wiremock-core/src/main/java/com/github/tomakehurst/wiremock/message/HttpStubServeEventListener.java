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

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ServeEventListener;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.store.Stores;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HttpStubServeEventListener implements ServeEventListener {

  private final MessageStubMappings messageStubMappings;
  private final MessageChannels messageChannels;
  private final Stores stores;
  private final Map<String, RequestMatcherExtension> customMatchers;

  public HttpStubServeEventListener(
      MessageStubMappings messageStubMappings,
      MessageChannels messageChannels,
      Stores stores,
      Map<String, RequestMatcherExtension> customMatchers) {
    this.messageStubMappings = messageStubMappings;
    this.messageChannels = messageChannels;
    this.stores = stores;
    this.customMatchers = customMatchers != null ? customMatchers : Collections.emptyMap();
  }

  @Override
  public String getName() {
    return "http-stub-message-trigger";
  }

  @Override
  public boolean applyGlobally() {
    return true;
  }

  @Override
  public void afterMatch(ServeEvent serveEvent, Parameters parameters) {
    if (serveEvent == null || serveEvent.getStubMapping() == null) {
      return;
    }

    List<MessageStubMapping> matchingStubs = findMatchingMessageStubs(serveEvent);
    for (MessageStubMapping stub : matchingStubs) {
      executeActions(stub);
    }
  }

  private List<MessageStubMapping> findMatchingMessageStubs(ServeEvent serveEvent) {
    return messageStubMappings.getAllSortedByPriority().stream()
        .filter(stub -> matchesTrigger(stub, serveEvent))
        .toList();
  }

  private boolean matchesTrigger(MessageStubMapping stub, ServeEvent serveEvent) {
    MessageTrigger trigger = stub.getTrigger();
    if (trigger instanceof HttpStubTrigger httpStubTrigger) {
      return httpStubTrigger.matches(serveEvent);
    } else if (trigger instanceof HttpRequestTrigger httpRequestTrigger) {
      return httpRequestTrigger.matches(serveEvent, customMatchers);
    }
    return false;
  }

  private void executeActions(MessageStubMapping stub) {
    for (MessageAction action : stub.getActions()) {
      executeAction(action);
    }
  }

  private void executeAction(MessageAction action) {
    if (action instanceof SendMessageAction sendAction) {
      executeSendMessageAction(sendAction);
    }
  }

  private void executeSendMessageAction(SendMessageAction action) {
    Message message = MessageStubRequestHandler.resolveToMessage(action.getMessage(), stores);
    if (action.getTargetChannelPattern() != null) {
      List<MessageChannel> matchingChannels =
          messageChannels.findByRequestPattern(action.getTargetChannelPattern(), customMatchers);
      for (MessageChannel channel : matchingChannels) {
        channel.sendMessage(message);
      }
    }
  }
}
