/*
 * Copyright (C) 2018-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.stubbing;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.github.tomakehurst.wiremock.common.Json;
import org.junit.jupiter.api.Test;

public class StubMappingTest {

  @Test
  public void excludesInsertionIndexFromPublicView() {
    StubMapping stub = get("/saveable").willReturn(ok()).build();

    String json = Json.write(stub);
    System.out.println(json);

    assertThat(json, not(containsString("insertionIndex")));
  }

  @Test
  public void includedInsertionIndexInPrivateView() {
    StubMapping stub = get("/saveable").willReturn(ok()).build();

    String json = Json.writePrivate(stub);
    System.out.println(json);

    assertThat(json, containsString("insertionIndex"));
  }

  @Test
  public void deserialisesInsertionIndex() {
    String json =
        // language=json
        "{\n"
            + "    \"request\": {\n"
            + "        \"method\": \"ANY\",\n"
            + "        \"url\": \"/\"\n"
            + "    },\n"
            + "    \"response\": {\n"
            + "        \"status\": 200\n"
            + "    },\n"
            + "    \"insertionIndex\": 42\n"
            + "}";

    StubMapping stub = Json.read(json, StubMapping.class);

    assertThat(stub.getInsertionIndex(), is(42L));
  }

  @Test
  public void ignoresUuidProperty() {
    String json =
        // language=json
        """
            {
              "id": "edf19376-0e08-4b27-8632-fb7852c9e62d",
              "request": {
                "url": "/",
                "method": "GET"
              },

              "response": {
                "status": 200
              },

              "uuid": "07150a3a-47ea-4182-9792-c49eb77b862e"
            }
            """;

    StubMapping stub = assertDoesNotThrow(() -> Json.read(json, StubMapping.class));

    assertThat(stub.getId().toString(), is("edf19376-0e08-4b27-8632-fb7852c9e62d"));
  }
}
