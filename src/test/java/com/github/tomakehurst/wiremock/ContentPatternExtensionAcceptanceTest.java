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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.ContentPatternExtension;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ContentPatternExtensionAcceptanceTest {

  @SuppressWarnings("unchecked")
  @RegisterExtension
  public static WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(
              options()
                  .dynamicPort()
                  .extensions(MyContentPatternExtension.class)
                  .usingFilesUnderClasspath("content-pattern-extension"))
          .build();

  public static WireMockTestClient client;

  @BeforeAll
  public static void beforeAll() {
    client = new WireMockTestClient(wm.getPort());
  }

  @Test
  public void customBodyMatcherWithRequestBody() {
    wm.stubFor(
        post("/stubFor/single").withRequestBody(new MyContentPatternExtension()).willReturn(ok()));
    assertThat(
        client.postWithBody("/stubFor/single", "myContent", "text/plain", "UTF-8").statusCode(),
        is(200));
  }

  @Test
  public void customBodyMatcherWithRequestBodyCombinedWithStandardMatchers() {
    wm.stubFor(
        post("/stubFor/multiple")
            .withRequestBody(new MyContentPatternExtension())
            .withRequestBody(containing("my"))
            .willReturn(ok()));
    assertThat(
        client.postWithBody("/stubFor/multiple", "myContent", "text/plain", "UTF-8").statusCode(),
        is(200));
  }

  @Test
  public void customBodyMatcherMappings() {
    assertThat(
        client.postWithBody("/mappings/single", "myContent", "text/plain", "UTF-8").statusCode(),
        is(200));
  }

  @Test
  public void customBodyMatcherMappingsCombinedWithStandardMatchers() {
    assertThat(
        client.postWithBody("/mappings/multiple", "myContent", "text/plain", "UTF-8").statusCode(),
        is(200));
  }

  public static class MyContentPatternExtension extends StringValuePattern
      implements ContentPatternExtension {

    public MyContentPatternExtension() {
      super("Java Service Provider Interface must have a zero-argument constructor.");
    }

    @JsonCreator
    public MyContentPatternExtension(@JsonProperty("myContent") String expectedValue) {
      super(expectedValue);
    }

    @Override
    public Class<? extends ContentPattern<?>> getContentPatternClass() {
      return MyContentPatternExtension.class;
    }

    @Override
    public MatchResult match(String value) {
      return "myContent".equals(value) ? MatchResult.exactMatch() : MatchResult.noMatch();
    }
  }
}
