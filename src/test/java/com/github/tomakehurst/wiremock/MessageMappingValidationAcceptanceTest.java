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
import org.junit.jupiter.api.Test;

public class MessageMappingValidationAcceptanceTest extends AcceptanceTestBase {

  @AfterEach
  void resetStubs() {
    testClient.delete("/__admin/message-mappings");
  }

  @Test
  void sendActionWithNoMessageReturns422() {
    WireMockResponse response =
        testClient.postWithBody(
            "/__admin/message-mappings",
            // language=json
            """
            {
              "actions": [
                {
                  "type": "send",
                  "channelTarget": { "type": "originating" }
                }
              ]
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

  @Test
  void fixedChannelTargetWithNoProviderNameReturns422() {
    WireMockResponse response =
        testClient.postWithBody(
            "/__admin/message-mappings",
            // language=json
            """
            {
              "actions": [
                {
                  "type": "send",
                  "message": { "body": "test" },
                  "channelTarget": {
                    "type": "fixed-channel",
                    "channelName": "orders"
                  }
                }
              ]
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
  void fixedChannelTargetWithNoChannelNameReturns422() {
    WireMockResponse response =
        testClient.postWithBody(
            "/__admin/message-mappings",
            // language=json
            """
            {
              "actions": [
                {
                  "type": "send",
                  "message": { "body": "test" },
                  "channelTarget": {
                    "type": "fixed-channel",
                    "providerName": "events"
                  }
                }
              ]
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
                  "source": { "pointer": "channelName" },
                  "title": "channelName is required"
                }
              ]
            }
            """));
  }

  @Test
  void httpRequestTriggerWithNoRequestPatternReturns422() {
    WireMockResponse response =
        testClient.postWithBody(
            "/__admin/message-mappings",
            // language=json
            """
            {
              "trigger": { "type": "http-request" },
              "actions": [
                {
                  "type": "send",
                  "message": { "body": "test" }
                }
              ]
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
                  "source": { "pointer": "requestPattern" },
                  "title": "requestPattern is required"
                }
              ]
            }
            """));
  }

  @Test
  void httpStubTriggerWithNoStubIdReturns422() {
    WireMockResponse response =
        testClient.postWithBody(
            "/__admin/message-mappings",
            // language=json
            """
            {
              "trigger": { "type": "http-stub" },
              "actions": [
                {
                  "type": "send",
                  "message": { "body": "test" }
                }
              ]
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
                  "source": { "pointer": "stubId" },
                  "title": "stubId is required"
                }
              ]
            }
            """));
  }
}
