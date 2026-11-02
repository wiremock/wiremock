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
package com.github.tomakehurst.wiremock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.resource.ClasspathResourceLoader;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ResponseDefinitionSchemaValidityTest {

  static Schema schema;

  @BeforeAll
  static void init() {
    SchemaRegistry schemaRegistry =
        SchemaRegistry.withDefaultDialect(
            WireMock.JsonSchemaVersion.V202012.toVersionFlag(),
            builder ->
                builder.resourceLoaders(
                    loaders -> loaders.add(ClasspathResourceLoader.getInstance())));

    schema =
        schemaRegistry.getSchema(
            SchemaLocation.of("classpath:/swagger/schemas/response-definition.yaml"));
  }

  @Test
  void headersAcceptStringValues() {
    String json = "{\"status\": 200, \"headers\": { \"Content-Type\": \"application/json\" }}";
    assertThat(validate(json), empty());
  }

  @Test
  void headersAcceptArrayValues() {
    String json = "{\"status\": 200, \"headers\": { \"Set-Cookie\": [\"a=b\", \"c=d\"] }}";
    assertThat(validate(json), empty());
  }

  @Test
  void headersRejectNonStringArrayItems() {
    String json = "{\"status\": 200, \"headers\": { \"Set-Cookie\": [1, 2] }}";
    assertThat(validate(json), Matchers.not(empty()));
  }

  private static List<Error> validate(String json) {
    return schema.validate(json, InputFormat.JSON);
  }
}
