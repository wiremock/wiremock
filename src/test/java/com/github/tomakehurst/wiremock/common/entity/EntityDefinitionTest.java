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
import static com.github.tomakehurst.wiremock.client.WireMock.entity;
import static com.github.tomakehurst.wiremock.common.Strings.bytesFromString;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.BROTLI;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.DEFLATE;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.GZIP;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.NONE;
import static com.github.tomakehurst.wiremock.common.entity.EntityDefinition.DEFAULT_CHARSET;
import static com.github.tomakehurst.wiremock.common.entity.Format.BINARY;
import static com.github.tomakehurst.wiremock.common.entity.Format.HTML;
import static com.github.tomakehurst.wiremock.common.entity.Format.JSON;
import static com.github.tomakehurst.wiremock.common.entity.Format.TEXT;
import static com.github.tomakehurst.wiremock.common.entity.Format.XML;
import static com.github.tomakehurst.wiremock.common.entity.Format.YAML;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class EntityDefinitionTest {

  @Test
  void v3StyleTextEntitySerializesAsString() {
    EntityDefinition entity = EntityDefinition.simple("simple text");

    String json = Json.write(entity);

    assertThat(json, is("\"simple text\""));
  }

  @Test
  void v3StyleTextEntityDeserializesFromString() {
    String json = "\"simple text\"";

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    assertThat(entity, is(EntityDefinition.simple("simple text")));
  }

  @Test
  void v3StyleTextEntityRoundTripSerialization() {
    EntityDefinition original = EntityDefinition.simple("round trip text");

    String json = Json.write(original);
    EntityDefinition deserialized = Json.read(json, EntityDefinition.class);

    assertEquals(original, deserialized);
  }

  @Test
  void normalTextEntitySerializesAsObject() {
    EntityDefinition entity = entity().setData("test data").build();

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

    assertThat(entity.getData(), is("test data"));
  }

  @Test
  void normalTextEntityRoundTripSerialization() {
    EntityDefinition original = entity().setData("round trip data").build();

    String json = Json.write(original);
    EntityDefinition deserialized = Json.read(json, EntityDefinition.class);

    assertEquals(original, deserialized);
  }

  @Test
  void textEntityWithFormatAndCompressionSerializesCorrectly() {
    String plain = "<root>data</root>";
    byte[] gzipped = Gzip.gzip(plain);
    String base64 = Encoding.encodeBase64(gzipped);

    EntityDefinition entity = entity().setFormat(XML).setCompression(GZIP).setData(gzipped).build();

    String json = Json.write(entity);

    String expectedJson =
        """
                    {
                      "format" : "xml",
                      "compression" : "gzip",
                      "base64Data" : "%s"
                    }
                    """
            .formatted(base64);

    assertThat(json, jsonEquals(expectedJson));
  }

  @Test
  void textEntityWithFormatAndCompressionDeserializesCorrectly() {
    String brotliBase64 =
        "ocgLACDwPH/zPaFjj1rrghQGtvef661+803Q1CAFJf58ZpJNkWShWbeuiEmI7HJB0oSH+7HrNoH/RIOj/B3loo/bex/rcev1MkWvQFEVOtUF9fnxDNqtXFOWD5kGQZSKz4t75WMjKMfr/z8PNX0IkWUZkoGYWxoQPYRtUWd5jPq6dANdoECcxrBq0I/P8Kr02v0cICWpkGZ8O+K7bP8dOLKLQZIZAw==";
    String json =
        """
                    {
                      "format" : "yaml",
                      "compression" : "brotli",
                      "base64Data" : "%s"
                    }
                    """
            .formatted(brotliBase64);

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    assertThat(entity.getFormat(), is(Format.YAML));
    assertThat(entity.isCompressed(), is(true));
    assertThat(entity.getCompression(), is(BROTLI));
    assertThat(entity.getDataAsBytes(), is(Encoding.decodeBase64(brotliBase64)));
  }

  @Test
  void textEntityWithFormatAndCompressionRoundTripSerialization() {
    EntityDefinition original =
        entity()
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
    EntityDefinition entity = binaryEntity().setData(data).build();

    String json = Json.write(entity);

    String expectedJson =
        """
                    {
                      "base64Data" : "%s"
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
                      "format" : "binary",
                      "base64Data" : "%s"
                    }
                    """
            .formatted(Base64.getEncoder().encodeToString(data));

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    assertThat(entity.getFormat(), is(BINARY));
    assertThat(entity.getDataAsBytes(), is(data));
  }

  @Test
  void binaryEntityRoundTripSerialization() {
    byte[] data = {1, 2, 3, 4, 5, 6, 7, 8};
    EntityDefinition original = binaryEntity().setData(data).build();

    String json = Json.write(original);
    EntityDefinition deserialized = Json.read(json, EntityDefinition.class);

    assertEquals(original, deserialized);
  }

  @Test
  void binaryEntityWithCompressionSerializesCorrectly() {
    byte[] data = {100, 101, 102, 103};
    EntityDefinition entity = binaryEntity().setCompression(GZIP).setData(data).build();

    String json = Json.write(entity);

    String expectedJson =
        """
                    {
                      "compression" : "gzip",
                      "base64Data" : "%s"
                    }
                    """
            .formatted(Base64.getEncoder().encodeToString(data));

    assertThat(json, jsonEquals(expectedJson));
  }

  @Test
  void compressedEntitySerialisesAsBase64() {
    String plain = "to be compressed";
    byte[] gzipped = Gzip.gzip(plain);
    String base64 = Encoding.encodeBase64(gzipped);

    EntityDefinition entity = entity().setCompression(GZIP).setData(gzipped).build();

    String expectedJson =
        // language=json
        """
            {
              "compression": "gzip",
              "base64Data": "%s"
            }
            """
            .formatted(base64);

    assertThat(Json.write(entity), jsonEquals(expectedJson));
  }

  @Test
  void base64EncodedCompressedDeserialisesCorrectly() {
    String plain = "to be compressed";
    byte[] gzipped = Gzip.gzip(plain);
    String base64 = Encoding.encodeBase64(gzipped);

    String json =
        // language=json
        """
            {
              "compression": "gzip",
              "base64Data": "%s"
            }
            """
            .formatted(base64);

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    assertThat(entity.getCompression(), is(GZIP));
    assertThat(entity.getDataAsBytes(), is(gzipped));
  }

  @Test
  void binaryEntityWithCompressionDeserializesCorrectly() {
    byte[] data = {50, 60, 70};
    String json =
        """
                    {
                      "format" : "binary",
                      "compression" : "deflate",
                      "base64Data" : "%s"
                    }
                    """
            .formatted(Base64.getEncoder().encodeToString(data));

    EntityDefinition entity = Json.read(json, EntityDefinition.class);

    assertThat(entity.getFormat(), is(BINARY));
    assertThat(entity.getCompression(), is(CompressionType.DEFLATE));
    assertThat(entity.getDataAsBytes(), is(data));
  }

  @Test
  void binaryEntityWithCompressionRoundTripSerialization() {
    byte[] data = {11, 22, 33, 44, 55};
    EntityDefinition original = binaryEntity().setCompression(BROTLI).setData(data).build();

    String json = Json.write(original);
    EntityDefinition deserialized = Json.read(json, EntityDefinition.class);

    assertEquals(original, deserialized);
  }

  @Test
  void correctlyReportsWhetherDecompressionIsPossible() {
    byte[] data = {11, 22, 33, 44, 55};

    assertThat(
        binaryEntity().setCompression(BROTLI).setData(data).build().isDecompressable(), is(false));

    assertThat(
        binaryEntity().setCompression(DEFLATE).setData(data).build().isDecompressable(), is(false));

    assertThat(
        binaryEntity().setCompression(GZIP).setData(data).build().isDecompressable(), is(true));

    assertThat(
        binaryEntity().setCompression(NONE).setData(data).build().isDecompressable(), is(true));
  }

  @Test
  void decompressesBinaryGzip() {
    byte[] plain = {11, 22, 33, 44, 55};
    byte[] compressed = Gzip.gzip(plain);

    EntityDefinition original = binaryEntity().setCompression(GZIP).setData(compressed).build();

    EntityDefinition decompressed = original.decompress();

    assertThat(decompressed.getDataAsBytes(), is(plain));
    assertThat(decompressed.getCompression(), is(NONE));
  }

  @Test
  void decompressesTextGzip() {
    String plain = "compress me";
    byte[] compressed = Gzip.gzip(plain);

    EntityDefinition original =
        entity().setCompression(GZIP).setFormat(TEXT).setData(compressed).build();

    EntityDefinition decompressed = original.decompress();

    assertThat(decompressed.getCompression(), is(NONE));
    assertThat(decompressed.getDataAsString(), is(plain));
  }

  @Test
  void throwsExceptionWhenAttemptingToDecompressesBinaryBrotli() {
    final byte[] body = {11};

    assertThrows(
        IllegalStateException.class,
        () -> binaryEntity().setData(body).setCompression(BROTLI).build().decompress());
  }

  @Test
  void throwsExceptionWhenAttemptingToDecompressesTextDeflate() {
    assertThrows(
        IllegalStateException.class,
        () -> entity().setData("blah").setCompression(DEFLATE).build().decompress());
  }

  @Test
  void decompressingUncompressedTextDoesNothing() {
    String plain = "don't compress me";

    var original = entity().setCompression(NONE).setData(plain).build();

    var decompressed = original.decompress();

    assertThat(decompressed.getDataAsString(), is(plain));
    assertThat(decompressed.getCompression(), is(NONE));
  }

  @Test
  void decompressingUncompressedBinaryDoesNothing() {
    final byte[] plain = {11};

    var original = binaryEntity().setCompression(NONE).setData(plain).build();

    var decompressed = original.decompress();

    assertThat(decompressed.getDataAsBytes(), is(plain));
    assertThat(decompressed.getCompression(), is(NONE));
  }

  @Test
  void guessesGzipCompressionWhenNotExplicit() {
    byte[] gzipped = Gzip.gzip("compressed");
    EntityDefinition entity = entity().setData(gzipped).build();
    assertThat(entity.getCompression(), is(GZIP));
  }

  @Test
  void detectsTextFormatWhenNotSpecified() {
    assertThat(entity().setData("\n{}  ").build().getFormat(), is(JSON));
    assertThat(entity().setData("  []").build().getFormat(), is(JSON));
    assertThat(entity().setData("  \n<things />").build().getFormat(), is(XML));

    String yaml =
        // language=yaml
        """
            root:
              myList:
                - one
                - two
                - three
            """;
    assertThat(entity().setData(yaml).build().getFormat(), is(YAML));

    String html =
        // language=html
        """
            <html lang="">
              <body>
                <h1>Hello World</h1>
              </body>
            </html>
            """;
    assertThat(entity().setData(html).build().getFormat(), is(HTML));

    assertThat(entity().setData("just some prose").build().getFormat(), is(TEXT));
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
    EntityDefinition entityDefinition = EntityDefinition.simple("simple text");
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
  void serialisesDataStoreAndRefBinaryEntityCorrectly() {
    EntityDefinition entityDefinition = binaryEntity().setDataStoreRef("store", "key").build();

    String json = Json.write(entityDefinition);
    assertThat(
        json,
        jsonEquals(
            // language=json
            """
                    {
                      "format": "binary",
                      "dataStore": "store",
                      "dataRef": "key"
                    }
                    """));
  }

  @Test
  void serialisesDataStoreAndRefTextEntityCorrectly() {
    EntityDefinition entityDefinition =
        entity().setFormat(XML).setDataStoreRef("store", "key").build();

    String json = Json.write(entityDefinition);
    assertThat(
        json,
        jsonEquals(
            // language=json
            """
                    {
                      "format": "xml",
                      "dataStore": "store",
                      "dataRef": "key"
                    }
                    """));
  }

  @Test
  void rejectsEntityWithBothDataAndFilePath() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new EntityDefinition(
                null, TEXT, null, null, bytesFromString("data", DEFAULT_CHARSET), "path"));
  }

  @Test
  void rejectsEntityWithBothBase64DataAndFilePath() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new EntityDefinition(null, BINARY, null, null, Encoding.decodeBase64("AQID"), "path"));
  }

  @Test
  void rejectsEntityWithBothDataAndStoreRef() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new EntityDefinition(
                null,
                TEXT,
                null,
                new DataStoreRef("store", "key"),
                bytesFromString("data", DEFAULT_CHARSET),
                null));
  }

  @Test
  void rejectsEntityWithBothBase64DataAndStoreRef() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new EntityDefinition(
                null,
                BINARY,
                null,
                new DataStoreRef("store", "key"),
                Encoding.decodeBase64("AQID"),
                null));
  }

  @Test
  void rejectsEntityWithBothFilePathAndStoreRef() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new EntityDefinition(null, null, null, new DataStoreRef("store", "key"), null, "path"));
  }

  @Test
  void builderClearsFilePathAndStoreRefWhenSettingData() {
    EntityDefinition entity =
        entity().setFilePath("path").setDataStoreRef("store", "key").setData("data").build();

    assertThat(entity.getData(), is("data"));
    assertThat(entity.getFilePath(), nullValue());
    assertThat(entity.getDataStoreRef(), nullValue());
  }

  @Test
  void builderClearsDataAndStoreRefWhenSettingFilePath() {
    EntityDefinition entity =
        entity().setData("data").setDataStoreRef("store", "key").setFilePath("path").build();

    assertThat(entity.getFilePath(), is("path"));
    assertThat(entity.getData(), nullValue());
    assertThat(entity.getDataStoreRef(), nullValue());
  }

  @Test
  void builderClearsDataAndFilePathWhenSettingStoreRef() {
    EntityDefinition entity =
        entity().setData("data").setFilePath("path").setDataStoreRef("store", "key").build();

    assertThat(entity.getDataStoreRef(), is(new DataStoreRef("store", "key")));
    assertThat(entity.getData(), nullValue());
    assertThat(entity.getFilePath(), nullValue());
  }
}
