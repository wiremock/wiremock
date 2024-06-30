/*
 * Copyright (C) 2015-2024 Thomas Akehurst
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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.AdminException;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.CustomMatcherDefinition;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcher;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CustomMatchingAcceptanceTest {

  @SuppressWarnings("unchecked")
  @RegisterExtension
  public WireMockExtension wmRule =
      WireMockExtension.newInstance()
          .options(
              options()
                  .dynamicPort()
                  .extensions(
                      MyExtensionRequestMatcher.class, MyPathParameterAwareRequestMatcher.class))
          .failOnUnmatchedRequests(false)
          .build();

  WireMockTestClient client;
  WireMock wm;

  @BeforeEach
  public void init() {
    client = new WireMockTestClient(wmRule.getPort());
    wm = WireMock.create().port(wmRule.getPort()).build();
  }

  @Test
  public void customRequestMatcherCanBeDefinedAsClass() {
    wmRule.stubFor(requestMatching(new MyRequestMatcher()).willReturn(aResponse().withStatus(200)));
    assertThat(client.get("/correct").statusCode(), is(200));
    assertThat(client.get("/wrong").statusCode(), is(404));
  }

  @Test
  public void customRequestMatcherCanBeDefinedInline() {
    wmRule.stubFor(
        requestMatching(
                new RequestMatcher() {
                  @Override
                  public MatchResult match(Request request) {
                    return MatchResult.of(request.getUrl().contains("correct"));
                  }

                  @Override
                  public String getName() {
                    return "inline";
                  }
                })
            .willReturn(aResponse().withStatus(200)));

    assertThat(client.get("/correct").statusCode(), is(200));
    assertThat(client.get("/wrong").statusCode(), is(404));
  }

  @Test
  public void customRequestMatcherCanBeSpecifiedAsNamedExtension() {
    wm.register(
        requestMatching("path-contains-param", Parameters.one("path", "findthis"))
            .willReturn(aResponse().withStatus(200)));
    assertThat(client.get("/findthis/thing").statusCode(), is(200));
  }

  @Test
  public void inlineCustomRequestMatcherCanBeCombinedWithStandardMatchers() {
    wmRule.stubFor(
        get(urlPathMatching("/the/.*/one")).andMatching(new MyRequestMatcher()).willReturn(ok()));

    assertThat(client.get("/the/correct/one").statusCode(), is(200));
    assertThat(client.get("/the/wrong/one").statusCode(), is(404));
    assertThat(client.postJson("/the/correct/one", "{}").statusCode(), is(404));
  }

  @Test
  public void namedCustomRequestMatcherCanBeCombinedWithStandardMatchers() {
    wm.register(
        get(urlPathMatching("/the/.*/one"))
            .andMatching("path-contains-param", Parameters.one("path", "correct"))
            .willReturn(ok()));

    assertThat(client.get("/the/correct/one").statusCode(), is(200));
    assertThat(client.get("/the/wrong/one").statusCode(), is(404));
    assertThat(client.postJson("/the/correct/one", "{}").statusCode(), is(404));
  }

  @Test
  public void customMatcherDefinitionCanBeCombinedWithStandardMatchers() {
    wm.register(
        get(urlPathMatching("/the/.*/one"))
            .andMatching(
                new CustomMatcherDefinition(
                    "path-contains-param", Parameters.one("path", "correct")))
            .willReturn(ok()));

    assertThat(client.get("/the/correct/one").statusCode(), is(200));
    assertThat(client.get("/the/wrong/one").statusCode(), is(404));
    assertThat(client.postJson("/the/correct/one", "{}").statusCode(), is(404));
  }

  @Test
  void customMatcherCanMakeUseOfPathParameters() {
    wm.register(
        get(urlPathTemplate("/things/{thingId}"))
            .andMatching("path-aware-matcher")
            .willReturn(ok()));

    assertThat(client.get("/things/123").statusCode(), is(200));
    assertThat(client.get("/things/456").statusCode(), is(404));
  }

  @Test
  void customMatcherCanBeUsedDuringVerification() {
    client.get("/things/123");
    wm.verifyThat(
        getRequestedFor(urlPathTemplate("/things/{thingId}")).andMatching("path-aware-matcher"));

    wm.resetRequests();

    client.get("/things/456");
    wm.verifyThat(
        0, getRequestedFor(urlPathTemplate("/things/{thingId}")).andMatching("path-aware-matcher"));
  }

  @Test
  public void throwsExceptionIfInlineCustomMatcherUsedWithRemote() {
    assertThrows(
        AdminException.class,
        () ->
            wm.register(
                get(urlPathMatching("/the/.*/one"))
                    .andMatching(new MyRequestMatcher())
                    .willReturn(ok())));
  }

  public static class MyRequestMatcher extends RequestMatcherExtension {

    @Override
    public MatchResult match(Request request, Parameters parameters) {
      return MatchResult.of(request.getUrl().contains("correct"));
    }
  }

  public static class MyExtensionRequestMatcher extends RequestMatcherExtension {

    @Override
    public MatchResult match(Request request, Parameters parameters) {
      String pathSegment = parameters.getString("path");
      return MatchResult.of(request.getUrl().contains(pathSegment));
    }

    @Override
    public String getName() {
      return "path-contains-param";
    }
  }

  public static class MyPathParameterAwareRequestMatcher extends RequestMatcherExtension {

    @Override
    public String getName() {
      return "path-aware-matcher";
    }

    @Override
    public MatchResult match(Request request, Parameters parameters) {
      return MatchResult.of("123".equals(request.getPathParameters().get("thingId")));
    }
  }
}
