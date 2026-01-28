/*
 * Copyright (C) 2011-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ContentTypes.*;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.*;
import java.util.*;

@SuppressWarnings("UnusedReturnValue")
public class ResponseDefinitionBuilder {

  protected final ResponseDefinition.Builder builder;

  public ResponseDefinitionBuilder() {
    this(new ResponseDefinition.Builder());
  }

  private ResponseDefinitionBuilder(ResponseDefinition.Builder builder) {
    this.builder = builder;
  }

  public static ResponseDefinitionBuilder like(ResponseDefinition responseDefinition) {
    return new ResponseDefinitionBuilder(responseDefinition.toBuilder());
  }

  public static ResponseDefinition jsonResponse(Object body) {
    return jsonResponse(body, HTTP_OK);
  }

  public static ResponseDefinition jsonResponse(Object body, int status) {
    return new ResponseDefinitionBuilder()
        .withBody(Json.write(body))
        .withStatus(status)
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .build();
  }

  public ResponseDefinitionBuilder but() {
    return this;
  }

  public ResponseDefinitionBuilder withStatus(int status) {
    builder.setStatus(status);
    return this;
  }

  public ResponseDefinitionBuilder withHeader(String key, String... values) {
    builder.headers(builder -> builder.add(key, values));
    return this;
  }

  public ResponseDefinitionBuilder withBodyFile(String fileName) {
    builder.setBodyFileName(fileName);
    return this;
  }

  public ResponseDefinitionBuilder withSimpleBody(String body) {
    builder.setBody(EntityDefinition.simple(body));
    return this;
  }

  public ResponseDefinitionBuilder withBody(String body) {
    builder.setBody(body);
    return this;
  }

  public ResponseDefinitionBuilder withBody(byte[] body) {
    builder.setBody(body);
    return this;
  }

  public ResponseDefinitionBuilder withEntityBody(EntityDefinition body) {
    builder.setBody(body);
    return this;
  }

  public ResponseDefinitionBuilder withJsonBody(JsonNode jsonBody) {
    builder.setBody(EntityDefinition.json(jsonBody));
    return this;
  }

  public ResponseDefinitionBuilder withFixedDelay(Integer milliseconds) {
    builder.setFixedDelayMilliseconds(milliseconds);
    return this;
  }

  public ResponseDefinitionBuilder withRandomDelay(DelayDistribution distribution) {
    builder.setDelayDistribution(distribution);
    return this;
  }

  public ResponseDefinitionBuilder withLogNormalRandomDelay(
      double medianMilliseconds, double sigma) {
    return withLogNormalRandomDelay(medianMilliseconds, sigma, null);
  }

  public ResponseDefinitionBuilder withLogNormalRandomDelay(
      double medianMilliseconds, double sigma, Double maxValue) {
    return withRandomDelay(new LogNormal(medianMilliseconds, sigma, maxValue));
  }

  public ResponseDefinitionBuilder withUniformRandomDelay(
      int lowerMilliseconds, int upperMilliseconds) {
    return withRandomDelay(new UniformDistribution(lowerMilliseconds, upperMilliseconds));
  }

  public ResponseDefinitionBuilder withChunkedDribbleDelay(int numberOfChunks, int totalDuration) {
    builder.setChunkedDribbleDelay(new ChunkedDribbleDelay(numberOfChunks, totalDuration));
    return this;
  }

  public ResponseDefinitionBuilder withTransformers(String... responseTransformerNames) {
    builder.setTransformers(asList(responseTransformerNames));
    return this;
  }

  public ResponseDefinitionBuilder withTransformerParameters(Map<String, Object> parameters) {
    builder.setTransformerParameters(
        builder.getTransformerParameters().merge(Parameters.from(parameters)));
    return this;
  }

  public ResponseDefinitionBuilder withTransformerParameter(String name, Object value) {
    return withTransformerParameters(Map.of(name, value));
  }

  public ResponseDefinitionBuilder withTransformer(
      String transformerName, String parameterKey, Object parameterValue) {
    withTransformers(transformerName);
    withTransformerParameter(parameterKey, parameterValue);
    return this;
  }

  public ProxyResponseDefinitionBuilder proxiedFrom(String proxyBaseUrl) {
    builder.setProxyBaseUrl(proxyBaseUrl);
    return new ProxyResponseDefinitionBuilder(this);
  }

  public ResponseDefinitionBuilder withGzipDisabled(boolean gzipDisabled) {
    if (gzipDisabled) {
      withHeader(CONTENT_ENCODING, "none");
    }
    return this;
  }

  public static ResponseDefinitionBuilder responseDefinition() {
    return new ResponseDefinitionBuilder();
  }

  public static <T> ResponseDefinitionBuilder okForJson(T body) {
    return responseDefinition()
        .withStatus(HTTP_OK)
        .withBody(Json.write(body))
        .withHeader(CONTENT_TYPE, APPLICATION_JSON);
  }

  public static <T> ResponseDefinitionBuilder okForEmptyJson() {
    return responseDefinition()
        .withStatus(HTTP_OK)
        .withBody("{}")
        .withHeader(CONTENT_TYPE, APPLICATION_JSON);
  }

  public ResponseDefinitionBuilder withHeaders(HttpHeaders headers) {
    builder.setHeaders(headers);
    return this;
  }

  public ResponseDefinitionBuilder withBase64Body(String base64Body) {
    builder.setBody(EntityDefinition.fromBase64(base64Body));
    return this;
  }

  public ResponseDefinitionBuilder withStatusMessage(String message) {
    builder.setStatusMessage(message);
    return this;
  }

  public static class ProxyResponseDefinitionBuilder extends ResponseDefinitionBuilder {

    public ProxyResponseDefinitionBuilder(ResponseDefinitionBuilder from) {
      super(from.builder);
    }

    public ProxyResponseDefinitionBuilder withAdditionalRequestHeader(String key, String value) {
      builder.setAdditionalProxyRequestHeaders(
          builder.getAdditionalProxyRequestHeaders().plus(new HttpHeader(key, value)));
      return this;
    }

    public ProxyResponseDefinitionBuilder withAdditionalRequestHeaders(HttpHeaders headers) {
      builder.setAdditionalProxyRequestHeaders(headers);
      return this;
    }

    public ProxyResponseDefinitionBuilder withRemoveRequestHeader(String key) {
      builder.getRemoveProxyRequestHeaders().add(key.toLowerCase());
      return this;
    }

    public ProxyResponseDefinitionBuilder withRemoveRequestHeaders(List<String> keys) {
      builder.setRemoveProxyRequestHeaders(keys);
      return this;
    }

    public ProxyResponseDefinitionBuilder withProxyUrlPrefixToRemove(
        String proxyUrlPrefixToRemove) {
      builder.setProxyUrlPrefixToRemove(proxyUrlPrefixToRemove);
      return this;
    }

    @Override
    public ResponseDefinition build() {
      return super.build(
          builder.getAdditionalProxyRequestHeaders(),
          builder.getRemoveProxyRequestHeaders(),
          builder.getProxyUrlPrefixToRemove());
    }
  }

  public ResponseDefinitionBuilder withFault(Fault fault) {
    builder.setFault(fault);
    return this;
  }

  public ResponseDefinition build() {
    return build(new HttpHeaders(), List.of(), null);
  }

  protected ResponseDefinition build(
      HttpHeaders additionalProxyRequestHeaders,
      List<String> removeProxyRequestHeaders,
      String proxyUrlPrefixToRemove) {
    return builder
        .setAdditionalProxyRequestHeaders(additionalProxyRequestHeaders)
        .setRemoveProxyRequestHeaders(removeProxyRequestHeaders)
        .setProxyUrlPrefixToRemove(proxyUrlPrefixToRemove)
        .build();
  }
}
