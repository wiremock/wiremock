/*
 * Copyright (C) 2011-2022 Thomas Akehurst
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
import static com.google.common.collect.Maps.newHashMap;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;

public class ResponseDefinitionBuilder {

  protected int status = HTTP_OK;
  protected String statusMessage;
  protected Body body = Body.none();
  protected String bodyFileName;
  protected List<HttpHeader> headers = newArrayList();
  protected Integer fixedDelayMilliseconds;
  protected DelayDistribution delayDistribution;
  protected ChunkedDribbleDelay chunkedDribbleDelay;
  protected String proxyBaseUrl;
  protected String proxyUrlPrefixToRemove;
  protected Fault fault;
  protected List<String> responseTransformerNames;
  protected Map<String, Object> transformerParameters = newHashMap();
  protected Boolean wasConfigured = true;

  public static ResponseDefinitionBuilder like(ResponseDefinition responseDefinition) {
    ResponseDefinitionBuilder builder = new ResponseDefinitionBuilder();
    builder.status = responseDefinition.getStatus();
    builder.statusMessage = responseDefinition.getStatusMessage();
    builder.headers =
        responseDefinition.getHeaders() != null
            ? newArrayList(responseDefinition.getHeaders().all())
            : Lists.<HttpHeader>newArrayList();
    builder.body = responseDefinition.getReponseBody();
    builder.bodyFileName = responseDefinition.getBodyFileName();
    builder.fixedDelayMilliseconds = responseDefinition.getFixedDelayMilliseconds();
    builder.delayDistribution = responseDefinition.getDelayDistribution();
    builder.chunkedDribbleDelay = responseDefinition.getChunkedDribbleDelay();
    builder.proxyBaseUrl = responseDefinition.getProxyBaseUrl();
    builder.proxyUrlPrefixToRemove = responseDefinition.getProxyUrlPrefixToRemove();
    builder.fault = responseDefinition.getFault();
    builder.responseTransformerNames = responseDefinition.getTransformers();
    builder.transformerParameters =
        responseDefinition.getTransformerParameters() != null
            ? Parameters.from(responseDefinition.getTransformerParameters())
            : Parameters.empty();
    builder.wasConfigured = responseDefinition.isFromConfiguredStub();

    if (builder.proxyBaseUrl != null) {
      ProxyResponseDefinitionBuilder proxyResponseDefinitionBuilder = new ProxyResponseDefinitionBuilder(builder);
      proxyResponseDefinitionBuilder.proxyUrlPrefixToRemove = responseDefinition.getProxyUrlPrefixToRemove();
      proxyResponseDefinitionBuilder.additionalRequestHeaders =
              responseDefinition.getAdditionalProxyRequestHeaders() != null
                      ? (List<HttpHeader>) responseDefinition.getAdditionalProxyRequestHeaders().all()
                      : Lists.<HttpHeader>newArrayList();

      return proxyResponseDefinitionBuilder;
    }

    return builder;
  }

  public static ResponseDefinition jsonResponse(Object body) {
    return jsonResponse(body, HTTP_OK);
  }

  public static ResponseDefinition jsonResponse(Object body, int status) {
    return new ResponseDefinitionBuilder()
        .withBody(Json.write(body))
        .withStatus(status)
        .withHeader("Content-Type", "application/json")
        .build();
  }

  public ResponseDefinitionBuilder but() {
    return this;
  }

  public ResponseDefinitionBuilder withStatus(int status) {
    this.status = status;
    return this;
  }

  public ResponseDefinitionBuilder withHeader(String key, String... values) {
    headers.add(new HttpHeader(key, values));
    return this;
  }

  public ResponseDefinitionBuilder withBodyFile(String fileName) {
    this.bodyFileName = fileName;
    return this;
  }

  public ResponseDefinitionBuilder withBody(String body) {
    this.body = Body.fromOneOf(null, body, null, null);
    return this;
  }

  public ResponseDefinitionBuilder withBody(byte[] body) {
    this.body = Body.fromOneOf(body, null, null, null);
    return this;
  }

  public ResponseDefinitionBuilder withResponseBody(Body body) {
    this.body = body;
    return this;
  }

  public ResponseDefinitionBuilder withJsonBody(JsonNode jsonBody) {
    this.body = Body.fromOneOf(null, null, jsonBody, null);
    return this;
  }

  public ResponseDefinitionBuilder withFixedDelay(Integer milliseconds) {
    this.fixedDelayMilliseconds = milliseconds;
    return this;
  }

  public ResponseDefinitionBuilder withRandomDelay(DelayDistribution distribution) {
    this.delayDistribution = distribution;
    return this;
  }

  public ResponseDefinitionBuilder withLogNormalRandomDelay(
      double medianMilliseconds, double sigma) {
    return withRandomDelay(new LogNormal(medianMilliseconds, sigma));
  }

  public ResponseDefinitionBuilder withUniformRandomDelay(
      int lowerMilliseconds, int upperMilliseconds) {
    return withRandomDelay(new UniformDistribution(lowerMilliseconds, upperMilliseconds));
  }

  public ResponseDefinitionBuilder withChunkedDribbleDelay(int numberOfChunks, int totalDuration) {
    this.chunkedDribbleDelay = new ChunkedDribbleDelay(numberOfChunks, totalDuration);
    return this;
  }

  public ResponseDefinitionBuilder withTransformers(String... responseTransformerNames) {
    this.responseTransformerNames = asList(responseTransformerNames);
    return this;
  }

  public ResponseDefinitionBuilder withTransformerParameters(Map<String, Object> parameters) {
    transformerParameters.putAll(parameters);
    return this;
  }

  public ResponseDefinitionBuilder withTransformerParameter(String name, Object value) {
    transformerParameters.put(name, value);
    return this;
  }

  public ResponseDefinitionBuilder withTransformer(
      String transformerName, String parameterKey, Object parameterValue) {
    withTransformers(transformerName);
    withTransformerParameter(parameterKey, parameterValue);
    return this;
  }

  public ProxyResponseDefinitionBuilder proxiedFrom(String proxyBaseUrl) {
    this.proxyBaseUrl = proxyBaseUrl;
    return new ProxyResponseDefinitionBuilder(this);
  }

  public static ResponseDefinitionBuilder responseDefinition() {
    return new ResponseDefinitionBuilder();
  }

  public static <T> ResponseDefinitionBuilder okForJson(T body) {
    return responseDefinition()
        .withStatus(HTTP_OK)
        .withBody(Json.write(body))
        .withHeader("Content-Type", "application/json");
  }

  public static <T> ResponseDefinitionBuilder okForEmptyJson() {
    return responseDefinition()
        .withStatus(HTTP_OK)
        .withBody("{}")
        .withHeader("Content-Type", "application/json");
  }

  public ResponseDefinitionBuilder withHeaders(HttpHeaders headers) {
    this.headers = ImmutableList.copyOf(headers.all());
    return this;
  }

  public ResponseDefinitionBuilder withBase64Body(String base64Body) {
    this.body = Body.fromOneOf(null, null, null, base64Body);
    return this;
  }

  public ResponseDefinitionBuilder withStatusMessage(String message) {
    this.statusMessage = message;
    return this;
  }

  public static class ProxyResponseDefinitionBuilder extends ResponseDefinitionBuilder {

    private List<HttpHeader> additionalRequestHeaders = newArrayList();

    public ProxyResponseDefinitionBuilder(ResponseDefinitionBuilder from) {
      this.status = from.status;
      this.statusMessage = from.statusMessage;
      this.headers = from.headers;
      this.body = from.body;
      this.bodyFileName = from.bodyFileName;
      this.fault = from.fault;
      this.fixedDelayMilliseconds = from.fixedDelayMilliseconds;
      this.delayDistribution = from.delayDistribution;
      this.chunkedDribbleDelay = from.chunkedDribbleDelay;
      this.proxyBaseUrl = from.proxyBaseUrl;
      this.proxyUrlPrefixToRemove = from.proxyUrlPrefixToRemove;
      this.responseTransformerNames = from.responseTransformerNames;
      this.transformerParameters = from.transformerParameters;
    }

    public ProxyResponseDefinitionBuilder withAdditionalRequestHeader(String key, String value) {
      additionalRequestHeaders.add(new HttpHeader(key, value));
      return this;
    }

    public ProxyResponseDefinitionBuilder withProxyUrlPrefixToRemove(
        String proxyUrlPrefixToRemove) {
      this.proxyUrlPrefixToRemove = proxyUrlPrefixToRemove;
      return this;
    }

    @Override
    public ResponseDefinition build() {
      return super.build(
          !additionalRequestHeaders.isEmpty() ? new HttpHeaders(additionalRequestHeaders) : null,
          proxyUrlPrefixToRemove);
    }
  }

  public ResponseDefinitionBuilder withFault(Fault fault) {
    this.fault = fault;
    return this;
  }

  public ResponseDefinition build() {
    return build(null, null);
  }

  protected ResponseDefinition build(
      HttpHeaders additionalProxyRequestHeaders, String proxyUrlPrefixToRemove) {
    HttpHeaders httpHeaders =
        headers == null || headers.isEmpty() ? null : new HttpHeaders(headers);
    Parameters transformerParameters =
        this.transformerParameters == null || this.transformerParameters.isEmpty()
            ? null
            : Parameters.from(this.transformerParameters);
    return new ResponseDefinition(
        status,
        statusMessage,
        body,
        bodyFileName,
        httpHeaders,
        additionalProxyRequestHeaders,
        fixedDelayMilliseconds,
        delayDistribution,
        chunkedDribbleDelay,
        proxyBaseUrl,
        proxyUrlPrefixToRemove,
        fault,
        responseTransformerNames,
        transformerParameters,
        wasConfigured);
  }
}
