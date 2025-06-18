/*
 * Copyright (C) 2025 Thomas Akehurst
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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class WireMockStubMappingJsonSchemaRegressionTest {

  private static final String SCHEMA_PATH = "schemas/wiremock-stub-mapping.json";
  private static final String EXPECTED_SCHEMA_PATH =
      "schema-validation/expected-wiremock-stub-mapping-schema.json";

  @Test
  void schemaIsAJsonSchema() {
    JsonNode schemaJson = Json.node(loadResourceAsString(SCHEMA_PATH));
    assertNotNull(schemaJson, "Schema file not found: " + SCHEMA_PATH);

    SchemaValidatorsConfig schemaValidatorsConfig = SchemaValidatorsConfig.builder().build();
    SpecVersion.VersionFlag version =
        SpecVersion.VersionFlag.fromId(schemaJson.get("$schema").textValue()).orElseThrow();

    final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(version);

    JsonNode metaSchemaJson =
        Json.node(loadResourceAsString(URI.create(version.getId()).getPath().substring(1)));
    final JsonSchema metaSchema = schemaFactory.getSchema(metaSchemaJson, schemaValidatorsConfig);

    assertThat(metaSchema.validate(schemaJson), is(empty()));
  }

  @Test
  void schemaIsExpectedSchema() throws IOException {
    // Load the actual schema
    String actualSchema = loadResourceAsString(SCHEMA_PATH);
    assertNotNull(actualSchema, "Schema file not found: " + SCHEMA_PATH);

    // Load the expected schema
    String expectedSchema = loadResourceAsString(EXPECTED_SCHEMA_PATH);
    assertNotNull(expectedSchema, "Expected schema file not found: " + EXPECTED_SCHEMA_PATH);

    // Compare the schemas
    try {
      assertThat(actualSchema, jsonEquals(expectedSchema));
    } catch (AssertionError e) {
      Path expectedSchemaPath = Paths.get("src/test/resources", EXPECTED_SCHEMA_PATH);
      Files.write(expectedSchemaPath, actualSchema.getBytes(UTF_8));
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
}
