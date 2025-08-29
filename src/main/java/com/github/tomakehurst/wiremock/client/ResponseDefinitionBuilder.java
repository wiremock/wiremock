/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_ENCODING;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ChunkedDribbleDelay;
import com.github.tomakehurst.wiremock.http.DelayDistribution;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.LogNormal;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.http.UniformDistribution;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A builder for creating {@link ResponseDefinition} instances.
 *
 * <p>This class provides a fluent API to define all aspects of an HTTP response, including status,
 * headers, body, delays, and proxying behavior. It is the primary way to define the {@code
 * willReturn} part of a stub mapping.
 *
 * @see ResponseDefinition
 */
public class ResponseDefinitionBuilder {

  protected int status = HTTP_OK;
  protected String statusMessage;
  protected Body body = Body.none();
  protected String bodyFileName;
  protected List<HttpHeader> headers = new ArrayList<>();
  protected Integer fixedDelayMilliseconds;
  protected DelayDistribution delayDistribution;
  protected ChunkedDribbleDelay chunkedDribbleDelay;
  protected String proxyBaseUrl;
  protected String proxyUrlPrefixToRemove;
  protected Fault fault;
  protected List<String> responseTransformerNames;
  protected Map<String, Object> transformerParameters = new HashMap<>();
  protected Boolean wasConfigured = true;

  /**
   * Creates a new builder instance pre-configured with the details of an existing response
   * definition.
   *
   * @param responseDefinition The response definition to copy.
   * @return A new builder instance.
   */
  public static ResponseDefinitionBuilder like(ResponseDefinition responseDefinition) {
    ResponseDefinitionBuilder builder = new ResponseDefinitionBuilder();
    builder.status = responseDefinition.getStatus();
    builder.statusMessage = responseDefinition.getStatusMessage();
    builder.headers =
        responseDefinition.getHeaders() != null
            ? new ArrayList<>(responseDefinition.getHeaders().all())
            : new ArrayList<>();
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
      ProxyResponseDefinitionBuilder proxyResponseDefinitionBuilder =
          new ProxyResponseDefinitionBuilder(builder);
      proxyResponseDefinitionBuilder.proxyUrlPrefixToRemove =
          responseDefinition.getProxyUrlPrefixToRemove();
      proxyResponseDefinitionBuilder.additionalRequestHeaders =
          responseDefinition.getAdditionalProxyRequestHeaders() != null
              ? (List<HttpHeader>) responseDefinition.getAdditionalProxyRequestHeaders().all()
              : new ArrayList<>();
      proxyResponseDefinitionBuilder.removeRequestHeaders =
          responseDefinition.getRemoveProxyRequestHeaders() != null
              ? responseDefinition.getRemoveProxyRequestHeaders()
              : new ArrayList<>();

      return proxyResponseDefinitionBuilder;
    }

