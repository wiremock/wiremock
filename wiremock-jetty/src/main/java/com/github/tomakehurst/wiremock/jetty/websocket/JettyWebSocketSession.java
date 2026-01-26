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
package com.github.tomakehurst.wiremock.jetty.websocket;

import com.github.tomakehurst.wiremock.message.Message;
import com.github.tomakehurst.wiremock.message.websocket.WebSocketSession;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;

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
  public void sendMessage(Message message) {
    if (isOpen()) {
      byte[] data = message.getBody().getData();
      if (message.isBinary()) {
        session.sendBinary(ByteBuffer.wrap(data), Callback.NOOP);
      } else {
        String text = new String(data, StandardCharsets.UTF_8);
        session.sendText(text, Callback.NOOP);
      }
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
