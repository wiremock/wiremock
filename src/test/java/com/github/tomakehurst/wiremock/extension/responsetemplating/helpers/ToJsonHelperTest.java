/*
 * Copyright (C) 2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

public class ToJsonHelperTest extends HandlebarsHelperTestBase {

  @Test
  void convertArrayToJson() {
    String responseTemplate = "{{ toJson (array 1 2 3) }}";
    ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("[ 1, 2, 3 ]"));
  }

  @Test
  void convertNullToJson() {
    String responseTemplate = "{{ toJson null }}";
    ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is(""));
  }

  @Test
  void convertStringToJson() {
    String responseTemplate = "{{ toJson 'null' }}";
    ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("\"null\""));
  }

  @Test
  @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void convertMapToJson() {
    String responseTemplate = "{{ toJson request.headers }}";
    ResponseDefinition responseDefinition =
        transform(
            transformer,
            mockRequest().header("Authorization", "whatever").header("Content-Type", "text/plain"),
            aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is("{\n  \"Authorization\" : \"whatever\",\n  \"Content-Type\" : \"text/plain\"\n}"));
  }

  @Test
  @EnabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void convertMapToJsonWindows() {
    String responseTemplate = "{{ toJson request.headers }}";
    ResponseDefinition responseDefinition =
        transform(
            transformer,
            mockRequest().header("Authorization", "whatever").header("Content-Type", "text/plain"),
            aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is("{\r\n  \"Authorization\" : \"whatever\",\r\n  \"Content-Type\" : \"text/plain\"\r\n}"));
  }

  @Test
  void convertBooleanToJson() {
    String responseTemplate = "{{ toJson true }}";
    ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("true"));
  }

  @Test
  void convertNumberToJson() {
    String responseTemplate = "{{ toJson 123 }}";
    ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("123"));
  }
}
