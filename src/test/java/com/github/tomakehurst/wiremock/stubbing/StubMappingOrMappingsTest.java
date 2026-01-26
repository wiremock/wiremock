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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.tomakehurst.wiremock.common.Json;
import org.junit.jupiter.api.Test;

public class StubMappingOrMappingsTest {

  @Test
  public void jacksonEmitsCorrectPointerWhenDeserializingCollection() {
    var stubMappingsJson =
        """
        {
          "mappings": [
            {
              "request": {
                "method": "GET"
              }
            },
            {
              "response": {
                "status": "not a number"
              }
            }
          ]
        }
        """;
    MismatchedInputException mismatchedInputException =
        assertThrows(
            MismatchedInputException.class,
            () -> Json.getObjectMapper().readValue(stubMappingsJson, StubMappingCollection.class));
    assertThat(
        ((JsonParser) mismatchedInputException.getProcessor())
            .getParsingContext()
            .pathAsPointer()
            .toString(),
        is("/mappings/1/response/status"));
  }

  @Test
  public void jacksonEmitsCorrectPointerWhenDeserializingMapping() {
    var stubMappingJson =
        """
        {
          "response": {
            "status": "not a number"
          }
        }
        """;
    MismatchedInputException mismatchedInputException =
        assertThrows(
            MismatchedInputException.class,
            () -> Json.getObjectMapper().readValue(stubMappingJson, StubMapping.class));
    assertThat(
        ((JsonParser) mismatchedInputException.getProcessor())
            .getParsingContext()
            .pathAsPointer()
            .toString(),
        is("/response/status"));
  }
}
