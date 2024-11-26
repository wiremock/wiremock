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

public class FormatJsonHelperTest extends HandlebarsHelperTestBase {

  static String compactJson = "{\"foo\":true,\"bar\":{\"baz\":false}}";

  static String prettyJson = "{\n  \"foo\" : true,\n  \"bar\" : {\n    \"baz\" : false\n  }\n}";
  static String prettyJsonWindows =
      "{\r\n  \"foo\" : true,\r\n  \"bar\" : {\r\n    \"baz\" : false\r\n  }\r\n}";

  @Test
  @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void formatJsonDefaultsToPrettyFormatWhenNoFormatSpecified() {
    String responseTemplate = "{{#formatJson}} " + compactJson + " {{/formatJson}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is(prettyJson));
  }

  @Test
  @EnabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void formatJsonDefaultsToPrettyFormatWhenNoFormatSpecifiedWindows() {
    String responseTemplate = "{{#formatJson}} " + compactJson + " {{/formatJson}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is(prettyJsonWindows));
  }

  @Test
  @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void formatJsonPrettyFormatReturnsJsonPrettyPrinted() {
    String responseTemplate = "{{#formatJson format='pretty'}} " + compactJson + " {{/formatJson}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is(prettyJson));
  }

  @Test
  @EnabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void formatJsonPrettyFormatReturnsJsonPrettyPrintedWindows() {
    String responseTemplate = "{{#formatJson format='pretty'}} " + compactJson + " {{/formatJson}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is(prettyJsonWindows));
  }

  @Test
  void formatJsonCompactFormatReturnsJsonInCompactFormat() {
    String responseTemplate = "{{#formatJson format='compact'}} " + prettyJson + " {{/formatJson}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is(compactJson));
  }

  @Test
  void formatJsonFormatsJsonInAVariable() {
    String responseTemplate =
        "{{~#assign 'someJson'~}} "
            + prettyJson
            + " {{/assign}}{{formatJson someJson format='compact'}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is(compactJson));
  }

  @Test
  void anInvalidFormatFieldResultsInAnError() {
    String responseTemplate = "{{#formatJson format='foo'}} " + compactJson + " {{/formatJson}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is("[ERROR: formatJson: format [foo] should be one of [pretty, compact]]"));
  }

  @Test
  void formatJsonReturnsAnErrorWhenJsonIsInvalid() {
    String responseTemplate = "{{#formatJson format=\"compact\"}} {\"foo\":true,} {{/formatJson}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is("[ERROR: There was an error parsing the json. Please make sure the json is valid]"));
  }

  @Test
  void emptyJsonPassedIntoTheFormatJsonHelper() {
    String responseTemplate = "{{#formatJson}}{{/formatJson}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is(
            "[ERROR: formatJson should take a block of JSON to format or a single parameter of type String]"));
  }

  @Test
  void noContentPassedIntoTheFormatJsonHelper() {
    String responseTemplate = "{{formatJson}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is(
            "[ERROR: formatJson should take a block of JSON to format or a single parameter of type String]"));
  }

  @Test
  void nullVariablePassedToTheFormatJsonHelper() {
    String responseTemplate = "{{formatJson nullVariable}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is(
            "[ERROR: formatJson should take a block of JSON to format or a single parameter of type String]"));
  }

  @Test
  void whitespacePassedIntoTheFormatJsonHelper() {
    String responseTemplate = "{{#formatJson}}                  {{/formatJson}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is(
            "[ERROR: formatJson should take a block of JSON to format or a single parameter of type String]"));
  }

  @Test
  void invalidFormatType() {
    String responseTemplate = "{{#formatJson format=1}} " + prettyJson + " {{/formatJson}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is(
            "[ERROR: formatJson: format [1] of type [java.lang.Integer should be a Format or a String and one of [pretty, compact]]]"));
  }
}
