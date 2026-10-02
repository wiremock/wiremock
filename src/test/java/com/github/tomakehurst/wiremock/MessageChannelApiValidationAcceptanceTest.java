/*
 * Copyright (C) 2026 Thomas Akehurst
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

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageChannelApiValidationAcceptanceTest extends AcceptanceTestBase {

  private static final String PROVIDER = "validation-test-provider";

  @BeforeEach
  void registerProvider() {
    testClient.postWithBody(
        "/__admin/channel-providers",
        // language=json
        """
        {
          "name": "%s",
          "driverType": "in-memory"
        }
        """
            .formatted(PROVIDER),
        "application/json");
  }

  @AfterEach
  void removeProvider() {
    testClient.delete("/__admin/channel-providers/" + PROVIDER);
  }

  @Test
  void registeringChannelProviderWithNoNameReturns422() {
    WireMockResponse response =
        testClient.postWithBody(
            "/__admin/channel-providers",
            // language=json
            """
            {
              "driverType": "in-memory"
            }
            """,
            "application/json");

    assertThat(response.statusCode(), is(422));
    assertThat(
        response.content(),
        jsonEquals(
            // language=json
            """
            {
              "errors": [
                {
                  "code": 10,
                  "source": { "pointer": "name" },
                  "title": "name is required"
                }
              ]
            }
            """));
  }

  @Test
  void creatingFixedChannelWithNoProviderNameReturns422() {
    WireMockResponse response =
        testClient.postWithBody(
            "/__admin/channels",
            // language=json
            """
            {
              "channelName": "orders"
            }
            """,
            "application/json");

    assertThat(response.statusCode(), is(422));
    assertThat(
        response.content(),
        jsonEquals(
            // language=json
            """
            {
              "errors": [
                {
                  "code": 10,
                  "source": { "pointer": "providerName" },
                  "title": "providerName is required"
                }
              ]
            }
            """));
  }

  @Test
  void creatingFixedChannelWithNoFieldsReturns422() {
    WireMockResponse response =
        testClient.postWithBody("/__admin/channels", "{}", "application/json");

    assertThat(response.statusCode(), is(422));
    assertThat(
        response.content(),
        jsonEquals(
            // language=json
            """
            {
              "errors": [
                {
                  "code": 10,
                  "source": { "pointer": "providerName" },
                  "title": "providerName is required"
                }
              ]
            }
            """));
  }

  @Test
  void updatingChannelProviderWithNoNameInBodyReturns422() {
    WireMockResponse response =
        testClient.putWithBody(
            "/__admin/channel-providers/" + PROVIDER,
            // language=json
            """
            {
              "driverType": "in-memory"
            }
            """,
            "application/json");

    assertThat(response.statusCode(), is(422));
    assertThat(
        response.content(),
        jsonEquals(
            // language=json
            """
            {
              "errors": [
                {
                  "code": 10,
                  "source": { "pointer": "name" },
                  "title": "name is required"
                }
              ]
            }
            """));
  }

  @Test
  void sendingMessageToFixedChannelWithNoMessageBodyReturns422() {
    WireMockResponse response =
        testClient.postWithBody(
            "/__admin/channels/send",
            // language=json
            """
            {
              "type": "FIXED",
              "providerName": "%s",
              "channelName": "orders"
            }
            """
                .formatted(PROVIDER),
            "application/json");

    assertThat(response.statusCode(), is(422));
    assertThat(
        response.content(),
        jsonEquals(
            // language=json
            """
            {
              "errors": [
                {
                  "code": 10,
                  "source": { "pointer": "message" },
                  "title": "message is required"
                }
              ]
            }
            """));
  }

  @Test
  void sendingMessageToWebsocketChannelsWithNoMessageBodyReturns422() {
    WireMockResponse response =
        testClient.postWithBody(
            "/__admin/channels/send",
            // language=json
            """
            {
              "type": "WEBSOCKET",
              "initiatingRequest": { "url": "/ws" }
            }
            """,
            "application/json");

    assertThat(response.statusCode(), is(422));
    assertThat(
        response.content(),
        jsonEquals(
            // language=json
            """
            {
              "errors": [
                {
                  "code": 10,
                  "source": { "pointer": "message" },
                  "title": "message is required"
                }
              ]
            }
            """));
  }
}
