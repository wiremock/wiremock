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
import com.github.tomakehurst.wiremock.websocket.MessageChannel;
import com.github.tomakehurst.wiremock.websocket.MessageChannels;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;

/**
 * WebSocket endpoint that accepts all WebSocket connections and registers them with the
 * MessageChannels.
 */
public class WireMockWebSocketEndpoint implements Session.Listener.AutoDemanding {

  private final MessageChannels messageChannels;
  private final Request upgradeRequest;
  private MessageChannel messageChannel;
  private Session session;

  public WireMockWebSocketEndpoint(MessageChannels messageChannels, Request upgradeRequest) {
    this.messageChannels = messageChannels;
    this.upgradeRequest = upgradeRequest;
  }

  @Override
  public void onWebSocketOpen(Session session) {
    this.session = session;
    JettyWebSocketSession webSocketSession = new JettyWebSocketSession(session);
    this.messageChannel = new MessageChannel(upgradeRequest, webSocketSession);
    messageChannels.add(messageChannel);
  }

  @Override
  public void onWebSocketText(String message) {
    // For now, we don't process incoming messages
    // This could be extended in the future to support request/response patterns
  }

  @Override
  public void onWebSocketBinary(java.nio.ByteBuffer payload, Callback callback) {
    // For now, we don't process incoming binary messages
    callback.succeed();
  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    if (messageChannel != null) {
      messageChannels.remove(messageChannel.getId());
    }
  }

  @Override
  public void onWebSocketError(Throwable cause) {
    // Log the error but keep the channel registered
    // The channel will be cleaned up when it's closed
  }

  public MessageChannel getMessageChannel() {
    return messageChannel;
  }
}

