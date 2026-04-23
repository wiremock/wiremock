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

public class FormatXmlHelperTest extends HandlebarsHelperTestBase {

  @Test
  @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void formatXmlHelperFormatsXmlPrettilyByDefault() {
    String responseTemplate =
        "{{#formatXml}}\n<foo><bar\n    >wh</bar></foo\n    >\n{{/formatXml}}";
    ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("<foo>\n  <bar>wh</bar>\n</foo>\n"));
  }

  @Test
  @EnabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void formatXmlHelperFormatsXmlPrettilyByDefaultWindows() {
    String responseTemplate =
        "{{#formatXml}}\n<foo><bar\n    >wh</bar></foo\n    >\n{{/formatXml}}";
    ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("<foo>\r\n  <bar>wh</bar>\r\n</foo>\r\n"));
  }

  @Test
  @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void formatXmlHelperFormatsXmlInAVariable() {
    String responseTemplate =
        "{{~#assign 'someXml'~}}\n"
            + "<foo><bar\n"
            + "    >wh</bar></foo\n"
            + "    >\n"
            + "{{/assign}}\n"
            + "{{~formatXml someXml format='pretty'~}}";
    ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("<foo>\n  <bar>wh</bar>\n</foo>\n"));
  }

  @Test
  @EnabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void formatXmlHelperFormatsXmlInAVariableWindows() {
    String responseTemplate =
        "{{~#assign 'someXml'~}}\n"
            + "<foo><bar\n"
            + "    >wh</bar></foo\n"
            + "    >\n"
            + "{{/assign}}\n"
            + "{{~formatXml someXml format='pretty'~}}";
    ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("<foo>\r\n  <bar>wh</bar>\r\n</foo>\r\n"));
  }

  @Test
  @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void formatXmlHelperFormatsXmlPrettily() {
    String responseTemplate =
        "{{#formatXml format='pretty'}}\n"
            + "  <foo><bar\n"
            + ">wh</bar></foo\n"
            + ">\n"
            + "{{/formatXml}}";
    ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("<foo>\n  <bar>wh</bar>\n</foo>\n"));
  }

  @Test
  @EnabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void formatXmlHelperFormatsXmlPrettilyWindows() {
    String responseTemplate =
        "{{#formatXml format='pretty'}}\n"
            + "  <foo><bar\n"
            + ">wh</bar></foo\n"
            + ">\n"
            + "{{/formatXml}}";
    ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("<foo>\r\n  <bar>wh</bar>\r\n</foo>\r\n"));
  }

  @Test
  void formatXmlHelperGivesGoodErrorOnUnknownFormat() {
    String responseTemplate =
        "{{#formatXml format='traditional'}}\n"
            + "<foo><bar\n"
            + "    >wh</bar></foo\n"
            + "    >\n"
            + "{{/formatXml}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is("[ERROR: formatXml: format [traditional] should be one of [pretty, compact]]"));
  }

  @Test
  void formatXmlHelperGivesGoodErrorOnNoInput() {
    String responseTemplate = "{{formatXml}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is(
            "[ERROR: formatXml should take a block of XML to format or a single parameter of type String]"));
  }

  @Test
  void formatXmlHelperGivesGoodErrorOnInvalidXml() {
    String responseTemplate = "{{#formatXml}}<foo>Not well formed!</bar>{{/formatXml}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("[ERROR: Input is not valid XML]"));
  }

  @Test
  void formatXmlHelperFormatsXmlCompact() {
    String responseTemplate =
        "{{#formatXml format='compact'}}\n"
            + "<foo><bar\n"
            + "    >wh</bar></foo\n"
            + "    >\n"
            + "{{/formatXml}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("<foo><bar>wh</bar></foo>"));
  }

  @Test
  void formatXmlHelperFormatsPrettyXmlCompact() {
    String responseTemplate =
        "{{#formatXml format='compact'}}\n"
            + "<foo>\n"
            + "  <bar>wh</bar>\n"
            + "</foo>\n"
            + "{{/formatXml}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("<foo><bar>wh</bar></foo>"));
  }

  @Test
  void invalidFormatType() {
    String responseTemplate =
        "{{#formatXml format=1}}\n"
            + "<foo>\n"
            + "  <bar>wh</bar>\n"
            + "</foo>\n"
            + "{{/formatXml}}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is(
            "[ERROR: formatXml: format [1] of type [java.lang.Integer should be a Format or a String and one of [pretty, compact]]]"));
  }
}
