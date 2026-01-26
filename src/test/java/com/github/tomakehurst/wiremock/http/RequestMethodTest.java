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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.http.RequestMethod.*;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.matching.MultiRequestMethodPattern.IsNoneOf;
import com.github.tomakehurst.wiremock.matching.MultiRequestMethodPattern.IsOneOf;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class RequestMethodTest {

  @Test
  void singleMethodSerializesToString() {
    String json = Json.write(GET);

    assertThat(json, is("\"GET\""));
  }

  @Test
  void singleMethodDeserializesFromString() {
    String json = "\"POST\"";

    RequestMethod method = Json.read(json, RequestMethod.class);

    assertThat(method, is(POST));
  }

  @Test
  void oneOfSerializesToObject() {
    RequestMethod method = isOneOf(GET, POST);

    String json = Json.write(method);

    assertThat(
        json,
        jsonEquals( // language=JSON
            """
            {
              "oneOf": ["GET", "POST"]
            }
            """));
  }

  @Test
  void oneOfDeserializesFromObject() {
    String json = "{\"oneOf\": [\"GET\", \"POST\", \"PUT\"]}";

    RequestMethod method = Json.read(json, RequestMethod.class);

    assertInstanceOf(IsOneOf.class, method);
    IsOneOf oneOf = (IsOneOf) method;
    assertEquals(3, oneOf.getMethods().size());
    assertTrue(
        oneOf.getMethods().stream().anyMatch(m -> m.getName().equals("GET")), "Should contain GET");
    assertTrue(
        oneOf.getMethods().stream().anyMatch(m -> m.getName().equals("POST")),
        "Should contain POST");
    assertTrue(
        oneOf.getMethods().stream().anyMatch(m -> m.getName().equals("PUT")), "Should contain PUT");
  }

  @Test
  void noneOfSerializesToObject() {
    RequestMethod method = isNoneOf(DELETE, PATCH);

    String json = Json.write(method);

    assertThat(
        json,
        jsonEquals( // language=JSON
            """
            {
              "noneOf": ["DELETE", "PATCH"]
            }
            """));
  }

  @Test
  void noneOfDeserializesFromObject() {
    String json = "{\"noneOf\": [\"DELETE\", \"PATCH\", \"OPTIONS\"]}";

    RequestMethod method = Json.read(json, RequestMethod.class);

    assertInstanceOf(IsNoneOf.class, method);
    IsNoneOf noneOf = (IsNoneOf) method;
    assertEquals(3, noneOf.getMethods().size());
    assertTrue(
        noneOf.getMethods().stream().anyMatch(m -> m.getName().equals("DELETE")),
        "Should contain DELETE");
    assertTrue(
        noneOf.getMethods().stream().anyMatch(m -> m.getName().equals("PATCH")),
        "Should contain PATCH");
    assertTrue(
        noneOf.getMethods().stream().anyMatch(m -> m.getName().equals("OPTIONS")),
        "Should contain OPTIONS");
  }

  @Test
  void singleMethodRoundTripSerialization() {
    RequestMethod original = RequestMethod.PUT;

    String json = Json.write(original);
    RequestMethod deserialized = Json.read(json, RequestMethod.class);

    assertThat(deserialized.getName(), is(original.getName()));
    assertThat(deserialized.value(), is(original.value()));
  }

  @Test
  void oneOfRoundTripSerialization() {
    IsOneOf original = new IsOneOf(Set.of(GET, RequestMethod.HEAD, RequestMethod.OPTIONS));

    String json = Json.write(original);
    RequestMethod deserialized = Json.read(json, RequestMethod.class);

    assertInstanceOf(IsOneOf.class, deserialized);
    IsOneOf deserializedOneOf = (IsOneOf) deserialized;
    assertEquals(original.getMethods().size(), deserializedOneOf.getMethods().size());
    assertTrue(
        deserializedOneOf.getMethods().stream().anyMatch(m -> m.getName().equals("GET")),
        "Should contain GET");
    assertTrue(
        deserializedOneOf.getMethods().stream().anyMatch(m -> m.getName().equals("HEAD")),
        "Should contain HEAD");
    assertTrue(
        deserializedOneOf.getMethods().stream().anyMatch(m -> m.getName().equals("OPTIONS")),
        "Should contain OPTIONS");
  }

  @Test
  void noneOfRoundTripSerialization() {
    IsNoneOf original = new IsNoneOf(Set.of(POST, RequestMethod.PUT, PATCH));

    String json = Json.write(original);
    RequestMethod deserialized = Json.read(json, RequestMethod.class);

    assertInstanceOf(IsNoneOf.class, deserialized);
    IsNoneOf deserializedNoneOf = (IsNoneOf) deserialized;
    assertEquals(original.getMethods().size(), deserializedNoneOf.getMethods().size());
    assertTrue(
        deserializedNoneOf.getMethods().stream().anyMatch(m -> m.getName().equals("POST")),
        "Should contain POST");
    assertTrue(
        deserializedNoneOf.getMethods().stream().anyMatch(m -> m.getName().equals("PUT")),
        "Should contain PUT");
    assertTrue(
        deserializedNoneOf.getMethods().stream().anyMatch(m -> m.getName().equals("PATCH")),
        "Should contain PATCH");
  }
}
