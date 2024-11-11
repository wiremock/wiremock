/*
 * Copyright (C) 2017-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RequestPatternBuilderTest {
  @Test
  void likeRequestPatternWithDifferentUrl() {
    RequestPattern requestPattern = RequestPattern.everything();

    RequestPattern newRequestPattern =
        RequestPatternBuilder.like(requestPattern).but().withUrl("/foo").build();

    assertThat(newRequestPattern.getUrl(), is("/foo"));
    assertThat(newRequestPattern, not(equalTo(requestPattern)));
  }

  @Test
  void likeRequestPatternWithDifferentUrlPath() {
    RequestPattern requestPattern = RequestPattern.everything();

    RequestPattern newRequestPattern =
        RequestPatternBuilder.like(requestPattern)
            .but()
            .withUrl(WireMock.urlPathEqualTo("/foo"))
            .build();

    assertThat(newRequestPattern.getUrlPath(), is("/foo"));
    assertThat(newRequestPattern, not(equalTo(requestPattern)));
  }

  @Test
  void likeRequestPatternWithoutCustomMatcher() {
    // Use a RequestPattern with everything defined except a custom matcher to ensure all fields are
    // set properly
    RequestPattern requestPattern =
        new RequestPattern(
            "https",
            WireMock.equalTo("my.wiremock.org"),
            1234,
            WireMock.urlEqualTo("/foo"),
            RequestMethod.POST,
            Map.of("X-Header", MultiValuePattern.of(WireMock.equalTo("bar"))),
            emptyMap(),
            Map.of("query_param", MultiValuePattern.of(WireMock.equalTo("bar"))),
            Map.of("form_param", MultiValuePattern.of(WireMock.equalTo("bar"))),
            Map.of("cookie", WireMock.equalTo("yum")),
            new BasicCredentials("user", "pass"),
            List.of(WireMock.equalTo("BODY")),
            null,
            null,
            null);

    RequestPattern newRequestPattern = RequestPatternBuilder.like(requestPattern).build();
    assertThat(newRequestPattern, is(requestPattern));
  }

  @Test
  void likeRequestPatternWithCustomMatcher() {
    RequestMatcher customRequestMatcher =
        new RequestMatcherExtension() {
          @Override
          public MatchResult match(Request request, Parameters parameters) {
            return MatchResult.noMatch();
          }
        };
    RequestPattern requestPattern = new RequestPattern(customRequestMatcher);

    RequestPattern newRequestPattern = RequestPatternBuilder.like(requestPattern).build();
    assertThat(newRequestPattern, is(requestPattern));
  }

  @Test
  void likeRequestPatternWithMultipartMatcher() {
    MultipartValuePattern multipartValuePattern = aMultipart().withBody(equalToJson("[]")).build();

    RequestPattern requestPattern = RequestPattern.everything();
    RequestPattern newRequestPattern =
        RequestPatternBuilder.like(requestPattern)
            .but()
            .withRequestBodyPart(multipartValuePattern)
            .build();

    assertThat(
        newRequestPattern.getMultipartPatterns(),
        everyItem(is(in(singletonList(multipartValuePattern)))));
    assertThat(newRequestPattern, not(equalTo(requestPattern)));
  }

  @Test
  void likeRequestPatternWithoutMultipartMatcher() {
    MultipartValuePattern multipartPattern = aMultipart().withBody(equalToJson("[]")).build();

    // Use a RequestPattern with everything defined except a custom matcher to ensure all fields are
    // set properly
    RequestPattern requestPattern =
        new RequestPattern(
            "https",
            WireMock.equalTo("my.wiremock.org"),
            1234,
            WireMock.urlEqualTo("/foo"),
            RequestMethod.POST,
            Map.of("X-Header", MultiValuePattern.of(WireMock.equalTo("bar"))),
            emptyMap(),
            Map.of("query_param", MultiValuePattern.of(WireMock.equalTo("bar"))),
            Map.of("form_param", MultiValuePattern.of(WireMock.equalTo("bar"))),
            Map.of("cookie", WireMock.equalTo("yum")),
            new BasicCredentials("user", "pass"),
            List.of(WireMock.equalTo("BODY")),
            null,
            null,
            singletonList(multipartPattern));

    RequestPattern newRequestPattern = RequestPatternBuilder.like(requestPattern).build();
    assertThat(newRequestPattern, is(requestPattern));
  }

  @Test
  void likeRequestPatternWithCustomMatcherDefinition() {
    CustomMatcherDefinition customMatcherDefinition =
        new CustomMatcherDefinition("foo", Parameters.empty());
    RequestPattern requestPattern = new RequestPattern(customMatcherDefinition);

    RequestPattern newRequestPattern = RequestPatternBuilder.like(requestPattern).build();
    assertThat(newRequestPattern, is(requestPattern));
  }

  @Test
  void likeRequestPatternCreatesIsolatedInstance() {
    RequestPattern newRequestPattern =
        RequestPatternBuilder.newRequestPattern()
            .withUrl(new UrlPathTemplatePattern("/bar/*"))
            .withScheme("https")
            .withHost(WireMock.equalTo("bar"))
            .withHeader("foo", WireMock.equalTo("bar"))
            .withPathParam("foo", WireMock.equalTo("bar"))
            .withQueryParam("foo", WireMock.equalTo("bar"))
            .withFormParam("foo", WireMock.equalTo("bar"))
            .withCookie("foo", WireMock.equalTo("bar"))
            .withRequestBody(WireMock.equalTo("bar"))
            .withAnyRequestBodyPart(new MultipartValuePatternBuilder("bar"))
            .build();

    RequestPattern newDerivedRequestPattern =
        RequestPatternBuilder.like(newRequestPattern)
            .withUrl(new UrlPathTemplatePattern("/baz/*"))
            .withScheme("http")
            .withHost(WireMock.equalTo("baz"))
            .withHeader("foo", WireMock.equalTo("baz"))
            .withPathParam("foo", WireMock.equalTo("baz"))
            .withQueryParam("foo", WireMock.equalTo("baz"))
            .withFormParam("foo", WireMock.equalTo("baz"))
            .withCookie("foo", WireMock.equalTo("baz"))
            .withRequestBody(WireMock.equalTo("baz"))
            .withAnyRequestBodyPart(new MultipartValuePatternBuilder("baz"))
            .build();

    assertThat(
        newRequestPattern.getUrlPathTemplate(), not(newDerivedRequestPattern.getUrlPathTemplate()));
    assertThat(newRequestPattern.getScheme(), not(newDerivedRequestPattern.getScheme()));
    assertThat(newRequestPattern.getHost(), not(newDerivedRequestPattern.getHost()));
    assertThat(newRequestPattern.getHeaders(), not(newDerivedRequestPattern.getHeaders()));
    assertThat(
        newRequestPattern.getPathParameters(), not(newDerivedRequestPattern.getPathParameters()));
    assertThat(
        newRequestPattern.getQueryParameters(), not(newDerivedRequestPattern.getQueryParameters()));
    assertThat(
        newRequestPattern.getFormParameters(), not(newDerivedRequestPattern.getFormParameters()));
    assertThat(newRequestPattern.getCookies(), not(newDerivedRequestPattern.getCookies()));
    assertThat(
        newRequestPattern.getBodyPatterns(), not(newDerivedRequestPattern.getBodyPatterns()));
    assertThat(
        newRequestPattern.getMultipartPatterns(),
        not(newDerivedRequestPattern.getMultipartPatterns()));
  }
}
