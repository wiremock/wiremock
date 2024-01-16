/*
 * Copyright (C) 2012-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.client;

import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_ENCODING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResponseDefinitionBuilderTest {

  @Test
  void withTransformerParameterShouldNotChangeOriginalTransformerParametersValue() {
    ResponseDefinition originalResponseDefinition =
        ResponseDefinitionBuilder.responseDefinition()
            .withTransformerParameter("name", "original")
            .build();

    ResponseDefinition transformedResponseDefinition =
        ResponseDefinitionBuilder.like(originalResponseDefinition)
            .but()
            .withTransformerParameter("name", "changed")
            .build();

    assertThat(
        originalResponseDefinition.getTransformerParameters().getString("name"), is("original"));
    assertThat(
        transformedResponseDefinition.getTransformerParameters().getString("name"), is("changed"));
  }

  @Test
  void likeShouldCreateCompleteResponseDefinitionCopy() {
    ResponseDefinition originalResponseDefinition =
        ResponseDefinitionBuilder.responseDefinition()
            .withStatus(200)
            .withStatusMessage("OK")
            .withBody("some body")
            .withBase64Body(
                Base64.getEncoder().encodeToString("some body".getBytes(StandardCharsets.UTF_8)))
            .withBodyFile("some_body.json")
            .withHeader("some header", "some value")
            .withFixedDelay(100)
            .withUniformRandomDelay(1, 2)
            .withChunkedDribbleDelay(1, 1000)
            .withFault(Fault.EMPTY_RESPONSE)
            .withTransformers("some transformer")
            .withTransformerParameter("some param", "some value")
            .build();

    ResponseDefinition copiedResponseDefinition =
        ResponseDefinitionBuilder.like(originalResponseDefinition).build();

    assertThat(copiedResponseDefinition, is(originalResponseDefinition));
  }

  @Test
  void proxyResponseDefinitionWithoutProxyInformationIsNotInResponseDefinition() {
    ResponseDefinition proxyDefinition =
        ResponseDefinitionBuilder.responseDefinition().proxiedFrom("http://my.domain").build();

    assertThat(proxyDefinition.getAdditionalProxyRequestHeaders(), nullValue());
    assertThat(proxyDefinition.getProxyUrlPrefixToRemove(), nullValue());
  }

  @Test
  void proxyResponseDefinitionWithoutProxyInformationIsNotInResponseDefinitionWithJsonBody() {
    ResponseDefinition proxyDefinition =
        ResponseDefinitionBuilder.responseDefinition()
            .proxiedFrom("http://my.domain")
            .withJsonBody(Json.read("{}", JsonNode.class))
            .build();

    assertThat(proxyDefinition.getAdditionalProxyRequestHeaders(), nullValue());
    assertThat(proxyDefinition.getProxyUrlPrefixToRemove(), nullValue());
  }

  @Test
  void proxyResponseDefinitionWithoutProxyInformationIsNotInResponseDefinitionWithBinaryBody() {
    ResponseDefinition proxyDefinition =
        ResponseDefinitionBuilder.responseDefinition()
            .proxiedFrom("http://my.domain")
            .withBody(new byte[] {0x01})
            .build();

    assertThat(proxyDefinition.getAdditionalProxyRequestHeaders(), nullValue());
    assertThat(proxyDefinition.getProxyUrlPrefixToRemove(), nullValue());
  }

  @Test
  void proxyResponseDefinitionWithExtraInformationIsInResponseDefinition() {
    ResponseDefinition proxyDefinition =
        ResponseDefinitionBuilder.responseDefinition()
            .proxiedFrom("http://my.domain")
            .withAdditionalRequestHeader("header", "value")
            .withProxyUrlPrefixToRemove("/remove")
            .build();

    assertThat(
        proxyDefinition.getAdditionalProxyRequestHeaders(),
        equalTo(new HttpHeaders(List.of(new HttpHeader("header", "value")))));
    assertThat(proxyDefinition.getProxyUrlPrefixToRemove(), equalTo("/remove"));
  }

  @Test
  void proxyResponseDefinitionWithExtraInformationIsInResponseDefinitionWithJsonBody() {
    ResponseDefinition proxyDefinition =
        ResponseDefinitionBuilder.responseDefinition()
            .proxiedFrom("http://my.domain")
            .withAdditionalRequestHeader("header", "value")
            .withProxyUrlPrefixToRemove("/remove")
            .withJsonBody(Json.read("{}", JsonNode.class))
            .build();

    assertThat(
        proxyDefinition.getAdditionalProxyRequestHeaders(),
        equalTo(new HttpHeaders(List.of(new HttpHeader("header", "value")))));
    assertThat(proxyDefinition.getProxyUrlPrefixToRemove(), equalTo("/remove"));
  }

  @Test
  void proxyResponseDefinitionWithExtraInformationIsInResponseDefinitionWithBinaryBody() {
    ResponseDefinition proxyDefinition =
        ResponseDefinitionBuilder.responseDefinition()
            .proxiedFrom("http://my.domain")
            .withAdditionalRequestHeader("header", "value")
            .withProxyUrlPrefixToRemove("/remove")
            .withBody(new byte[] {0x01})
            .build();

    assertThat(
        proxyDefinition.getAdditionalProxyRequestHeaders(),
        equalTo(new HttpHeaders(List.of(new HttpHeader("header", "value")))));
    assertThat(proxyDefinition.getProxyUrlPrefixToRemove(), equalTo("/remove"));
  }

  @Test
  void responseDefinitionBuilderWithGzipDisabled() {
    ResponseDefinition responseDefinition =
        ResponseDefinitionBuilder.responseDefinition().withGzipDisabled(true).build();

    assertNotNull(responseDefinition);
    assertEquals(
        "none", responseDefinition.getHeaders().getHeader(CONTENT_ENCODING).getValues().get(0));
  }
}
