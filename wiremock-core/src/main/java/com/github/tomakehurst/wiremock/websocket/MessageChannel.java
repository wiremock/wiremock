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
package com.github.tomakehurst.wiremock.websocket;

import com.github.tomakehurst.wiremock.http.Request;
import java.util.UUID;

/**
 * Represents a WebSocket message channel, storing the HTTP request used to initiate the WebSocket
 * session and a reference to the session itself.
 */
public class MessageChannel {

  private final UUID id;
  private final Request request;
  private final WebSocketSession session;

  public MessageChannel(UUID id, Request request, WebSocketSession session) {
    this.id = id;
    this.request = request;
    this.session = session;
  }

  public MessageChannel(Request request, WebSocketSession session) {
    this(UUID.randomUUID(), request, session);
  }

  public UUID getId() {
    return id;
  }

  public Request getRequest() {
    return request;
  }

  public WebSocketSession getSession() {
    return session;
  }

  public boolean isOpen() {
    return session != null && session.isOpen();
  }

  public void sendMessage(String message) {
    if (session != null && session.isOpen()) {
      session.sendMessage(message);
    }
  }

  public void close() {
    if (session != null && session.isOpen()) {
      session.close();
    }
  }
}

