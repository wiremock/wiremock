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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.admin.model.SendChannelMessageResult;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.testsupport.WebsocketTestClient;
import com.github.tomakehurst.wiremock.verification.LoggedMessageChannel;
import java.util.List;
import org.junit.jupiter.api.Test;

public class WebsocketConnectionAcceptanceTest extends WebsocketAcceptanceTestBase {

  @Test
  void websocketConnectionCanBeEstablished() {
    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/notifications");
    testClient.withWebsocketSession(
        url,
        session -> {
          assertThat(session.isOpen(), is(true));
          return null;
        });
  }

  @Test
  void canSendMessageToWebsocketViaAdminApi() {
    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/notifications");
    testClient.withWebsocketSession(
        url,
        session -> {
          RequestPattern pattern = newRequestPattern().withUrl("/notifications").build();
          SendChannelMessageResult result1 =
              wireMockServer.sendChannelMessage(ChannelType.WEBSOCKET, pattern, "Hello WebSocket!");
          SendChannelMessageResult result2 =
              wireMockServer.sendChannelMessage(ChannelType.WEBSOCKET, pattern, "Second message");

          assertThat(result1.getChannelsMessaged(), is(1));
          assertThat(result2.getChannelsMessaged(), is(1));

          List<LoggedMessageChannel> channels = result1.getChannels();
          assertThat(channels, hasSize(1));
          LoggedMessageChannel channel = channels.get(0);
          assertThat(channel.getType(), is(ChannelType.WEBSOCKET));
          assertThat(channel.isOpen(), is(true));
          assertThat(channel.getInitiatingRequest().getUrl(), is("/notifications"));

          return null;
        });

    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().size() == 2);
    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("Hello WebSocket!"));
    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("Second message"));
  }

  @Test
  void canSendMessageToMultipleWebsocketConnections() {
    WebsocketTestClient testClient1 = new WebsocketTestClient();
    WebsocketTestClient testClient2 = new WebsocketTestClient();
    String url = websocketUrl("/broadcast");

    testClient1.withWebsocketSession(
        url,
        session1 ->
            testClient2.withWebsocketSession(
                url,
                session2 -> {
                  RequestPattern pattern = newRequestPattern().withUrl("/broadcast").build();
                  SendChannelMessageResult result =
                      wireMockServer.sendChannelMessage(
                          ChannelType.WEBSOCKET, pattern, "Broadcast message");

                  assertThat(result.getChannelsMessaged(), is(2));

                  List<LoggedMessageChannel> channels = result.getChannels();
                  assertThat(channels, hasSize(2));
                  for (LoggedMessageChannel channel : channels) {
                    assertThat(channel.getType(), is(ChannelType.WEBSOCKET));
                    assertThat(channel.isOpen(), is(true));
                    assertThat(channel.getInitiatingRequest().getUrl(), is("/broadcast"));
                  }

                  return null;
                }));

    waitAtMost(5, SECONDS).until(() -> testClient1.getMessages().contains("Broadcast message"));
    waitAtMost(5, SECONDS).until(() -> testClient2.getMessages().contains("Broadcast message"));
  }

  @Test
  void requestPatternMatchingFiltersWebsocketChannels() {
    WebsocketTestClient testClientA = new WebsocketTestClient();
    WebsocketTestClient testClientB = new WebsocketTestClient();
    String urlA = websocketUrl("/channel-a");
    String urlB = websocketUrl("/channel-b");

    testClientA.withWebsocketSession(
        urlA,
        sessionA ->
            testClientB.withWebsocketSession(
                urlB,
                sessionB -> {
                  RequestPattern patternA = newRequestPattern().withUrl("/channel-a").build();
                  SendChannelMessageResult result =
                      wireMockServer.sendChannelMessage(
                          ChannelType.WEBSOCKET, patternA, "Message for A");

                  assertThat(result.getChannelsMessaged(), is(1));

                  return null;
                }));

    waitAtMost(5, SECONDS).until(() -> testClientA.getMessages().contains("Message for A"));
    assertThat(testClientB.getMessages().isEmpty(), is(true));
  }

  @Test
  void urlPatternMatchingWorksForWebsocketChannels() {
    WebsocketTestClient testClient1 = new WebsocketTestClient();
    WebsocketTestClient testClient2 = new WebsocketTestClient();
    String url1 = websocketUrl("/events/user1");
    String url2 = websocketUrl("/events/user2");

    testClient1.withWebsocketSession(
        url1,
        session1 ->
            testClient2.withWebsocketSession(
                url2,
                session2 -> {
                  RequestPattern pattern =
                      newRequestPattern().withUrl(urlPathMatching("/events/.*")).build();
                  SendChannelMessageResult result =
                      wireMockServer.sendChannelMessage(
                          ChannelType.WEBSOCKET, pattern, "Event notification");

                  assertThat(result.getChannelsMessaged(), is(2));

                  return null;
                }));

    waitAtMost(5, SECONDS).until(() -> testClient1.getMessages().contains("Event notification"));
    waitAtMost(5, SECONDS).until(() -> testClient2.getMessages().contains("Event notification"));
  }

  @Test
  void channelIsRemovedWhenWebsocketCloses() throws Exception {
    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/temp-channel");

    testClient.withWebsocketSession(
        url,
        session -> {
          RequestPattern pattern = newRequestPattern().withUrl("/temp-channel").build();
          SendChannelMessageResult result1 =
              wireMockServer.sendChannelMessage(ChannelType.WEBSOCKET, pattern, "Before close");
          assertThat(result1.getChannelsMessaged(), is(1));
          assertThat(result1.getChannels(), hasSize(1));
          return null;
        });

    Thread.sleep(100);

    RequestPattern pattern = newRequestPattern().withUrl("/temp-channel").build();
    SendChannelMessageResult result2 =
        wireMockServer.sendChannelMessage(ChannelType.WEBSOCKET, pattern, "After close");
    assertThat(result2.getChannelsMessaged(), is(0));
    assertThat(result2.getChannels(), hasSize(0));
  }
}
