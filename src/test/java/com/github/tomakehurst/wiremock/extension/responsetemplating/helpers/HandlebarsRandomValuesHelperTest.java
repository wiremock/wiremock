/*
 * Copyright (C) 2018-2023 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.testsupport.NoFileSource.noFileSource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.testsupport.WireMatchers;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HandlebarsRandomValuesHelperTest {

  private HandlebarsRandomValuesHelper helper;
  private ResponseTemplateTransformer transformer;

  @BeforeEach
  public void init() {
    helper = new HandlebarsRandomValuesHelper();
    transformer = new ResponseTemplateTransformer(true);

    LocalNotifier.set(new ConsoleNotifier(true));
  }

  @Test
  void generatesRandomAlphaNumericOfSpecifiedLength() throws Exception {
    ImmutableMap<String, Object> optionsHash = ImmutableMap.of("length", 36);

    String output = render(optionsHash);

    assertThat(output.length(), is(36));
    assertThat(output, WireMatchers.matches("^[a-z0-9]+$"));
  }

  @Test
  void generatesUppercaseRandomAlphaNumericOfSpecifiedLength() throws Exception {
    ImmutableMap<String, Object> optionsHash = ImmutableMap.of("length", 36, "uppercase", true);

    String output = render(optionsHash);

    assertThat(output.length(), is(36));
    assertThat(output, WireMatchers.matches("^[A-Z0-9]+$"));
  }

  @Test
  void generatesRandomAlphabeticOfSpecifiedLength() throws Exception {
    ImmutableMap<String, Object> optionsHash =
        ImmutableMap.of("length", 43, "type", "ALPHABETIC", "uppercase", true);

    String output = render(optionsHash);

    assertThat(output.length(), is(43));
    assertThat(output, WireMatchers.matches("^[A-Z]+$"));
  }

  @Test
  void generatesRandomNumericOfSpecifiedLength() throws Exception {
    ImmutableMap<String, Object> optionsHash = ImmutableMap.of("length", 55, "type", "NUMERIC");

    String output = render(optionsHash);

    assertThat(output.length(), is(55));
    assertThat(output, WireMatchers.matches("^[0-9]+$"));
  }

  @Test
  void generatesRandomStringOfSpecifiedLength() throws Exception {
    ImmutableMap<String, Object> optionsHash =
        ImmutableMap.of("length", 67, "type", "ALPHANUMERIC_AND_SYMBOLS");

    String output = render(optionsHash);

    assertThat(output.length(), is(67));
    assertThat(output, WireMatchers.matches("^.+$"));
  }

  @Test
  void generatesRandomHexadecimalOfSpecifiedLength() throws Exception {
    ImmutableMap<String, Object> optionsHash = ImmutableMap.of("length", 64, "type", "HEXADECIMAL");

    String output = render(optionsHash);

    assertThat(output.length(), is(64));
    assertThat(output, WireMatchers.matches("^[0-9a-f]+$"));
  }

  @Test
  void randomValuesCanBeAssignedToVariables() {
    final ResponseDefinition responseDefinition =
        this.transformer.transform(
            mockRequest().url("/random-value"),
            aResponse()
                .withBody(
                    "{{#assign 'paymentId'}}{{randomValue length=20 type='ALPHANUMERIC' uppercase=true}}{{/assign}}\n"
                        + "{{paymentId}}\n"
                        + "{{paymentId}}")
                .build(),
            noFileSource(),
            Parameters.empty());

    String[] bodyLines = responseDefinition.getBody().trim().split("\n");
    assertThat(bodyLines[0], is(bodyLines[1]));
    assertThat(bodyLines[0].length(), is(20));
  }

  @Test
  void generatesRandomUUID() throws Exception {
    ImmutableMap<String, Object> optionsHash = ImmutableMap.of("type", "UUID");

    String output = render(optionsHash);

    assertThat(output.length(), is(36));
    assertThat(output, WireMatchers.matches("^[a-z0-9\\-]+$"));
  }

  private String render(ImmutableMap<String, Object> optionsHash) throws IOException {
    return helper
        .apply(null, new Options.Builder(null, null, null, null, null).setHash(optionsHash).build())
        .toString();
  }
}
