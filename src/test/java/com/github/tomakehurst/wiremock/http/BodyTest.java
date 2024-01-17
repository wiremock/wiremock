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

import static com.github.tomakehurst.wiremock.common.Encoding.*;
import static com.github.tomakehurst.wiremock.common.Strings.bytesFromString;
import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.JsonException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import io.netty.handler.codec.base64.Base64Encoder;
import org.junit.jupiter.api.Test;

class BodyTest {

  @Test
  void constructsFromBytes() {
    String content = "this content";
    Body body =
        Body.fromOneOf(content.getBytes(), "not this content", new IntNode(1), "lskdjflsjdflks");

    assertThat(body.asString(), is(content));
    assertThat(body.isBinary(), is(true));
    assertThat(body.asBytes(), is(bytesFromString(content)));
    assertThat(body.isJson(), is(false));
    assertThatThrownBy(body::asJson).isInstanceOf(JsonException.class);
    assertThat(body.asBase64(), is(encodeBase64(bytesFromString(content))));
  }

  @Test
  void constructsFromBytesWhichIsUnknowinglyAJson() {
    String content = "{\"name\":\"wiremock\",\"isCool\":true}";
    Body body =
        Body.fromOneOf(content.getBytes(), "not this content", new IntNode(1), "lskdjflsjdflks");

    assertThat(body.asString(), is(content));
    assertThat(body.isBinary(), is(true));
    assertThat(body.asBytes(), is(bytesFromString(content)));
    assertThat(body.isJson(), is(false));
    assertThat(body.asJson(), is(Json.node(content)));
    assertThat(body.asBase64(), is(encodeBase64(bytesFromString(content))));
  }

  @Test
  void constructsFromString() {
    String content = "this content";
    Body body = Body.fromOneOf(null, content, new IntNode(1), "lskdjflsjdflks");

    assertThat(body.asString(), is(content));
    assertThat(body.isBinary(), is(false));
    assertThat(body.asBytes(), is(bytesFromString(content)));
    assertThat(body.isJson(), is(false));
    assertThatThrownBy(body::asJson).isInstanceOf(JsonException.class);
    assertThat(body.asBase64(), is(encodeBase64(bytesFromString(content))));
  }

  @Test
  void constructsFromJson() {
    IntNode jsonContent = new IntNode(1);
    Body body = Body.fromOneOf(null, null, jsonContent, "lskdjflsjdflks");

    assertThat(body.asString(), is("1"));
    assertThat(body.isBinary(), is(false));
    assertThat(body.asBytes(), is(bytesFromString("1")));
    assertThat(body.isJson(), is(true));
    assertThat(body.asJson(), is(jsonContent));
    assertThat(body.asBase64(), is(encodeBase64(bytesFromString("1"))));
  }

  @Test
  void mustEscapeWhenConstructedFromJson() {
    String jsonString =
        "{\n"
            + "  \"escape\": [\n"
            + "    \"quotation mark : \\\" padding\",\n"
            + "    \"reverse solidas : \\\\ padding\",\n"
            + "    \"backspace : \\b padding\",\n"
            + "    \"formfeed : \\f padding\",\n"
            + "    \"newline : \\n padding\",\n"
            + "    \"carriage return : \\r padding\",\n"
            + "    \"horizontal tab: \\t padding\",\n"
            + "    \"hex digit: \\u12ab"
            + " padding\"\n"
            + "  ]\n"
            + "}\n";
    JsonNode jsonNode = Json.node(jsonString);
    String jsonCompressedAndEscaped =
        jsonNode
            .toString()
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\b", "\b")
            .replace("\\f", "\f")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replaceAll("\\\\u12ab", "\u12ab");

    Body body = Body.fromOneOf(null, null, jsonNode, "lskdjflsjdflks");

    assertThat(body.asString(), is(jsonCompressedAndEscaped));
    assertThat(body.isBinary(), is(false));
    assertThat(body.asBytes(), is(bytesFromString(jsonCompressedAndEscaped)));
    assertThat(body.isJson(), is(true));
    assertThat(body.asJson(), is(jsonNode));
    assertThat(body.asBase64(), is(encodeBase64(bytesFromString(jsonCompressedAndEscaped))));
  }

  @Test
  void constructsFromBase64() {
    String content = "this content";
    byte[] base64Encoded = bytesFromString(encodeBase64(bytesFromString(content)));
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
    Body body = Body.fromJsonBytes(bytesFromString(jsonString));

    assertThat(body.asString(), is(jsonString));
    assertThat(body.isBinary(), is(false));
    assertThat(body.asBytes(), is(bytesFromString(jsonString)));
    assertThat(body.isJson(), is(true));
    assertThat(body.asJson(), is(jsonNode));
    assertThat(body.asBase64(), is(encodeBase64(bytesFromString(jsonString))));
  }

  @Test
  void mustEscapeWhenConstructedFromJsonBytes() {
    String jsonString =
        "{\n"
            + "  \"escape\": [\n"
            + "    \"quotation mark : \\\" padding\",\n"
            + "    \"reverse solidas : \\\\ padding\",\n"
            + "    \"backspace : \\b padding\",\n"
            + "    \"formfeed : \\f padding\",\n"
            + "    \"newline : \\n padding\",\n"
            + "    \"carriage return : \\r padding\",\n"
            + "    \"horizontal tab: \\t padding\",\n"
            + "    \"hex digit: \\u12ab"
            + " padding\"\n"
            + "  ]\n"
            + "}\n";
    byte[] jsonBytes = bytesFromString(jsonString);
    JsonNode jsonNode = Json.node(jsonString);
    String jsonCompressedAndEscaped =
        jsonNode
            .toString()
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\b", "\b")
            .replace("\\f", "\f")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replaceAll("\\\\u12ab", "\u12ab");

    Body body = Body.fromJsonBytes(jsonBytes);

    assertThat(body.asString(), is(jsonCompressedAndEscaped));
    assertThat(body.isBinary(), is(false));
    assertThat(body.asBytes(), is(bytesFromString(jsonCompressedAndEscaped)));
    assertThat(body.isJson(), is(true));
    assertThat(body.asJson(), is(jsonNode));
    assertThat(body.asBase64(), is(encodeBase64(bytesFromString(jsonCompressedAndEscaped))));
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
}