    return builder;
  }

  /**
   * A convenience method for creating a JSON response with a 200 OK status.
   *
   * @param body The JSON body content.
   * @return A complete {@link ResponseDefinition}.
   */
  public static ResponseDefinition jsonResponse(Object body) {
    return jsonResponse(body, HTTP_OK);
  }

  /**
   * A convenience method for creating a JSON response with a specified status.
   *
   * @param body The JSON body content.
   * @param status The HTTP status code.
   * @return A complete {@link ResponseDefinition}.
   */
  public static ResponseDefinition jsonResponse(Object body, int status) {
    return new ResponseDefinitionBuilder()
        .withBody(Json.write(body))
        .withStatus(status)
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .build();
  }

  /**
   * A no-op method to make the DSL more readable.
   *
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder but() {
    return this;
  }

  /**
   * Sets the HTTP status code of the response.
   *
   * @param status The status code.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withStatus(int status) {
    this.status = status;
    return this;
  }

  /**
   * Adds an HTTP header to the response.
   *
   * @param key The header name.
   * @param values The header value(s).
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withHeader(String key, String... values) {
    headers.add(new HttpHeader(key, values));
    return this;
  }

  /**
   * Sets the response body from a file. The file is resolved relative to the `__files` directory.
   *
   * @param fileName The name of the file.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withBodyFile(String fileName) {
    this.bodyFileName = fileName;
    return this;
  }

  /**
   * Sets the response body as a string.
   *
   * @param body The body content.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withBody(String body) {
    this.body = Body.fromOneOf(null, body, null, null);
    return this;
  }

  /**
   * Sets the response body as a byte array.
   *
   * @param body The body content.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withBody(byte[] body) {
    this.body = Body.fromOneOf(body, null, null, null);
    return this;
  }

  /**
   * Sets the response body.
   *
   * @param body The body content.
   */
  public void withResponseBody(Body body) {
    this.body = body;
  }

  /**
   * Sets the response body from a Jackson {@link JsonNode}.
   *
   * @param jsonBody The JSON body content.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withJsonBody(JsonNode jsonBody) {
    this.body = Body.fromOneOf(null, null, jsonBody, null);
    return this;
  }

  /**
   * Sets a fixed delay for the response.
   *
   * @param milliseconds The delay in milliseconds.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withFixedDelay(Integer milliseconds) {
    this.fixedDelayMilliseconds = milliseconds;
    return this;
  }

  /**
   * Sets a random delay for the response based on a distribution.
   *
   * @param distribution The delay distribution.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withRandomDelay(DelayDistribution distribution) {
    this.delayDistribution = distribution;
    return this;
  }

  /**
   * Sets a log-normal random delay for the response.
   *
   * @param medianMilliseconds The median of the delay distribution.
   * @param sigma The standard deviation of the delay distribution.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withLogNormalRandomDelay(
      double medianMilliseconds, double sigma) {
    return withLogNormalRandomDelay(medianMilliseconds, sigma, null);
  }

  /**
   * Sets a log-normal random delay for the response, truncated at a max value.
   *
   * @param medianMilliseconds The median of the delay distribution.
   * @param sigma The standard deviation of the delay distribution.
   * @param maxValue The maximum possible value of the delay.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withLogNormalRandomDelay(
      double medianMilliseconds, double sigma, Double maxValue) {
    return withRandomDelay(new LogNormal(medianMilliseconds, sigma, maxValue));
  }

  /**
   * Sets a uniform random delay for the response.
   *
   * @param lowerMilliseconds The minimum delay in milliseconds.
   * @param upperMilliseconds The maximum delay in milliseconds.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withUniformRandomDelay(
      int lowerMilliseconds, int upperMilliseconds) {
    return withRandomDelay(new UniformDistribution(lowerMilliseconds, upperMilliseconds));
  }

  /**
   * Causes the response to be "dribbled" over a period of time in chunks.
   *
   * @param numberOfChunks The number of chunks to send.
   * @param totalDuration The total duration in milliseconds.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withChunkedDribbleDelay(int numberOfChunks, int totalDuration) {
    this.chunkedDribbleDelay = new ChunkedDribbleDelay(numberOfChunks, totalDuration);
    return this;
  }

  /**
   * Attaches response transformers to this response.
   *
   * @param responseTransformerNames The names of the transformer extensions.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withTransformers(String... responseTransformerNames) {
    this.responseTransformerNames = asList(responseTransformerNames);
    return this;
  }

  /**
   * Sets all parameters for the response transformers.
   *
   * @param parameters A map of parameters.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withTransformerParameters(Map<String, Object> parameters) {
    transformerParameters.putAll(parameters);
    return this;
  }

  /**
   * Sets a single parameter for a response transformer.
   *
   * @param name The parameter name.
   * @param value The parameter value.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withTransformerParameter(String name, Object value) {
    transformerParameters.put(name, value);
    return this;
  }

  /**
   * A convenience method to attach a single transformer with a single parameter.
   *
   * @param transformerName The name of the transformer extension.
   * @param parameterKey The parameter name.
   * @param parameterValue The parameter value.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withTransformer(
      String transformerName, String parameterKey, Object parameterValue) {
    withTransformers(transformerName);
    withTransformerParameter(parameterKey, parameterValue);
    return this;
  }

  /**
   * Configures this response to proxy requests to the specified URL.
   *
   * @param proxyBaseUrl The base URL to proxy requests to.
   * @return A {@link ProxyResponseDefinitionBuilder} for further proxy-specific configuration.
   */
  public ProxyResponseDefinitionBuilder proxiedFrom(String proxyBaseUrl) {
    this.proxyBaseUrl = proxyBaseUrl;
    return new ProxyResponseDefinitionBuilder(this);
  }

  /**
   * Disables GZIP compression for this response.
   *
   * @param gzipDisabled true to disable GZIP, false otherwise.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withGzipDisabled(boolean gzipDisabled) {
    if (gzipDisabled) {
      this.headers.add(new HttpHeader(CONTENT_ENCODING, "none"));
    }
    return this;
  }

  /**
   * A factory method to create a new, empty builder.
   *
   * @return a new builder instance.
   */
  public static ResponseDefinitionBuilder responseDefinition() {
    return new ResponseDefinitionBuilder();
  }

  /**
   * A convenience method for creating a 200 OK JSON response.
   *
   * @param body The JSON body content.
   * @param <T> The type of the body object.
   * @return A response definition builder.
   */
  public static <T> ResponseDefinitionBuilder okForJson(T body) {
    return responseDefinition()
        .withStatus(HTTP_OK)
        .withBody(Json.write(body))
        .withHeader(CONTENT_TYPE, APPLICATION_JSON);
  }

  /**
   * A convenience method for creating a 200 OK response with an empty JSON object body.
   *
   * @return A response definition builder.
   */
  public static ResponseDefinitionBuilder okForEmptyJson() {
    return responseDefinition()
        .withStatus(HTTP_OK)
        .withBody("{}")
        .withHeader(CONTENT_TYPE, APPLICATION_JSON);
  }

  /**
   * Adds multiple HTTP headers to the response.
   *
   * @param headers The headers to add.
   */
  public void withHeaders(HttpHeaders headers) {
    this.headers = new ArrayList<>(headers.all());
  }

  /**
   * Sets the response body from a Base64-encoded string.
   *
   * @param base64Body The Base64-encoded body content.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withBase64Body(String base64Body) {
    this.body = Body.fromOneOf(null, null, null, base64Body);
    return this;
  }

  /**
   * Sets a custom HTTP status message for the response.
   *
   * @param message The status message.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withStatusMessage(String message) {
    this.statusMessage = message;
    return this;
  }

  /**
   * A specialized builder for configuring proxy responses.
   *
   * <p>It extends {@link ResponseDefinitionBuilder} with methods for manipulating the request
   * before it is sent to the proxy target.
   */
  public static class ProxyResponseDefinitionBuilder extends ResponseDefinitionBuilder {

    private List<HttpHeader> additionalRequestHeaders = new ArrayList<>();
    private List<String> removeRequestHeaders = new ArrayList<>();

    /**
     * Constructs a new ProxyResponseDefinitionBuilder by copying properties from an existing
     * builder.
     *
     * @param from The {@link ResponseDefinitionBuilder} to copy properties from.
     */
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

    /**
     * Adds a header to be sent to the proxy target.
     *
     * @param key The header name.
     * @param value The header value.
     * @return This builder for chaining.
     */
    public ProxyResponseDefinitionBuilder withAdditionalRequestHeader(String key, String value) {
      additionalRequestHeaders.add(new HttpHeader(key, value));
      return this;
    }

    /**
     * Specifies a request header to be removed before sending to the proxy target.
     *
     * @param key The name of the header to remove.
     * @return This builder for chaining.
     */
    public ProxyResponseDefinitionBuilder withRemoveRequestHeader(String key) {
      removeRequestHeaders.add(key.toLowerCase());
      return this;
    }

    /**
     * Specifies a prefix to be removed from the request URL before sending to the proxy target.
     *
     * @param proxyUrlPrefixToRemove The URL prefix to remove.
     * @return This builder for chaining.
     */
    public ProxyResponseDefinitionBuilder withProxyUrlPrefixToRemove(
        String proxyUrlPrefixToRemove) {
      this.proxyUrlPrefixToRemove = proxyUrlPrefixToRemove;
      return this;
    }

    @Override
    public ResponseDefinition build() {
      return super.build(
          !additionalRequestHeaders.isEmpty() ? new HttpHeaders(additionalRequestHeaders) : null,
          !removeRequestHeaders.isEmpty() ? removeRequestHeaders : null,
          proxyUrlPrefixToRemove);
    }
  }

  /**
   * Sets a fault to be returned instead of a normal response.
   *
   * @param fault The fault type.
   * @return This builder for chaining.
   */
  public ResponseDefinitionBuilder withFault(Fault fault) {
    this.fault = fault;
    return this;
  }

  /**
   * Builds the final, configured {@link ResponseDefinition} instance.
   *
   * @return A new {@code ResponseDefinition} instance.
   */
  public ResponseDefinition build() {
    return build(null, null, null);
  }

  /**
   * The internal build method that constructs the final {@link ResponseDefinition}.
   *
   * <p>This method is called by the public {@code build()} and the specialized {@link
   * ProxyResponseDefinitionBuilder} to create the response definition with all configured
   * properties.
   *
   * @param additionalProxyRequestHeaders Headers to add to the request when proxying.
   * @param removeProxyRequestHeaders Headers to remove from the request when proxying.
   * @param proxyUrlPrefixToRemove A URL prefix to remove from the request path when proxying.
   * @return A new {@code ResponseDefinition} instance.
   */
  protected ResponseDefinition build(
      HttpHeaders additionalProxyRequestHeaders,
      List<String> removeProxyRequestHeaders,
      String proxyUrlPrefixToRemove) {
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
        removeProxyRequestHeaders,
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
