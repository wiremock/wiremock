/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common.entity;

import static com.github.tomakehurst.wiremock.common.entity.BinaryEntityDefinition.aBinaryMessage;
import static com.github.tomakehurst.wiremock.common.entity.TextEntityDefinition.aTextMessage;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.common.Json;
import java.util.Base64;
import org.junit.jupiter.api.Test;

public class EntityDefinitionSerializationTest {

  @Test
  void stringEntityDefinitionSerializesCorrectly() {
    StringEntityDefinition entity = new StringEntityDefinition("test message");

    String json = Json.write(entity);

    assertThat(json, is("\"test message\""));
  }

  @Test
  void stringEntityDefinitionDeserializesCorrectly() {
    String json = "\"test message\"";

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    assertThat(entity, is(new StringEntityDefinition("test message")));
  }

  @Test
  void stringEntityDefinitionRoundTripSerialization() {
    StringEntityDefinition original = new StringEntityDefinition("simple string");

    String json = Json.write(original);
    EntityDefinition deserialized = Json.read(json, EntityDefinition.class);

    assertEquals(original, deserialized);
  }

  @Test
  void textEntityDefinitionSerializesWithLowercaseEnums() {
    TextEntityDefinition entity =
        aTextMessage()
            .withFormat(FormatType.XML)
            .withCompression(CompressionType.GZIP)
            .withBody("test data")
            .build();

    String json = Json.write(entity);

    String expectedJson =
        """
        {
          "format" : "xml",
          "compression" : "gzip",
          "data" : "test data"
        }
        """;

    assertThat(json, jsonEquals(expectedJson));
  }

  @Test
  void textEntityDefinitionDeserializesWithLowercaseEnums() {
    String json =
        """
        {
          "format" : "yaml",
          "compression" : "brotli",
          "data" : "test data"
        }
        """;

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    TextEntityDefinition textEntity = (TextEntityDefinition) entity;
    assertThat(textEntity.getFormat(), is(FormatType.YAML));
    assertThat(textEntity.getCompression(), is(CompressionType.BROTLI));
    assertThat(textEntity.getData(), is("test data"));
  }

  @Test
  void textEntityDefinitionWithDefaultsOmitsEnums() {
    TextEntityDefinition entity = aTextMessage().withBody("test data").build();

    String json = Json.write(entity);

    String expectedJson = """
        {
          "data" : "test data"
        }
        """;

    assertThat(json, jsonEquals(expectedJson));
  }

  @Test
  void textEntityDefinitionRoundTripSerialization() {
    TextEntityDefinition original =
        aTextMessage()
            .withFormat(FormatType.JSON)
            .withCompression(CompressionType.GZIP)
            .withBody("{\"key\":\"value\"}")
            .build();

    String json = Json.write(original);
    EntityDefinition deserialized = Json.read(json, EntityDefinition.class);

    assertEquals(original, deserialized);
  }

  @Test
  void binaryEntityDefinitionSerializesWithLowercaseEnums() {
    byte[] data = {1, 2, 3, 4, 5};
    BinaryEntityDefinition entity =
        aBinaryMessage().withCompression(CompressionType.DEFLATE).withBody(data).build();

    String json = Json.write(entity);

    String expectedJson =
        """
        {
          "encoding" : "binary",
          "compression" : "deflate",
          "data" : "%s"
        }
        """
            .formatted(Base64.getEncoder().encodeToString(data));

    assertThat(json, jsonEquals(expectedJson));
  }

  @Test
  void binaryEntityDefinitionDeserializesWithLowercaseEnums() {
    byte[] data = {10, 20, 30};
    String json =
        """
        {
          "encoding" : "binary",
          "compression" : "gzip",
          "data" : "%s"
        }
        """
            .formatted(Base64.getEncoder().encodeToString(data));

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    BinaryEntityDefinition binaryEntity = (BinaryEntityDefinition) entity;
    assertThat(binaryEntity.getEncoding(), is(EncodingType.BINARY));
    assertThat(binaryEntity.getCompression(), is(CompressionType.GZIP));
    assertThat(binaryEntity.getDataAsBytes(), is(data));
  }

  @Test
  void binaryEntityDefinitionRoundTripSerialization() {
    byte[] data = {1, 2, 3, 4, 5, 6, 7, 8};
    BinaryEntityDefinition original =
        aBinaryMessage().withCompression(CompressionType.BROTLI).withBody(data).build();

    String json = Json.write(original);
    EntityDefinition deserialized = Json.read(json, EntityDefinition.class);

    assertEquals(original, deserialized);
  }
}
