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

import static com.github.tomakehurst.wiremock.ContentPatternExtensionAcceptanceTest.StartsWithMatcher.startsWith;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.client.WireMock;
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
                  .extensions(StartsWithMatcherExtension.class)
                  .usingFilesUnderClasspath("content-pattern-extension"))
          .build();

  static WireMock wireMockRemoteClient;

  public static WireMockTestClient client;

  @SuppressWarnings("unchecked")
  @BeforeAll
  public static void beforeAll() {
    client = new WireMockTestClient(wm.getPort());
    wireMockRemoteClient = create()
            .port(wm.getPort())
            .extensions(StartsWithMatcherExtension.class)
            .build();
  }

  @Test
  public void customMatcherWithRequestBody() {
    wm.stubFor(
        post("/stubFor/single").withRequestBody(startsWith("my")).willReturn(ok()));
    assertThat(
        client.postWithBody("/stubFor/single", "myContent", "text/plain", "UTF-8").statusCode(),
        is(200));
  }

  @Test
  public void customMatcherWithRequestBodyCombinedWithStandardMatchers() {
    wm.stubFor(
        post("/stubFor/multiple")
            .withRequestBody(startsWith("my"))
            .withRequestBody(containing("my"))
            .willReturn(ok()));
    assertThat(
        client.postWithBody("/stubFor/multiple", "myContent", "text/plain", "UTF-8").statusCode(),
        is(200));
  }

  @Test
  public void customMatcherWithHeaderDefinedViaRemoteClient() {
    wireMockRemoteClient.register(
            get("/header/statically")
                    .withHeader("X-Content", startsWith("my"))
                    .willReturn(ok()));
    assertThat(
            client.get("/header/statically", withHeader("X-Content", "myContent")).statusCode(),
            is(200));

    assertThat(
            client.get("/header/statically", withHeader("X-Content", "wrong")).statusCode(),
            is(404));
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

  public static class StartsWithMatcherExtension implements ContentPatternExtension {

    @Override
    public Class<? extends ContentPattern<?>> getContentPatternClass() {
      return StartsWithMatcher.class;
    }

    @Override
    public String getName() {
      return "starts-with-matcher";
    }
  }

  public static class StartsWithMatcher extends StringValuePattern {

    public static StartsWithMatcher startsWith(String prefix) {
      return new StartsWithMatcher(prefix);
    }

    @JsonCreator
    public StartsWithMatcher(@JsonProperty("startsWith") String expectedValue) {
      super(expectedValue);
    }

    @Override
    public MatchResult match(String value) {
      return MatchResult.of(value.startsWith(expectedValue));
    }
  }
}
