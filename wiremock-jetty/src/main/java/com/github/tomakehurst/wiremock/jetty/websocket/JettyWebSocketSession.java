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

import com.github.tomakehurst.wiremock.websocket.WebSocketSession;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;

/** Jetty implementation of WebSocketSession. */
public class JettyWebSocketSession implements WebSocketSession {

  private final Session session;

  public JettyWebSocketSession(Session session) {
    this.session = session;
  }

  @Override
  public boolean isOpen() {
    return session != null && session.isOpen();
  }

  @Override
  public void sendMessage(String message) {
    if (isOpen()) {
      session.sendText(message, Callback.NOOP);
    }
  }

  @Override
  public void close() {
    if (isOpen()) {
      session.close(1000, "Closed by server", Callback.NOOP);
    }
  }

  public Session getJettySession() {
    return session;
  }
}
