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
 * Represents a message channel, storing the HTTP request used to initiate the channel and providing
 * methods to send messages and manage the channel lifecycle. Different implementations support
 * different channel types (e.g., WebSocket, Server-Sent Events).
 */
public interface MessageChannel {

  /** Returns the type of this channel. */
  ChannelType getType();

  /** Returns the unique identifier for this channel. */
  UUID getId();

  /** Returns the HTTP request that was used to initiate this channel. */
  Request getRequest();

  /** Returns whether this channel is currently open. */
  boolean isOpen();

  /** Sends a message through this channel. */
  void sendMessage(String message);

  /** Closes this channel. */
  void close();
}
