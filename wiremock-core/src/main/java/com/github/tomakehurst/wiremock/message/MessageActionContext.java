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

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

public class MessageActionContext {

  private final MessageStubMapping stubMapping;
  private final MessageChannel originatingChannel;
  private final Message incomingMessage;
  private final ServeEvent httpServeEvent;

  private MessageActionContext(
      MessageStubMapping stubMapping,
      MessageChannel originatingChannel,
      Message incomingMessage,
      ServeEvent httpServeEvent) {
    this.stubMapping = stubMapping;
    this.originatingChannel = originatingChannel;
    this.incomingMessage = incomingMessage;
    this.httpServeEvent = httpServeEvent;
  }

  public static MessageActionContext forIncomingMessage(
      MessageStubMapping stubMapping, MessageChannel originatingChannel, Message incomingMessage) {
    return new MessageActionContext(stubMapping, originatingChannel, incomingMessage, null);
  }

  public static MessageActionContext forHttpTrigger(
      MessageStubMapping stubMapping, ServeEvent httpServeEvent) {
    return new MessageActionContext(stubMapping, null, null, httpServeEvent);
  }

  public MessageStubMapping getStubMapping() {
    return stubMapping;
  }

  public MessageChannel getOriginatingChannel() {
    return originatingChannel;
  }

  public Message getIncomingMessage() {
    return incomingMessage;
  }

  public ServeEvent getHttpServeEvent() {
    return httpServeEvent;
  }

  public boolean isTriggeredByMessage() {
    return originatingChannel != null;
  }

  public boolean isTriggeredByHttp() {
    return httpServeEvent != null;
  }
}
