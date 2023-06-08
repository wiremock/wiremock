/*
 * Copyright (C) 2021-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import org.junit.jupiter.api.Test;

class ResponseDefinitionTest {

  @Test
  void getProxyUrlGivesBackRequestUrlIfBrowserProxyRequest() {
    ResponseDefinition response =
        ResponseDefinition.browserProxy(
            MockRequest.mockRequest()
                .host("http://my.domain")
                .url("/path")
                .isBrowserProxyRequest(true));

    assertThat(response.getProxyUrl(), equalTo("http://my.domain/path"));
  }

  @Test
  void getProxyUrlGivesBackTheProxyUrlWhenNotBrowserProxy() {
    ResponseDefinition response =
        ResponseDefinitionBuilder.responseDefinition().proxiedFrom("http://my.proxy.url").build();

    response.setOriginalRequest(MockRequest.mockRequest().url("/path"));

    assertThat(response.getProxyUrl(), equalTo("http://my.proxy.url/path"));
  }

  @Test
  void doesNotRemoveRequestPathPrefixWhenPrefixToRemoveDoesNotMatch() {
    ResponseDefinition response =
        ResponseDefinitionBuilder.responseDefinition()
            .proxiedFrom("http://my.proxy.url")
            .withProxyUrlPrefixToRemove("/no/match")
            .build();

    response.setOriginalRequest(MockRequest.mockRequest().url("/path"));

    assertThat(response.getProxyUrl(), equalTo("http://my.proxy.url/path"));
  }

  @Test
  void removesRequestPathPrefixWhenPrefixToRemoveMatches() {
    ResponseDefinition response =
        ResponseDefinitionBuilder.responseDefinition()
            .proxiedFrom("http://my.proxy.url")
            .withProxyUrlPrefixToRemove("/path")
            .build();

    response.setOriginalRequest(MockRequest.mockRequest().url("/path"));

    assertThat(response.getProxyUrl(), equalTo("http://my.proxy.url"));
  }

  @Test
  void getProxyUrlGivesBackTheProxyUrlWhenProxiedUrlBeginWithWhiteSpace() {
    ResponseDefinition response =
        ResponseDefinitionBuilder.responseDefinition().proxiedFrom(" http://my.proxy.url").build();

    response.setOriginalRequest(MockRequest.mockRequest().url("/path"));

    assertThat(response.getProxyUrl(), equalTo("http://my.proxy.url/path"));
  }

  @Test
  void getProxyUrlGivesBackTheProxyUrlWhenProxiedUrlEndWithWhiteSpace() {
    ResponseDefinition response =
        ResponseDefinitionBuilder.responseDefinition().proxiedFrom("http://my.proxy.url ").build();

    response.setOriginalRequest(MockRequest.mockRequest().url("/path"));

    assertThat(response.getProxyUrl(), equalTo("http://my.proxy.url/path"));
  }

  @Test
  void getProxyUrlGivesBackTheProxyUrlWhenProxiedFromUrlNull() {
    ResponseDefinition response = ResponseDefinitionBuilder.responseDefinition().build();

    response.setOriginalRequest(MockRequest.mockRequest().url("/path"));

    assertThat(response.getProxyUrl(), equalTo("null/path"));
  }
}
