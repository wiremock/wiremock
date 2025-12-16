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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.admin.model.SendChannelMessageResult;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import org.junit.jupiter.api.Test;

/**
 * Acceptance tests for WebSocket mocking support. Tests that WebSocket connections can be
 * established and messages can be sent to connected clients via the admin API.
 */
public class WebsocketAcceptanceTest extends AcceptanceTestBase {

  WebSocketContainer websocketClient = ContainerProvider.getWebSocketContainer();

  @Test
  void websocketConnectionCanBeEstablished() throws Exception {
    NotificationsClientEndpoint endpoint = new NotificationsClientEndpoint();
    ClientEndpointConfig endpointConfig = ClientEndpointConfig.Builder.create().build();

    URI url = URI.create("ws://localhost:" + wireMockServer.port() + "/notifications");
    try (Session session = websocketClient.connectToServer(endpoint, endpointConfig, url)) {
      assertThat(session.isOpen(), is(true));
      session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Goodbye"));
    }
  }

  @Test
  void canSendMessageToWebsocketViaAdminApi() throws Exception {
    NotificationsClientEndpoint endpoint = new NotificationsClientEndpoint();
    ClientEndpointConfig endpointConfig = ClientEndpointConfig.Builder.create().build();
    var responses = new ArrayList<String>();

    URI url = URI.create("ws://localhost:" + wireMockServer.port() + "/notifications");
    try (Session session = websocketClient.connectToServer(endpoint, endpointConfig, url)) {
      // Give the server a moment to register the channel
      Thread.sleep(100);

      // Send messages via the admin API using request pattern matching
      RequestPattern pattern = newRequestPattern().withUrl("/notifications").build();
      SendChannelMessageResult result1 =
          wireMockServer.sendWebSocketMessage(pattern, "Hello WebSocket!");
      SendChannelMessageResult result2 =
          wireMockServer.sendWebSocketMessage(pattern, "Second message");

      assertThat(result1.getChannelsMessaged(), is(1));
      assertThat(result2.getChannelsMessaged(), is(1));

      String message = endpoint.messageQueue.poll(5, SECONDS);
      responses.add(message);
      message = endpoint.messageQueue.poll(5, SECONDS);
      responses.add(message);

      session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Goodbye"));
    }

    assertThat(responses.size(), is(2));
    assertThat(responses.get(0), is("Hello WebSocket!"));
    assertThat(responses.get(1), is("Second message"));
  }

  @Test
  void canSendMessageToMultipleWebsocketConnections() throws Exception {
    NotificationsClientEndpoint endpoint1 = new NotificationsClientEndpoint();
    NotificationsClientEndpoint endpoint2 = new NotificationsClientEndpoint();
    ClientEndpointConfig endpointConfig = ClientEndpointConfig.Builder.create().build();

    URI url = URI.create("ws://localhost:" + wireMockServer.port() + "/broadcast");
    try (Session session1 = websocketClient.connectToServer(endpoint1, endpointConfig, url);
        Session session2 = websocketClient.connectToServer(endpoint2, endpointConfig, url)) {
      // Give the server a moment to register the channels
      Thread.sleep(100);

      // Send message to all connections on /broadcast
      RequestPattern pattern = newRequestPattern().withUrl("/broadcast").build();
      SendChannelMessageResult result =
          wireMockServer.sendWebSocketMessage(pattern, "Broadcast message");

      assertThat(result.getChannelsMessaged(), is(2));

      String message1 = endpoint1.messageQueue.poll(5, SECONDS);
      String message2 = endpoint2.messageQueue.poll(5, SECONDS);

      assertThat(message1, is("Broadcast message"));
      assertThat(message2, is("Broadcast message"));

      session1.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Goodbye"));
      session2.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Goodbye"));
    }
  }

