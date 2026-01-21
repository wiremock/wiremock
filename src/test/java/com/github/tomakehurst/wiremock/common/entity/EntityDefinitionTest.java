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
package com.github.tomakehurst.wiremock.common.entity;

import static com.github.tomakehurst.wiremock.client.WireMock.binaryEntity;
import static com.github.tomakehurst.wiremock.client.WireMock.textEntity;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.BROTLI;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.DEFLATE;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.GZIP;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.NONE;
import static com.github.tomakehurst.wiremock.common.entity.FormatType.HTML;
import static com.github.tomakehurst.wiremock.common.entity.FormatType.JSON;
import static com.github.tomakehurst.wiremock.common.entity.FormatType.TEXT;
import static com.github.tomakehurst.wiremock.common.entity.FormatType.XML;
import static com.github.tomakehurst.wiremock.common.entity.FormatType.YAML;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class EntityDefinitionTest {

  @Test
  void v3StyleTextEntitySerializesAsString() {
    EntityDefinition entity = TextEntityDefinition.simple("simple text");

    String json = Json.write(entity);

    assertThat(json, is("\"simple text\""));
  }

  @Test
  void v3StyleTextEntityDeserializesFromString() {
    String json = "\"simple text\"";

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    assertThat(entity, is(TextEntityDefinition.simple("simple text")));
  }

  @Test
  void v3StyleTextEntityRoundTripSerialization() {
    TextEntityDefinition original = TextEntityDefinition.simple("round trip text");

    String json = Json.write(original);
    EntityDefinition deserialized = Json.read(json, EntityDefinition.class);

    assertEquals(original, deserialized);
  }

  @Test
  void normalTextEntitySerializesAsObject() {
    TextEntityDefinition entity = textEntity().setData("test data").build();

    String json = Json.write(entity);

    String expectedJson =
        """
                    {
                      "data" : "test data"
                    }
                    """;

    assertThat(json, jsonEquals(expectedJson));
  }

  @Test
  void normalTextEntityDeserializesFromObject() {
    String json =
        """
                    {
                      "data" : "test data"
                    }
                    """;

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    TextEntityDefinition textEntity = (TextEntityDefinition) entity;
    assertThat(textEntity.getData(), is("test data"));
  }

  @Test
  void normalTextEntityRoundTripSerialization() {
    TextEntityDefinition original = textEntity().setData("round trip data").build();

    String json = Json.write(original);
    EntityDefinition deserialized = Json.read(json, EntityDefinition.class);

    assertEquals(original, deserialized);
  }

  @Test
  void textEntityWithFormatAndCompressionSerializesCorrectly() {
    TextEntityDefinition entity =
        textEntity()
            .setFormat(FormatType.XML)
            .setCompression(CompressionType.GZIP)
            .setData("<root>data</root>")
            .build();

    String json = Json.write(entity);

    String expectedJson =
        """
                    {
                      "format" : "xml",
                      "compression" : "gzip",
                      "data" : "<root>data</root>"
                    }
                    """;

    assertThat(json, jsonEquals(expectedJson));
  }

  @Test
  void textEntityWithFormatAndCompressionDeserializesCorrectly() {
    String json =
        """
                    {
                      "format" : "yaml",
                      "compression" : "brotli",
                      "data" : "key: value"
                    }
                    """;

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    TextEntityDefinition textEntity = (TextEntityDefinition) entity;
    assertThat(textEntity.getFormat(), is(FormatType.YAML));
    assertThat(textEntity.getCompression(), is(BROTLI));
    assertThat(textEntity.getData(), is("key: value"));
  }

  @Test
  void textEntityWithFormatAndCompressionRoundTripSerialization() {
    TextEntityDefinition original =
        textEntity()
            .setFormat(JSON)
            .setCompression(CompressionType.DEFLATE)
            .setData("{\"key\":\"value\"}")
            .build();

    String json = Json.write(original);
    EntityDefinition deserialized = Json.read(json, EntityDefinition.class);

    assertEquals(original, deserialized);
  }

  @Test
  void binaryEntitySerializesAsObject() {
    byte[] data = {1, 2, 3, 4, 5};
    BinaryEntityDefinition entity = binaryEntity().setBody(data).build();

    String json = Json.write(entity);

    String expectedJson =
        """
                    {
                      "encoding" : "binary",
                      "data" : "%s"
                    }
                    """
            .formatted(Base64.getEncoder().encodeToString(data));

    assertThat(json, jsonEquals(expectedJson));
  }

  @Test
  void binaryEntityDeserializesFromObject() {
    byte[] data = {10, 20, 30, 40};
    String json =
        """
                    {
                      "encoding" : "binary",
                      "data" : "%s"
                    }
                    """
            .formatted(Base64.getEncoder().encodeToString(data));

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    BinaryEntityDefinition binaryEntity = (BinaryEntityDefinition) entity;
    assertThat(binaryEntity.getEncoding(), is(EncodingType.BINARY));
    assertThat(binaryEntity.getDataAsBytes(), is(data));
  }

  @Test
  void binaryEntityRoundTripSerialization() {
    byte[] data = {1, 2, 3, 4, 5, 6, 7, 8};
    BinaryEntityDefinition original = binaryEntity().setBody(data).build();

    String json = Json.write(original);
    EntityDefinition deserialized = Json.read(json, EntityDefinition.class);

    assertEquals(original, deserialized);
  }

  @Test
  void binaryEntityWithCompressionSerializesCorrectly() {
    byte[] data = {100, 101, 102, 103};
    BinaryEntityDefinition entity =
        binaryEntity().setCompression(CompressionType.GZIP).setBody(data).build();

    String json = Json.write(entity);

    String expectedJson =
        """
                    {
                      "encoding" : "binary",
                      "compression" : "gzip",
                      "data" : "%s"
                    }
                    """
            .formatted(Base64.getEncoder().encodeToString(data));

    assertThat(json, jsonEquals(expectedJson));
  }

  @Test
  void binaryEntityWithCompressionDeserializesCorrectly() {
    byte[] data = {50, 60, 70};
    String json =
        """
                    {
                      "encoding" : "binary",
                      "compression" : "deflate",
                      "data" : "%s"
                    }
                    """
            .formatted(Base64.getEncoder().encodeToString(data));

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    BinaryEntityDefinition binaryEntity = (BinaryEntityDefinition) entity;
    assertThat(binaryEntity.getEncoding(), is(EncodingType.BINARY));
    assertThat(binaryEntity.getCompression(), is(CompressionType.DEFLATE));
    assertThat(binaryEntity.getDataAsBytes(), is(data));
  }

  @Test
  void binaryEntityWithCompressionRoundTripSerialization() {
    byte[] data = {11, 22, 33, 44, 55};
    BinaryEntityDefinition original = binaryEntity().setCompression(BROTLI).setBody(data).build();

    String json = Json.write(original);
    EntityDefinition deserialized = Json.read(json, EntityDefinition.class);

    assertEquals(original, deserialized);
  }

  @Test
  void correctlyReportsWhetherDecompressionIsPossible() {
    byte[] data = {11, 22, 33, 44, 55};

    assertThat(
        binaryEntity().setCompression(BROTLI).setBody(data).build().isDecompressable(), is(false));

    assertThat(
        binaryEntity().setCompression(DEFLATE).setBody(data).build().isDecompressable(), is(false));

    assertThat(
        binaryEntity().setCompression(GZIP).setBody(data).build().isDecompressable(), is(true));

    assertThat(
        binaryEntity().setCompression(NONE).setBody(data).build().isDecompressable(), is(true));
  }

  @Test
  void decompressesBinaryGzip() {
    byte[] plain = {11, 22, 33, 44, 55};
    byte[] compressed = Gzip.gzip(plain);

    BinaryEntityDefinition original =
        binaryEntity().setCompression(GZIP).setBody(compressed).build();

    BinaryEntityDefinition decompressed = original.decompress();

    assertThat(decompressed.getDataAsBytes(), is(plain));
    assertThat(decompressed.getCompression(), is(NONE));
  }

  @Test
  void decompressesTextGzip() {
    String plain = "compress me";
    byte[] compressed = Gzip.gzip(plain);

    TextEntityDefinition original = textEntity().setCompression(GZIP).setData(compressed).build();

    TextEntityDefinition decompressed = original.decompress();

    assertThat(decompressed.getDataAsString(), is(plain));
    assertThat(decompressed.getCompression(), is(NONE));
  }

  @Test
  void throwsExceptionWhenAttemptingToDecompressesBinaryBrotli() {
    final byte[] body = {11};

    assertThrows(
        IllegalStateException.class,
        () -> binaryEntity().setBody(body).setCompression(BROTLI).build().decompress());
  }

  @Test
  void throwsExceptionWhenAttemptingToDecompressesTextDeflate() {
    assertThrows(
        IllegalStateException.class,
        () -> textEntity().setData("blah").setCompression(DEFLATE).build().decompress());
  }

  @Test
  void decompressingUncompressedTextDoesNothing() {
    String plain = "don't compress me";

    var original = textEntity().setCompression(NONE).setData(plain).build();

    var decompressed = original.decompress();

    assertThat(decompressed.getDataAsString(), is(plain));
    assertThat(decompressed.getCompression(), is(NONE));
  }

  @Test
  void decompressingUncompressedBinaryDoesNothing() {
    final byte[] plain = {11};

    var original = binaryEntity().setCompression(NONE).setBody(plain).build();

    var decompressed = original.decompress();

    assertThat(decompressed.getDataAsBytes(), is(plain));
    assertThat(decompressed.getCompression(), is(NONE));
  }

  @Test
  void detectsTextFormatWhenNotSpecified() {
    assertThat(textEntity().setData("\n{}  ").build().getFormat(), is(JSON));
    assertThat(textEntity().setData("  []").build().getFormat(), is(JSON));
    assertThat(textEntity().setData("  \n<things />").build().getFormat(), is(XML));

    String yaml =
        // language=yaml
        """
            root:
              myList:
                - one
                - two
                - three
            """;
    assertThat(textEntity().setData(yaml).build().getFormat(), is(YAML));

    String html =
        // language=html
        """
            <html>
              <body>
                <h1>Hello World</h1>
              </body>
            </html>
            """;
    assertThat(textEntity().setData(html).build().getFormat(), is(HTML));

    assertThat(textEntity().setData("just some prose").build().getFormat(), is(TEXT));
  }

  @Test
  void detectsTextFormatWhenNotSpecifiedDuringDeserialization() {
    var json =
        // language=json
        """
            {
              "data": "\\n{ \\"key\\": \\"value\\" }  "
            }
            """;

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    assertThat(entity.getFormat(), is(JSON));
  }

  @Test
  void serialisesSimpleStringEntityDefinitionAsString() {
    EntityDefinition entityDefinition = TextEntityDefinition.simple("simple text");
    String json = Json.write(entityDefinition);
    assertThat(json, is("\"simple text\""));
  }

  @Test
  void deserialisesStringToSimpleStringEntityDefinition() {
    EntityDefinition entityDefinition = Json.read("\"simple text\"", EntityDefinition.class);

    assertThat(entityDefinition, instanceOf(SimpleStringEntityDefinition.class));
  }

  @Test
  void acceptsJsonObjectAsData() {
    EntityDefinition entityDefinition =
        Json.read(
            // language=json
            """
        {
          "data": {
            "key": "value"
          }
        }
        """,
            EntityDefinition.class);

    assertThat(entityDefinition, instanceOf(JsonEntityDefinition.class));

    JsonEntityDefinition jsonEntity = (JsonEntityDefinition) entityDefinition;
    JsonNode dataAsJson = jsonEntity.getDataAsJson();
    assertThat(dataAsJson.get("key").textValue(), is("value"));

    assertThat(
        jsonEntity.getDataAsString(),
        jsonEquals(
            // language=json
            """
          {
            "key": "value"
          }"""));
  }

  @Test
  void serialisesJsonDataObject() {
    EntityDefinition jsonEntityDef = JsonEntityDefinition.json(Map.of("key", "value"));
    String json = Json.write(jsonEntityDef);
    assertThat(
        json,
        jsonEquals(
            // language=json
            """
        {
          "format": "json",
          "data": {
            "key": "value"
          }
        }
        """));
  }

  @Test
  void rejectsEntityWithBothDataAndFilePath() {
    assertThrows(
        IllegalArgumentException.class,
        () -> textEntity().setData("data").setFilePath("path").build());
  }

  @Test
  void rejectsEntityWithBothDataAndStoreRef() {
    assertThrows(
        IllegalArgumentException.class,
        () -> textEntity().setData("data").setDataStoreRef("store", "key").build());
  }

  @Test
  void rejectsEntityWithBothFilePathAndStoreRef() {
    assertThrows(
        IllegalArgumentException.class,
        () -> textEntity().setFilePath("data").setDataStoreRef("store", "key").build());
  }
}
