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
package com.github.tomakehurst.wiremock.message.sse;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.Message;
import com.github.tomakehurst.wiremock.message.RequestInitiatedMessageChannel;
import java.util.Base64;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class SseMessageChannel implements RequestInitiatedMessageChannel {

  public static final String EVENT_HEADER = "event";
  public static final String ID_HEADER = "id";
  public static final String RETRY_HEADER = "retry";

  private final UUID id;
  private final Request request;
  @Nullable private final SseSession session;

  public SseMessageChannel(UUID id, Request request, @Nullable SseSession session) {
    this.id = id;
    this.request = request;
    this.session = session;
  }

  public SseMessageChannel(Request request, @Nullable SseSession session) {
    this(UUID.randomUUID(), request, session);
  }

  @Override
  public ChannelType getType() {
    return ChannelType.SSE;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public Request getInitiatingRequest() {
    return request;
  }

  @Override
  public boolean isOpen() {
    return session != null && session.isOpen();
  }

  @Override
  public void sendMessage(Message message) {
    if (session != null && session.isOpen()) {
      String data;
      if (message.isBinary()) {
        data = Base64.getEncoder().encodeToString(message.getBodyAsBytes());
      } else {
        data = message.getBodyAsString();
      }
      session.sendEvent(
          wireSafeHeaderValue(message, EVENT_HEADER),
          data,
          wireSafeHeaderValue(message, ID_HEADER),
          wireSafeHeaderValue(message, RETRY_HEADER));
    }
  }

  private static @Nullable String wireSafeHeaderValue(Message message, String key) {
    String value = message.getHeaders().getFirstValue(key);
    if (value != null && (value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0)) {
      return null;
    }
    return value;
  }

  @Override
  public void close() {
    if (session != null && session.isOpen()) {
      session.close();
    }
  }
}
