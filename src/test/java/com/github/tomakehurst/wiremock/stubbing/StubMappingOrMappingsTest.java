/*
 * Copyright (C) 2018-2026 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Json;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;

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
            () -> Json.getJsonMapper().readValue(stubMappingsJson, StubMappingCollection.class));
    assertThat(
        mismatchedInputException.getPath().stream()
            .map(
                reference -> {
                  if (reference.getIndex() == -1) {
                    return reference.getPropertyName();
                  } else {
                    return String.valueOf(reference.getIndex());
                  }
                })
            .collect(Collectors.joining("/")),
        is("mappings/1/response/status"));
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
            () -> Json.getJsonMapper().readValue(stubMappingJson, StubMapping.class));
    assertThat(
        mismatchedInputException.getPath().stream()
            .map(JacksonException.Reference::getPropertyName)
            .collect(Collectors.joining("/")),
        is("response/status"));
  }
}
