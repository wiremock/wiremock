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
package com.github.tomakehurst.wiremock.schema;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.tomakehurst.wiremock.common.Json;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

class WireMockMessageStubMappingJsonSchemaRegressionTest {

  private static final String SCHEMA_PATH = "schemas/wiremock-message-stub-mapping.json";
  private static final Path EXPECTED_SCHEMA_PATH =
      Path.of("schemas/wiremock-message-stub-mapping-or-mappings.json");

  @Test
  void schemaIsAJsonSchema() {
    JsonNode schemaJson = Json.node(loadResourceAsString(SCHEMA_PATH));
    assertNotNull(schemaJson, "Schema file not found: " + SCHEMA_PATH);

    SchemaRegistryConfig schemaValidatorsConfig = SchemaRegistryConfig.builder().build();
    Dialect version = Dialects.getDraft202012();

    final SchemaRegistry schemaFactory =
        SchemaRegistry.withDialect(
            version, builder -> builder.schemaRegistryConfig(schemaValidatorsConfig));

    JsonNode metaSchemaJson =
        Json.node(loadResourceAsString(URI.create(version.getId()).getPath().substring(1)));
    final Schema metaSchema = schemaFactory.getSchema(metaSchemaJson);

    assertThat(metaSchema.validate(schemaJson), is(empty()));
  }

  @Test
  void schemaIsExpectedSchema() throws IOException {
    String actualSchema = loadResourceAsString(SCHEMA_PATH);
    assertNotNull(actualSchema, "Schema file not found: " + SCHEMA_PATH);

    String expectedSchema = loadFileAsString(EXPECTED_SCHEMA_PATH);
    assertNotNull(expectedSchema, "Expected schema file not found: " + EXPECTED_SCHEMA_PATH);

    try {
      assertThat(actualSchema, jsonEquals(expectedSchema));
    } catch (AssertionError e) {
      Files.writeString(EXPECTED_SCHEMA_PATH, actualSchema);
      System.err.println(
          "The regression test failing may just mean that your intended changes need to be committed."
              + System.lineSeparator()
              + EXPECTED_SCHEMA_PATH
              + " has been updated - compare it with the previous version, and if you are happy with the changes commit them.");
      throw e;
    }
  }

  private String loadResourceAsString(String resourcePath) {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      if (is == null) {
        return null;
      }
      return new String(is.readAllBytes(), UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load resource: " + resourcePath, e);
    }
  }

  private String loadFileAsString(Path filePath) {
    try {
      return Files.readString(filePath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load file: " + filePath, e);
    }
  }
}
