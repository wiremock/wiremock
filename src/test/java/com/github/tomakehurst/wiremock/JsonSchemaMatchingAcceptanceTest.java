/*
 * Copyright (C) 2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.file;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.jupiter.api.Test;

public class JsonSchemaMatchingAcceptanceTest extends AcceptanceTestBase {

  @Test
  void matchesStubWhenRequestBodyJsonValidatesAgainstSchema() {
    String schema = file("schema-validation/new-pet.schema.json");
    String json = file("schema-validation/new-pet.json");

    stubFor(
        post(urlPathEqualTo("/schema-match"))
            .withRequestBody(matchingJsonSchema(schema))
            .willReturn(ok()));

    WireMockResponse response = testClient.postJson("/schema-match", json);

    assertThat(response.statusCode(), is(200));
  }

  @Test
  void doesNotMatchStubWhenRequestBodyJsonDoesNotValidateAgainstSchema() {
    String schema = file("schema-validation/new-pet.schema.json");
    String json = file("schema-validation/new-pet.invalid.json");

    stubFor(
        post(urlPathEqualTo("/schema-match"))
            .withRequestBody(matchingJsonSchema(schema))
            .willReturn(ok()));

    WireMockResponse response = testClient.postJson("/schema-match", json);

    assertThat(response.statusCode(), is(404));
  }

  @Test
  void doesNotMatchStubWhenRequestBodyIsNotValidJson() {
    String schema = file("schema-validation/new-pet.schema.json");
    String json = file("schema-validation/new-pet.unparseable.json");

    stubFor(
        post(urlPathEqualTo("/schema-match"))
            .withRequestBody(matchingJsonSchema(schema))
            .willReturn(ok()));

    WireMockResponse response = testClient.postJson("/schema-match", json);

    assertThat(response.statusCode(), is(404));
  }

  // TODO: Diffs
}
