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
package com.github.tomakehurst.wiremock.message.websocket;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.Message;
import com.github.tomakehurst.wiremock.message.RequestInitiatedMessageChannel;
import java.util.UUID;

public class WebSocketMessageChannel implements RequestInitiatedMessageChannel {

  private final UUID id;
  private final Request request;
  private final WebSocketSession session;

  public WebSocketMessageChannel(UUID id, Request request, WebSocketSession session) {
    this.id = id;
    this.request = request;
    this.session = session;
  }

  public WebSocketMessageChannel(Request request, WebSocketSession session) {
    this(UUID.randomUUID(), request, session);
  }

  @Override
  public ChannelType getType() {
    return ChannelType.WEBSOCKET;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public Request getInitiatingRequest() {
    return request;
  }

  public WebSocketSession getSession() {
    return session;
  }

  @Override
  public boolean isOpen() {
    return session != null && session.isOpen();
  }

  @Override
  public void sendMessage(Message message) {
    if (session != null && session.isOpen()) {
      session.sendMessage(message);
    }
  }

  @Override
  public void close() {
    if (session != null && session.isOpen()) {
      session.close();
    }
  }
}
