/*
 * Copyright (C) 2018-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.stubbing;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPattern.ANYTHING;
import static java.util.stream.Collectors.toMap;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeActionDefinition;
import com.github.tomakehurst.wiremock.extension.ServeEventListenerDefinition;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class StubMappingTest {

  @Test
  public void excludesInsertionIndexFromPublicView() {
    StubMapping stub = get("/saveable").willReturn(ok()).build();

    String json = Json.write(stub);
    System.out.println(json);

    assertThat(json, not(containsString("insertionIndex")));
  }

  @Test
  public void includedInsertionIndexInPrivateView() {
    StubMapping stub = get("/saveable").willReturn(ok()).build();

    String json = Json.writePrivate(stub);
    System.out.println(json);

    assertThat(json, containsString("insertionIndex"));
  }

  @Test
  public void deserialisesInsertionIndex() {
    String json =
        // language=json
        "{\n"
            + "    \"request\": {\n"
            + "        \"method\": \"ANY\",\n"
            + "        \"url\": \"/\"\n"
            + "    },\n"
            + "    \"response\": {\n"
            + "        \"status\": 200\n"
            + "    },\n"
            + "    \"insertionIndex\": 42\n"
            + "}";

    StubMapping stub = Json.read(json, StubMapping.class);

    assertThat(stub.getInsertionIndex(), is(42L));
  }

  @Test
  public void ignoresUuidProperty() {
    String json =
        // language=json
        """
            {
              "id": "edf19376-0e08-4b27-8632-fb7852c9e62d",
              "request": {
                "url": "/",
                "method": "GET"
              },

              "response": {
                "status": 200
              },

              "uuid": "07150a3a-47ea-4182-9792-c49eb77b862e"
            }
            """;

    StubMapping stub = assertDoesNotThrow(() -> Json.read(json, StubMapping.class));

    assertThat(stub.getId().toString(), is("edf19376-0e08-4b27-8632-fb7852c9e62d"));
  }

  @Test
  public void canBeDeeplyTransformed() {
    StubMapping stub =
        get("/transformable")
            .withHeader("One", equalTo("1"))
            .withHeader("Two", containing("2"))
            .willReturn(okJson("{}").withHeader("To-Remove", "xxx"))
            .build();

    StubMapping transformed =
        stub.transform(
            stubBuilder ->
                stubBuilder
                    .setName("Transformed stub")
                    .setPriority(8)
                    .request(
                        requestBuilder ->
                            requestBuilder
                                .setUrl(urlPathEqualTo("/transformed"))
                                .setMethod(RequestMethod.POST)
                                .setHeaders(
                                    requestBuilder.getHeaders().entrySet().stream()
                                        .filter(e -> !e.getKey().equals("Two"))
                                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))))
                    .response(
                        responseBuilder ->
                            responseBuilder.headers(
                                headersBuilder ->
                                    headersBuilder.remove("To-Remove").add("To-Add", "yyy"))));

    assertThat(transformed.getName(), is("Transformed stub"));
    assertThat(transformed.getPriority(), is(8));

    assertThat(transformed.getRequest().getHeaders().get("Two"), nullValue());

    assertThat(
        transformed.getResponse().getHeaders().getHeader("To-Remove").isPresent(), is(false));
    assertThat(transformed.getResponse().getHeaders().getHeader("To-Add").firstValue(), is("yyy"));
  }

  @Test
  void stringFormIsJson() {
    StubMapping stub = get("/foo").withHeader("One", equalTo("1")).willReturn(okJson("{}")).build();

    assertThat(
        stub.toString(),
        jsonEquals(
            """
            {
              "id": "${json-unit.any-string}",
              "request": {
                "headers": {
                  "One": {
                    "equalTo": "1"
                  }
                },
                "method": "GET",
                "url": "/foo"
              },
              "response": {
                "body": {
                  "data": "{}",
                  "format": "json"
                },
                "headers": {
                  "Content-Type": "application/json"
                },
                "status": 200
              }
            }
            """));
  }

  @Test
  public void postServeActionsListIsImmutable() {
    var postServeActionDefinitions = new ArrayList<PostServeActionDefinition>();
    var builder1 = StubMapping.builder();
    postServeActionDefinitions.add(
        new PostServeActionDefinition("def-1", Parameters.one("param1", "value1")));
    builder1.setPostServeActions(postServeActionDefinitions);

    var stub1 = builder1.build();
    assertThat(stub1.getPostServeActions(), hasSize(1));
    assertThat(stub1.getPostServeActions().get(0).getName(), is("def-1"));
    assertThat(
        stub1.getPostServeActions().get(0).getParameters(), is(Parameters.one("param1", "value1")));

    postServeActionDefinitions.clear();

    assertThat(stub1.getPostServeActions(), hasSize(1));
    assertThat(stub1.getPostServeActions().get(0).getName(), is("def-1"));
    assertThat(
        stub1.getPostServeActions().get(0).getParameters(), is(Parameters.one("param1", "value1")));

    builder1.getPostServeActions().clear();

    assertThat(stub1.getPostServeActions(), hasSize(1));
    assertThat(stub1.getPostServeActions().get(0).getName(), is("def-1"));
    assertThat(
        stub1.getPostServeActions().get(0).getParameters(), is(Parameters.one("param1", "value1")));

    assertThrows(
        UnsupportedOperationException.class,
        () ->
            stub1
                .getPostServeActions()
                .add(new PostServeActionDefinition("def-2", Parameters.empty())));

    assertThat(stub1.getPostServeActions(), hasSize(1));
    assertThat(stub1.getPostServeActions().get(0).getName(), is("def-1"));
    assertThat(
        stub1.getPostServeActions().get(0).getParameters(), is(Parameters.one("param1", "value1")));

    var builder2 = stub1.toBuilder();
    builder2.getPostServeActions().add(new PostServeActionDefinition("def-2", Parameters.empty()));
    var stub2 = builder2.build();

    assertThat(stub1.getPostServeActions(), hasSize(1));
    assertThat(stub1.getPostServeActions().get(0).getName(), is("def-1"));
    assertThat(
        stub1.getPostServeActions().get(0).getParameters(), is(Parameters.one("param1", "value1")));

    assertThat(stub2.getPostServeActions(), hasSize(2));
    assertThat(stub2.getPostServeActions().get(0).getName(), is("def-1"));
    assertThat(
        stub2.getPostServeActions().get(0).getParameters(), is(Parameters.one("param1", "value1")));
    assertThat(stub2.getPostServeActions().get(1).getName(), is("def-2"));
    assertThat(stub2.getPostServeActions().get(1).getParameters(), is(Parameters.empty()));
  }

  @Test
  public void serveEventListenersListIsImmutable() {
    var serveEventListenerDefinitions = new ArrayList<ServeEventListenerDefinition>();
    var builder1 = StubMapping.builder();
    serveEventListenerDefinitions.add(
        new ServeEventListenerDefinition("def-1", Parameters.one("param1", "value1")));
    builder1.setServeEventListeners(serveEventListenerDefinitions);

    var stub1 = builder1.build();
    assertThat(stub1.getServeEventListeners(), hasSize(1));
    assertThat(stub1.getServeEventListeners().get(0).getName(), is("def-1"));
    assertThat(
        stub1.getServeEventListeners().get(0).getParameters(),
        is(Parameters.one("param1", "value1")));

    serveEventListenerDefinitions.clear();

    assertThat(stub1.getServeEventListeners(), hasSize(1));
    assertThat(stub1.getServeEventListeners().get(0).getName(), is("def-1"));
    assertThat(
        stub1.getServeEventListeners().get(0).getParameters(),
        is(Parameters.one("param1", "value1")));

    assertThrows(
        UnsupportedOperationException.class,
        () ->
            stub1
                .getServeEventListeners()
                .add(new ServeEventListenerDefinition("def-2", Parameters.empty())));

    assertThat(stub1.getServeEventListeners(), hasSize(1));
    assertThat(stub1.getServeEventListeners().get(0).getName(), is("def-1"));
    assertThat(
        stub1.getServeEventListeners().get(0).getParameters(),
        is(Parameters.one("param1", "value1")));

    builder1.getServeEventListeners().clear();

    assertThat(stub1.getServeEventListeners(), hasSize(1));
    assertThat(stub1.getServeEventListeners().get(0).getName(), is("def-1"));
    assertThat(
        stub1.getServeEventListeners().get(0).getParameters(),
        is(Parameters.one("param1", "value1")));

    var builder2 = stub1.toBuilder();
    builder2
        .getServeEventListeners()
        .add(new ServeEventListenerDefinition("def-2", Parameters.empty()));
    var stub2 = builder2.build();

    assertThat(stub1.getServeEventListeners(), hasSize(1));
    assertThat(stub1.getServeEventListeners().get(0).getName(), is("def-1"));
    assertThat(
        stub1.getServeEventListeners().get(0).getParameters(),
        is(Parameters.one("param1", "value1")));

    assertThat(stub2.getServeEventListeners(), hasSize(2));
    assertThat(stub2.getServeEventListeners().get(0).getName(), is("def-1"));
    assertThat(
        stub2.getServeEventListeners().get(0).getParameters(),
        is(Parameters.one("param1", "value1")));
    assertThat(stub2.getServeEventListeners().get(1).getName(), is("def-2"));
    assertThat(stub2.getServeEventListeners().get(1).getParameters(), is(Parameters.empty()));
  }

  @Test
  public void postServeActionsCannotBeNull() {
    var builder = StubMapping.builder();
    assertThat(builder.getPostServeActions(), empty());
    assertThrows(NullPointerException.class, () -> builder.setPostServeActions(null));
    assertThat(builder.getPostServeActions(), empty());
    assertThat(builder.build().getPostServeActions(), empty());
    var stub =
        new StubMapping(
            UUID.randomUUID(),
            "whatever",
            false,
            ANYTHING,
            ResponseDefinition.ok(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            0);
    assertThat(stub.getPostServeActions(), empty());
  }

  @Test
  public void serveEventListenersCannotBeNull() {
    var builder = StubMapping.builder();
    assertThat(builder.getServeEventListeners(), empty());
    assertThrows(NullPointerException.class, () -> builder.setServeEventListeners(null));
    assertThat(builder.getServeEventListeners(), empty());
    assertThat(builder.build().getServeEventListeners(), empty());
    var stub =
        new StubMapping(
            UUID.randomUUID(),
            "whatever",
            false,
            ANYTHING,
            ResponseDefinition.ok(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            0);
    assertThat(stub.getServeEventListeners(), empty());
  }

  @Test
  public void metadataCannotBeNull() {
    var builder = StubMapping.builder();
    assertThat(builder.getMetadata(), anEmptyMap());
    assertThrows(NullPointerException.class, () -> builder.setMetadata(null));
    assertThat(builder.getMetadata(), anEmptyMap());
    assertThat(builder.build().getMetadata(), anEmptyMap());
    var stub =
        new StubMapping(
            UUID.randomUUID(),
            "whatever",
            false,
            ANYTHING,
            ResponseDefinition.ok(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            0);
    assertThat(stub.getMetadata(), anEmptyMap());
  }
}
