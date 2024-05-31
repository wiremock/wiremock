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
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.List;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ContentPatternExtensionAcceptanceTest {

  private static final RequestPattern CUSTOM =
      post(urlPathTemplate("/{path}"))
          .withQueryParam("query", startsWith("query"))
          .withFormParam("form", startsWith("form"))
          .withPathParam("path", startsWith("path"))
          .withHeader("header", startsWith("header"))
          .withCookie("cookie", startsWith("cookie"))
          .withRequestBody(startsWith("body"))
          .willReturn(ok())
          .build()
          .getRequest();

  @SuppressWarnings("unchecked")
  @RegisterExtension
  public static WireMockExtension wmLocal =
      WireMockExtension.newInstance()
          .options(
              options()
                  .dynamicPort()
                  .extensions(StartsWithMatcherExtension.class)
                  .usingFilesUnderClasspath("content-pattern-extension"))
          .build();

  public static WireMock wmRemote;

  public static WireMockTestClient client;

  @SuppressWarnings("unchecked")
  @BeforeAll
  public static void beforeAll() {
    wmRemote =
        create().port(wmLocal.getPort()).extensions(StartsWithMatcherExtension.class).build();
    client = new WireMockTestClient(wmLocal.getPort());
  }

  @Test
  public void localStubFor() {
    wmLocal.stubFor(
        post(urlPathTemplate("/stubFor/{path}"))
            .withQueryParam("query", startsWith("local"))
            .withQueryParam("query", equalTo("localQuery"))
            .withPathParam("path", startsWith("local"))
            .withPathParam("path", equalTo("localPath"))
            .withHeader("X-Local", startsWith("local"))
            .withHeader("X-Local", equalTo("localHeader"))
            .withRequestBody(startsWith("local"))
            .withRequestBody(equalTo("localBody"))
            .willReturn(ok()));
    assertThat(
        client
            .post(
                "/stubFor/localPath?query=localQuery",
                new StringEntity("localBody"),
                withHeader("X-Local", "localHeader"))
            .statusCode(),
        is(200));
  }

  @Test
  public void remoteRegister() {
    wmRemote.register(
        post(urlPathTemplate("/register/{path}"))
            .withQueryParam("query", startsWith("remote"))
            .withQueryParam("query", equalTo("remoteQuery"))
            .withPathParam("path", startsWith("remote"))
            .withPathParam("path", equalTo("remotePath"))
            .withHeader("X-Remote", startsWith("remote"))
            .withHeader("X-Remote", equalTo("remoteHeader"))
            .withRequestBody(startsWith("remote"))
            .withRequestBody(equalTo("remoteBody"))
            .willReturn(ok()));
    assertThat(
        client
            .post(
                "/register/remotePath?query=remoteQuery",
                new StringEntity("remoteBody"),
                withHeader("X-Remote", "remoteHeader"))
            .statusCode(),
        is(200));
  }

  @Test
  public void localGetStubMappings() {
    List<StubMapping> stubMappings = wmLocal.getStubMappings();
    assertThat(stubMappings.size(), is(1));
    assertThat(stubMappings.get(0).getRequest(), is(CUSTOM));
  }

  @Test
  public void remoteAllStubMappings() {
    List<StubMapping> stubMappings = wmRemote.allStubMappings().getMappings();
    assertThat(stubMappings.size(), is(1));
    assertThat(stubMappings.get(0).getRequest(), is(CUSTOM));
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

    public String getStartsWith() {
      return expectedValue;
    }
  }
}
