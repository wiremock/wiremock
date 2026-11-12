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
package com.github.tomakehurst.wiremock.message;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.message;
import static com.github.tomakehurst.wiremock.client.WireMock.sendMessage;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.common.Json;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MessageStubMappingOrMappingsTest {

  @Test
  void deserialisesASingleMappingFromAnObjectWithoutAMessageMappingsProperty() {
    MessageStubMappingOrMappings result =
        Json.read(
            // language=json
            """
            {
              "name": "single",
              "trigger": {
                "message": { "body": { "equalTo": "ping" } }
              },
              "actions": [
                { "type": "send", "message": { "body": "pong" } }
              ]
            }
            """,
            MessageStubMappingOrMappings.class);

    assertThat(result, instanceOf(MessageStubMapping.class));
    assertThat(result.isMulti(), is(false));
    assertThat(result.getMappingOrMappings(), hasSize(1));
    assertThat(result.getMappingOrMappings().get(0).getName(), is("single"));
  }

  @Test
  void deserialisesACollectionFromAnObjectWithAMessageMappingsProperty() {
    MessageStubMappingOrMappings result =
        Json.read(
            // language=json
            """
            {
              "$schema": "https://example.com/schema.json",
              "meta": { "total": 2 },
              "messageMappings": [
                { "name": "one" },
                { "name": "two" }
              ]
            }
            """,
            MessageStubMappingOrMappings.class);

    assertThat(result, instanceOf(MessageStubMappingCollection.class));
    assertThat(result.isMulti(), is(true));
    assertThat(result.getMappingOrMappings(), hasSize(2));
    assertThat(result.getMappingOrMappings().get(1).getName(), is("two"));
  }

  @Test
  void deserialisesAnEmptyObjectAsASingleMapping() {
    MessageStubMappingOrMappings result = Json.read("{}", MessageStubMappingOrMappings.class);

    assertThat(result, instanceOf(MessageStubMapping.class));
    assertThat(result.getMappingOrMappings(), hasSize(1));
  }

  @Test
  void ignoresSchemaAndUuidPropertiesOnASingleMapping() {
    MessageStubMappingOrMappings result =
        Json.read(
            // language=json
            """
            {
              "$schema": "https://example.com/schema.json",
              "uuid": "a7b4e9e2-9f3c-4b2e-8d0e-2b9d7f0c1a11",
              "name": "with-ignored-properties"
            }
            """,
            MessageStubMappingOrMappings.class);

    assertThat(result, instanceOf(MessageStubMapping.class));
    assertThat(result.getMappingOrMappings().get(0).getName(), is("with-ignored-properties"));
  }

  @Test
  void serialisingASingleMappingDoesNotEmitATypeProperty() {
    UUID id = UUID.fromString("c0a1b2c3-d4e5-4f60-8a71-9b8c7d6e5f40");
    MessageStubMapping stub =
        message()
            .withId(id)
            .withName("serialised")
            .withBody(equalTo("ping"))
            .willTriggerActions(sendMessage("pong").onOriginatingChannel());

    assertThat(
        Json.write(stub),
        jsonEquals(
            // language=json
            """
            {
              "id": "c0a1b2c3-d4e5-4f60-8a71-9b8c7d6e5f40",
              "name": "serialised",
              "trigger": {
                "type": "message",
                "message": { "body": { "equalTo": "ping" } }
              },
              "actions": [
                {
                  "type": "send",
                  "message": { "body": { "data": "pong" } },
                  "channelTarget": { "type": "originating" }
                }
              ]
            }
            """));
  }
}
