/*
 * Copyright (C) 2012-2021 Thomas Akehurst
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

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ChunkedDribbleDelay;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import java.util.ArrayList;

import com.github.tomakehurst.wiremock.http.UniformDistribution;
import org.junit.jupiter.api.Test;

public class ResponseDefinitionBuilderTest {

  @Test
  public void withTransformerParameterShouldNotChangeOriginalTransformerParametersValue() {
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
  public void likeShouldCreateCompleteResponseDefinitionCopy() throws Exception {
    final ArrayList<String> transformerNames = new ArrayList<>();
    transformerNames.add("some transformer");
    ResponseDefinition originalResponseDefinition =
        new ResponseDefinition(200,
                               "OK",
                               Body.fromOneOf(null, "some body", null, null),
                               "some_body.json",
                               new HttpHeaders(new HttpHeader("some header", "some value")),
                               null,
                               100,
                               new UniformDistribution(1, 2),
                               new ChunkedDribbleDelay(1, 1000),
                               "",
                               null,
                               Fault.EMPTY_RESPONSE,
                               transformerNames,
                               Parameters.one("some param", "some value"),
                               true);

    ResponseDefinition copiedResponseDefinition =
        ResponseDefinitionBuilder.like(originalResponseDefinition).build();

    assertThat(copiedResponseDefinition, is(originalResponseDefinition));
  }

  @Test
  public void proxyResponseDefinitionWithoutProxyInformationIsNotInResponseDefinition() {
    ResponseDefinition proxyDefinition =
        ResponseDefinitionBuilder.responseDefinition().proxiedFrom("http://my.domain").build();

    assertThat(proxyDefinition.getAdditionalProxyRequestHeaders(), nullValue());
    assertThat(proxyDefinition.getProxyUrlPrefixToRemove(), nullValue());
  }

  @Test
  public void
      proxyResponseDefinitionWithoutProxyInformationIsNotInResponseDefinitionWithJsonBody() {
    ResponseDefinition proxyDefinition =
        ResponseDefinitionBuilder.responseDefinition()
            .proxiedFrom("http://my.domain")
            .withJsonBody(Json.read("{}", JsonNode.class))
            .build();

    assertThat(proxyDefinition.getAdditionalProxyRequestHeaders(), nullValue());
    assertThat(proxyDefinition.getProxyUrlPrefixToRemove(), nullValue());
  }

  @Test
  public void
      proxyResponseDefinitionWithoutProxyInformationIsNotInResponseDefinitionWithBinaryBody() {
    ResponseDefinition proxyDefinition =
        ResponseDefinitionBuilder.responseDefinition()
            .proxiedFrom("http://my.domain")
            .withBody(new byte[] {0x01})
            .build();

    assertThat(proxyDefinition.getAdditionalProxyRequestHeaders(), nullValue());
    assertThat(proxyDefinition.getProxyUrlPrefixToRemove(), nullValue());
  }

  @Test
  public void proxyResponseDefinitionWithExtraInformationIsInResponseDefinition() {
    ResponseDefinition proxyDefinition =
        ResponseDefinitionBuilder.responseDefinition()
            .proxiedFrom("http://my.domain")
            .withAdditionalRequestHeader("header", "value")
            .withProxyUrlPrefixToRemove("/remove")
            .build();

    assertThat(
        proxyDefinition.getAdditionalProxyRequestHeaders(),
        equalTo(new HttpHeaders(newArrayList(new HttpHeader("header", "value")))));
    assertThat(proxyDefinition.getProxyUrlPrefixToRemove(), equalTo("/remove"));
  }

  @Test
  public void proxyResponseDefinitionWithExtraInformationIsInResponseDefinitionWithJsonBody() {
    ResponseDefinition proxyDefinition =
        ResponseDefinitionBuilder.responseDefinition()
            .proxiedFrom("http://my.domain")
            .withAdditionalRequestHeader("header", "value")
            .withProxyUrlPrefixToRemove("/remove")
            .withJsonBody(Json.read("{}", JsonNode.class))
            .build();

    assertThat(
        proxyDefinition.getAdditionalProxyRequestHeaders(),
        equalTo(new HttpHeaders(newArrayList(new HttpHeader("header", "value")))));
    assertThat(proxyDefinition.getProxyUrlPrefixToRemove(), equalTo("/remove"));
  }

  @Test
  public void proxyResponseDefinitionWithExtraInformationIsInResponseDefinitionWithBinaryBody() {
    ResponseDefinition proxyDefinition =
        ResponseDefinitionBuilder.responseDefinition()
            .proxiedFrom("http://my.domain")
            .withAdditionalRequestHeader("header", "value")
            .withProxyUrlPrefixToRemove("/remove")
            .withBody(new byte[] {0x01})
            .build();

    assertThat(
        proxyDefinition.getAdditionalProxyRequestHeaders(),
        equalTo(new HttpHeaders(newArrayList(new HttpHeader("header", "value")))));
    assertThat(proxyDefinition.getProxyUrlPrefixToRemove(), equalTo("/remove"));
  }
}
