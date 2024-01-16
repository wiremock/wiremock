/*
 * Copyright (C) 2015-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.JsonException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class BodyTest {

  @Test
  void constructsFromBytes() {
    String content = "this content";
    Body body =
        Body.fromOneOf(content.getBytes(), "not this content", new IntNode(1), "lskdjflsjdflks");

    assertThat(body.asString(), is(content));
    assertThat(body.isBinary(), is(true));
    assertThat(body.asBytes(), is(content.getBytes(StandardCharsets.UTF_8)));
    assertThat(body.isJson(), is(false));
    assertThatThrownBy(body::asJson).isInstanceOf(JsonException.class);
    assertThat(body.asBase64(), is(base64EncodeToString(content)));
  }

  @Test
  void constructsFromBytesWhichIsUnknowinglyAJson() {
    String content = "{\"name\":\"wiremock\",\"isCool\":true}";
    Body body =
        Body.fromOneOf(content.getBytes(), "not this content", new IntNode(1), "lskdjflsjdflks");

    assertThat(body.asString(), is(content));
    assertThat(body.isBinary(), is(true));
    assertThat(body.asBytes(), is(content.getBytes(StandardCharsets.UTF_8)));
    assertThat(body.isJson(), is(false));
    assertThat(body.asJson(), is(Json.node(content)));
    assertThat(body.asBase64(), is(base64EncodeToString(content)));
  }

  @Test
  void constructsFromString() {
    String content = "this content";
    Body body = Body.fromOneOf(null, content, new IntNode(1), "lskdjflsjdflks");

    assertThat(body.asString(), is(content));
    assertThat(body.isBinary(), is(false));
    assertThat(body.asBytes(), is(content.getBytes(StandardCharsets.UTF_8)));
    assertThat(body.isJson(), is(false));
    assertThatThrownBy(body::asJson).isInstanceOf(JsonException.class);
    assertThat(body.asBase64(), is(base64EncodeToString(content)));
  }

  @Test
  void constructsFromJson() {
    IntNode jsonContent = new IntNode(1);
    Body body = Body.fromOneOf(null, null, jsonContent, "lskdjflsjdflks");

    assertThat(body.asString(), is("1"));
    assertThat(body.isBinary(), is(false));
    assertThat(body.asBytes(), is("1".getBytes(StandardCharsets.UTF_8)));
    assertThat(body.isJson(), is(true));
    assertThat(body.asJson(), is(jsonContent));
    assertThat(body.asBase64(), is(base64EncodeToString("1")));
  }

  @Test
  void constructsFromBase64() {
    String content = "this content";
    byte[] base64Encoded = base64Encode(content);
    String encodedText = stringFromBytes(base64Encoded);
    Body body = Body.fromOneOf(null, null, null, encodedText);

    assertThat(body.asString(), is(content));
    assertThat(body.isBinary(), is(true));
    assertThat(body.asBytes(), is(content.getBytes(StandardCharsets.UTF_8)));
    assertThat(body.isJson(), is(false));
    assertThatThrownBy(body::asJson).isInstanceOf(JsonException.class);
    assertThat(body.asBase64(), is(encodedText));
  }

  @Test
  void constructsFromJsonBytes() {
    String jsonString = "{\"name\":\"wiremock\",\"isCool\":true}";
    JsonNode jsonNode = Json.node(jsonString);
    Body body = Body.fromJsonBytes(jsonString.getBytes(StandardCharsets.UTF_8));

    assertThat(body.asString(), is(jsonString));
    assertThat(body.isBinary(), is(false));
    assertThat(body.asBytes(), is(jsonString.getBytes(StandardCharsets.UTF_8)));
    assertThat(body.isJson(), is(true));
    assertThat(body.asJson(), is(jsonNode));
    assertThat(body.asBase64(), is(base64EncodeToString(jsonString)));
  }

  @Test
  void bodyAsJson() {
    final JsonNode jsonContent = Json.node("{\"name\":\"wiremock\",\"isCool\":true}");
    Body body = Body.fromOneOf(null, null, jsonContent, "lskdjflsjdflks");

    assertThat(body.asJson(), is(jsonContent));
  }

  @Test
  void hashCorrectly() {
    byte[] primes = {2, 3, 5, 7};
    byte[] primes2 = {2, 3, 5, 7};

    Body body = new Body(primes);
    Body body2 = new Body(primes2);

    assertEquals(body.hashCode(), body2.hashCode());
  }

  private String base64EncodeToString(String string) {
    return new String(base64Encode(string), StandardCharsets.UTF_8);
  }

  private byte[] base64Encode(String string) {
    return Base64.getEncoder().encode(string.getBytes(StandardCharsets.UTF_8));
  }
}
