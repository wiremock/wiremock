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
package org.wiremock.webhooks;

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.Format;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.UniformDistribution;
import java.util.List;
import org.junit.jupiter.api.Test;

public class WebhookDefinitionTest {

  @Test
  void serialisesCorrectlyWhenStringBodyUsed() {
    WebhookDefinition webhookDefinition =
        new WebhookDefinition()
            .withUrl("https://example.com/post-me")
            .withMethod(RequestMethod.POST)
            .withBody("body text")
            .withDelay(new UniformDistribution(10, 20))
            .withHeaders(List.of(httpHeader("Content-Type", "text/plain")));

    var json = Json.write(webhookDefinition);

    assertThat(
        json,
        jsonEquals(
            // language=json
            """
                {
                   "url": "https://example.com/post-me",
                   "method": "POST",
                   "headers": {
                     "Content-Type": "text/plain"
                   },
                   "body": "body text",
                   "delay": {
                     "type": "uniform",
                     "lower": 10,
                     "upper": 20
                   }
                 }
                """));
  }

  @Test
  void serialisesCorrectlyWhenBodyFileNameUsed() {
    WebhookDefinition webhookDefinition =
        new WebhookDefinition()
            .withUrl("https://example.com/post-me")
            .withMethod(RequestMethod.POST)
            .withBody("body text")
            .withBodyFileName("webhooks/body.json");

    var json = Json.write(webhookDefinition);

    assertThat(
        json,
        jsonEquals(
            // language=json
            """
                {
                   "url": "https://example.com/post-me",
                   "method": "POST",
                   "bodyFileName": "webhooks/body.json"
                 }
                """));
  }

  @Test
  void deserialisesCorrectlyWhenStringBodyUsed() {
    var json = // language=json
        """
                {
                   "url": "https://example.com/post-me",
                   "method": "POST",
                   "body": "<stuff />",
                   "headers": {
                      "Content-Type": "application/xml"
                   }
                 }
                """;

    WebhookDefinition webhookDefinition = Json.read(json, WebhookDefinition.class);

    assertThat(webhookDefinition.getBody(), is("<stuff />"));

    EntityDefinition entity = webhookDefinition.getBodyEntityDefinition();
    assertThat(entity.getFilePath(), nullValue());
    assertThat(entity.getDataAsString(), is("<stuff />"));
    assertThat(entity.getFormat(), is(Format.XML));
  }

  @Test
  void deserialisesCorrectlyWhenBodyFileNameUsed() {
    var json = // language=json
        """
                {
                   "url": "https://example.com/post-me",
                   "method": "POST",
                   "bodyFileName": "webhooks/body.json",
                   "headers": {
                      "Content-Type": "text/json"
                   }
                 }
                """;

    WebhookDefinition webhookDefinition = Json.read(json, WebhookDefinition.class);

    assertThat(webhookDefinition.getBodyFileName(), is("webhooks/body.json"));

    EntityDefinition entity = webhookDefinition.getBodyEntityDefinition();
    assertThat(entity.getFilePath(), is("webhooks/body.json"));
    assertThat(entity.getFormat(), is(Format.JSON));
    assertThat(entity.getData(), nullValue());
  }

  @Test
  void deserialisesCorrectlyWhenBase64BodyUsed() {
    String base64Body = "e30K";
    var json = // language=json
        """
                {
                   "url": "https://example.com/post-me",
                   "method": "POST",
                   "base64Body": "%s",
                   "headers": {
                      "Content-Type": "application/octet-stream"
                   }
                 }
                """
            .formatted(base64Body);

    WebhookDefinition webhookDefinition = Json.read(json, WebhookDefinition.class);

    assertThat(webhookDefinition.getBase64Body(), is(base64Body));

    EntityDefinition entity = webhookDefinition.getBodyEntityDefinition();
    assertThat(entity.getDataAsBytes(), is(Encoding.decodeBase64(base64Body)));
    assertThat(entity.getFormat(), is(Format.BINARY));
    assertThat(entity.getData(), nullValue());
  }

  @Test
  void deserialisesCorrectlyWhenJsonBodyUsed() {
    var json = // language=json
        """
                {
                   "url": "https://example.com/post-me",
                   "method": "POST",
                   "jsonBody": {
                     "key": "value"
                   }
                 }
                """;

    WebhookDefinition webhookDefinition = Json.read(json, WebhookDefinition.class);

    assertThat(webhookDefinition.getBody(), jsonEquals("{\"key\":\"value\"}"));

    EntityDefinition entity = webhookDefinition.getBodyEntityDefinition();
    assertThat(entity.getDataAsString(), jsonEquals("{\"key\":\"value\"}"));
    assertThat(entity.getFormat(), is(Format.JSON));
  }
}
