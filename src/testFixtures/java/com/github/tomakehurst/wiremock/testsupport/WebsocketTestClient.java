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
package com.github.tomakehurst.wiremock.testsupport;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class WebsocketTestClient {

  private final WebSocketContainer websocketClient = ContainerProvider.getWebSocketContainer();

  private final NotificationCapturingEndpoint endpoint = new NotificationCapturingEndpoint();

  private Session persistentSession;

  public void connect(String url) {
    ClientEndpointConfig endpointConfig = ClientEndpointConfig.Builder.create().build();
    URI uri = URI.create(url);
    try {
      persistentSession = websocketClient.connectToServer(endpoint, endpointConfig, uri);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isConnected() {
    return persistentSession != null && persistentSession.isOpen();
  }

  public void sendMessage(String message) {
    if (persistentSession == null || !persistentSession.isOpen()) {
      throw new IllegalStateException("Not connected. Call connect() first.");
    }
    try {
      persistentSession.getBasicRemote().sendText(message);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void sendBinaryMessage(byte[] data) {
    if (persistentSession == null || !persistentSession.isOpen()) {
      throw new IllegalStateException("Not connected. Call connect() first.");
    }
    try {
      persistentSession.getBasicRemote().sendBinary(ByteBuffer.wrap(data));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void disconnect() {
    if (persistentSession != null && persistentSession.isOpen()) {
      try {
        persistentSession.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    persistentSession = null;
  }

  public <T> T withWebsocketSession(String url, Function<Session, T> work) {
    ClientEndpointConfig endpointConfig = ClientEndpointConfig.Builder.create().build();
    URI uri = URI.create(url);
    try (Session session = websocketClient.connectToServer(endpoint, endpointConfig, uri)) {
      Thread.sleep(100);
      return work.apply(session);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void sendMessage(String url, String message) {
    withWebsocketSession(
        url,
        session -> {
          try {
            session.getBasicRemote().sendText(message);
            Thread.sleep(100); // Give time for response
            return null;
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  public String sendMessageAndWaitForResponse(String url, String message) {
    return sendMessageAndWaitForResponse(url, message, m -> true);
  }

  public String sendMessageAndWaitForResponse(
      String url, String message, Predicate<String> responsePredicate) {
    ClientEndpointConfig endpointConfig = ClientEndpointConfig.Builder.create().build();
    URI uri = URI.create(url);
    try (Session session = websocketClient.connectToServer(endpoint, endpointConfig, uri)) {
      Thread.sleep(100);
      int messageCountBefore = endpoint.messages.size();
      session.getBasicRemote().sendText(message);
      await()
          .atMost(5, SECONDS)
          .until(
              () ->
                  endpoint.messages.size() > messageCountBefore
                      && endpoint
                          .messages
                          .subList(messageCountBefore, endpoint.messages.size())
                          .stream()
                          .anyMatch(responsePredicate));
      return endpoint.messages.subList(messageCountBefore, endpoint.messages.size()).stream()
          .filter(responsePredicate)
          .findFirst()
          .orElse(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<String> getMessages() {
    return endpoint.messages;
  }

  public void clearMessages() {
    endpoint.messages.clear();
  }

  public String waitForMessage(Predicate<String> predicate) {
    await().atMost(5, SECONDS).until(() -> endpoint.messages.stream().anyMatch(predicate));
    return endpoint.messages.stream().filter(predicate).findFirst().get();
  }

  public static class NotificationCapturingEndpoint extends Endpoint
      implements MessageHandler.Whole<String> {

    public final List<String> messages = new LinkedList<>();

    @Override
    public void onOpen(Session session, EndpointConfig config) {
      session.addMessageHandler(this);
    }

    @Override
    public void onMessage(String message) {
      messages.add(message);
    }
  }
}
