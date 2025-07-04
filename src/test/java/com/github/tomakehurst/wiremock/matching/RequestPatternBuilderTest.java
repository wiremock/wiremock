/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

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
            WireMock.equalTo("192.168.2.2"),
            WireMock.urlEqualTo("/foo"),
            RequestMethod.POST,
            Map.of("X-Header", MultiValuePattern.of(WireMock.equalTo("bar"))),
            null,
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
            WireMock.equalTo("192.168.1.1"),
            WireMock.urlEqualTo("/foo"),
            RequestMethod.POST,
            Map.of("X-Header", MultiValuePattern.of(WireMock.equalTo("bar"))),
            null,
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
}
