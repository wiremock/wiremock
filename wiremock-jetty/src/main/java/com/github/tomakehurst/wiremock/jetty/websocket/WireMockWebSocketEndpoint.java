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
package com.github.tomakehurst.wiremock.jetty.websocket;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.message.Message;
import com.github.tomakehurst.wiremock.message.MessageChannel;
import com.github.tomakehurst.wiremock.message.MessageDefinition;
import com.github.tomakehurst.wiremock.message.MessageStubRequestHandler;
import com.github.tomakehurst.wiremock.message.websocket.WebSocketMessageChannel;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;

public class WireMockWebSocketEndpoint implements Session.Listener.AutoDemanding {

  private final MessageStubRequestHandler messageStubRequestHandler;
  private final Request upgradeRequest;
  private MessageChannel messageChannel;
  private Session session;

  public WireMockWebSocketEndpoint(
      MessageStubRequestHandler messageStubRequestHandler, Request upgradeRequest) {
    this.messageStubRequestHandler = messageStubRequestHandler;
    this.upgradeRequest = upgradeRequest;
  }

  @Override
  public void onWebSocketOpen(Session session) {
    this.session = session;
    JettyWebSocketSession webSocketSession = new JettyWebSocketSession(session);
    this.messageChannel = new WebSocketMessageChannel(upgradeRequest, webSocketSession);
    messageStubRequestHandler.getMessageChannels().add(messageChannel);
  }

  @Override
  public void onWebSocketText(String text) {
    if (messageStubRequestHandler != null && messageChannel != null) {
      Message message =
          MessageStubRequestHandler.resolveToMessage(MessageDefinition.fromString(text), null);
      messageStubRequestHandler.processMessage(messageChannel, message);
    }
  }

  @Override
  public void onWebSocketBinary(java.nio.ByteBuffer payload, Callback callback) {
    if (messageStubRequestHandler != null && messageChannel != null) {
      byte[] data = new byte[payload.remaining()];
      payload.get(data);
      Message message =
          MessageStubRequestHandler.resolveToMessage(MessageDefinition.fromBytes(data), null);
      messageStubRequestHandler.processMessage(messageChannel, message);
    }
    callback.succeed();
  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    if (messageChannel != null) {
      messageStubRequestHandler.getMessageChannels().remove(messageChannel.getId());
    }
  }

  @Override
  public void onWebSocketError(Throwable cause) {}

  public MessageChannel getMessageChannel() {
    return messageChannel;
  }
}