  @Test
  void requestPatternMatchingFiltersWebsocketChannels() throws Exception {
    NotificationsClientEndpoint endpoint1 = new NotificationsClientEndpoint();
    NotificationsClientEndpoint endpoint2 = new NotificationsClientEndpoint();
    ClientEndpointConfig endpointConfig = ClientEndpointConfig.Builder.create().build();

    URI url1 = URI.create("ws://localhost:" + wireMockServer.port() + "/channel-a");
    URI url2 = URI.create("ws://localhost:" + wireMockServer.port() + "/channel-b");
    try (Session session1 = websocketClient.connectToServer(endpoint1, endpointConfig, url1);
        Session session2 = websocketClient.connectToServer(endpoint2, endpointConfig, url2)) {
      // Give the server a moment to register the channels
      Thread.sleep(100);

      // Send message only to /channel-a
      RequestPattern patternA = newRequestPattern().withUrl("/channel-a").build();
      SendChannelMessageResult result =
          wireMockServer.sendWebSocketMessage(patternA, "Message for A");

      assertThat(result.getChannelsMessaged(), is(1));

      String message1 = endpoint1.messageQueue.poll(2, SECONDS);
      String message2 = endpoint2.messageQueue.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS);

      assertThat(message1, is("Message for A"));
      assertThat(message2, is((String) null)); // endpoint2 should not receive the message

      session1.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Goodbye"));
      session2.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Goodbye"));
    }
  }

  @Test
  void urlPatternMatchingWorksForWebsocketChannels() throws Exception {
    NotificationsClientEndpoint endpoint1 = new NotificationsClientEndpoint();
    NotificationsClientEndpoint endpoint2 = new NotificationsClientEndpoint();
    ClientEndpointConfig endpointConfig = ClientEndpointConfig.Builder.create().build();

    URI url1 = URI.create("ws://localhost:" + wireMockServer.port() + "/events/user1");
    URI url2 = URI.create("ws://localhost:" + wireMockServer.port() + "/events/user2");
    try (Session session1 = websocketClient.connectToServer(endpoint1, endpointConfig, url1);
        Session session2 = websocketClient.connectToServer(endpoint2, endpointConfig, url2)) {
      // Give the server a moment to register the channels
      Thread.sleep(100);

      // Send message to all /events/* channels using path pattern
      RequestPattern pattern =
          newRequestPattern().withUrl(urlPathMatching("/events/.*")).build();
      SendChannelMessageResult result =
          wireMockServer.sendWebSocketMessage(pattern, "Event notification");

      assertThat(result.getChannelsMessaged(), is(2));

      String message1 = endpoint1.messageQueue.poll(5, SECONDS);
      String message2 = endpoint2.messageQueue.poll(5, SECONDS);

      assertThat(message1, is("Event notification"));
      assertThat(message2, is("Event notification"));

      session1.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Goodbye"));
      session2.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Goodbye"));
    }
  }

  @Test
  void channelIsRemovedWhenWebsocketCloses() throws Exception {
    NotificationsClientEndpoint endpoint = new NotificationsClientEndpoint();
    ClientEndpointConfig endpointConfig = ClientEndpointConfig.Builder.create().build();

    URI url = URI.create("ws://localhost:" + wireMockServer.port() + "/temp-channel");
    Session session = websocketClient.connectToServer(endpoint, endpointConfig, url);

    // Give the server a moment to register the channel
    Thread.sleep(100);

    // Verify channel is registered
    RequestPattern pattern = newRequestPattern().withUrl("/temp-channel").build();
    SendChannelMessageResult result1 =
        wireMockServer.sendWebSocketMessage(pattern, "Before close");
    assertThat(result1.getChannelsMessaged(), is(1));

    // Close the session
    session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Goodbye"));

    // Give the server a moment to process the close
    Thread.sleep(100);

    // Verify channel is removed
    SendChannelMessageResult result2 =
        wireMockServer.sendWebSocketMessage(pattern, "After close");
    assertThat(result2.getChannelsMessaged(), is(0));
  }

  public static class NotificationsClientEndpoint extends Endpoint
      implements MessageHandler.Whole<String> {

    private final LinkedBlockingDeque<String> messageQueue = new LinkedBlockingDeque<>();
    private final CountDownLatch closeLatch = new CountDownLatch(1);

    @Override
    public void onClose(Session session, CloseReason closeReason) {
      closeLatch.countDown();
    }

    @Override
    public void onError(Session session, Throwable cause) {}

    @Override
    public void onOpen(Session session, EndpointConfig config) {
      session.addMessageHandler(this);
    }

    @Override
    public void onMessage(String message) {
      messageQueue.offer(message);
    }
  }
}
