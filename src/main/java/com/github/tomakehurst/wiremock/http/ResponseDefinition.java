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
package com.github.tomakehurst.wiremock.http;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static com.github.tomakehurst.wiremock.common.ContentTypes.LOCATION;
import static com.github.tomakehurst.wiremock.common.Strings.removeStart;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.Parameters;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** The type Response definition. */
public class ResponseDefinition {

  private final int status;
  private final String statusMessage;
  private final Body body;
  private final String bodyFileName;
  private final HttpHeaders headers;
  private final HttpHeaders additionalProxyRequestHeaders;
  private final List<String> removeProxyRequestHeaders;
  private final Integer fixedDelayMilliseconds;
  private final DelayDistribution delayDistribution;
  private final ChunkedDribbleDelay chunkedDribbleDelay;
  private final String proxyBaseUrl;
  private final String proxyUrlPrefixToRemove;
  private final Fault fault;
  private final List<String> transformers;
  private final Parameters transformerParameters;

  private String browserProxyUrl;
  private Boolean wasConfigured = true;
  private Request originalRequest;

  /**
   * Instantiates a new Response definition.
   *
   * @param status the status
   * @param statusMessage the status message
   * @param body the body
   * @param jsonBody the json body
   * @param base64Body the base 64 body
   * @param bodyFileName the body file name
   * @param headers the headers
   * @param additionalProxyRequestHeaders the additional proxy request headers
   * @param removeProxyRequestHeaders the remove proxy request headers
   * @param fixedDelayMilliseconds the fixed delay milliseconds
   * @param delayDistribution the delay distribution
   * @param chunkedDribbleDelay the chunked dribble delay
   * @param proxyBaseUrl the proxy base url
   * @param proxyUrlPrefixToRemove the proxy url prefix to remove
   * @param fault the fault
   * @param transformers the transformers
   * @param transformerParameters the transformer parameters
   * @param wasConfigured the was configured
   */
  @JsonCreator
  public ResponseDefinition(
      @JsonProperty("status") int status,
      @JsonProperty("statusMessage") String statusMessage,
      @JsonProperty("body") String body,
      @JsonProperty("jsonBody") JsonNode jsonBody,
      @JsonProperty("base64Body") String base64Body,
      @JsonProperty("bodyFileName") String bodyFileName,
      @JsonProperty("headers") HttpHeaders headers,
      @JsonProperty("additionalProxyRequestHeaders") HttpHeaders additionalProxyRequestHeaders,
      @JsonProperty("removeProxyRequestHeaders") List<String> removeProxyRequestHeaders,
      @JsonProperty("fixedDelayMilliseconds") Integer fixedDelayMilliseconds,
      @JsonProperty("delayDistribution") DelayDistribution delayDistribution,
      @JsonProperty("chunkedDribbleDelay") ChunkedDribbleDelay chunkedDribbleDelay,
      @JsonProperty("proxyBaseUrl") String proxyBaseUrl,
      @JsonProperty("proxyUrlPrefixToRemove") String proxyUrlPrefixToRemove,
      @JsonProperty("fault") Fault fault,
      @JsonProperty("transformers") List<String> transformers,
      @JsonProperty("transformerParameters") Parameters transformerParameters,
      @JsonProperty("fromConfiguredStub") Boolean wasConfigured) {
    this(
        status,
        statusMessage,
        Body.fromOneOf(null, body, jsonBody, base64Body),
        bodyFileName,
        headers,
        additionalProxyRequestHeaders,
        removeProxyRequestHeaders,
        fixedDelayMilliseconds,
        delayDistribution,
        chunkedDribbleDelay,
        proxyBaseUrl,
        proxyUrlPrefixToRemove,
        fault,
        transformers,
        transformerParameters,
        wasConfigured);
  }

  /**
   * Instantiates a new Response definition.
   *
   * @param status the status
   * @param statusMessage the status message
   * @param body the body
   * @param jsonBody the json body
   * @param base64Body the base 64 body
   * @param bodyFileName the body file name
   * @param headers the headers
   * @param additionalProxyRequestHeaders the additional proxy request headers
   * @param removeProxyRequestHeaders the remove proxy request headers
   * @param fixedDelayMilliseconds the fixed delay milliseconds
   * @param delayDistribution the delay distribution
   * @param chunkedDribbleDelay the chunked dribble delay
   * @param proxyBaseUrl the proxy base url
   * @param proxyUrlPrefixToRemove the proxy url prefix to remove
   * @param fault the fault
   * @param transformers the transformers
   * @param transformerParameters the transformer parameters
   * @param wasConfigured the was configured
   */
  public ResponseDefinition(
      int status,
      String statusMessage,
      byte[] body,
      JsonNode jsonBody,
      String base64Body,
      String bodyFileName,
      HttpHeaders headers,
      HttpHeaders additionalProxyRequestHeaders,
      List<String> removeProxyRequestHeaders,
      Integer fixedDelayMilliseconds,
      DelayDistribution delayDistribution,
      ChunkedDribbleDelay chunkedDribbleDelay,
      String proxyBaseUrl,
      String proxyUrlPrefixToRemove,
      Fault fault,
      List<String> transformers,
      Parameters transformerParameters,
      Boolean wasConfigured) {
    this(
        status,
        statusMessage,
        Body.fromOneOf(body, null, jsonBody, base64Body),
        bodyFileName,
        headers,
        additionalProxyRequestHeaders,
        removeProxyRequestHeaders,
        fixedDelayMilliseconds,
        delayDistribution,
        chunkedDribbleDelay,
        proxyBaseUrl,
        proxyUrlPrefixToRemove,
        fault,
        transformers,
        transformerParameters,
        wasConfigured);
  }

  /**
   * Instantiates a new Response definition.
   *
   * @param status the status
   * @param statusMessage the status message
   * @param body the body
   * @param bodyFileName the body file name
   * @param headers the headers
   * @param additionalProxyRequestHeaders the additional proxy request headers
   * @param removeProxyRequestHeaders the remove proxy request headers
   * @param fixedDelayMilliseconds the fixed delay milliseconds
   * @param delayDistribution the delay distribution
   * @param chunkedDribbleDelay the chunked dribble delay
   * @param proxyBaseUrl the proxy base url
   * @param proxyUrlPrefixToRemove the proxy url prefix to remove
   * @param fault the fault
   * @param transformers the transformers
   * @param transformerParameters the transformer parameters
   * @param wasConfigured the was configured
   */
  public ResponseDefinition(
      int status,
      String statusMessage,
      Body body,
      String bodyFileName,
      HttpHeaders headers,
      HttpHeaders additionalProxyRequestHeaders,
      List<String> removeProxyRequestHeaders,
      Integer fixedDelayMilliseconds,
      DelayDistribution delayDistribution,
      ChunkedDribbleDelay chunkedDribbleDelay,
      String proxyBaseUrl,
      String proxyUrlPrefixToRemove,
      Fault fault,
      List<String> transformers,
      Parameters transformerParameters,
      Boolean wasConfigured) {
    this.status = status > 0 ? status : 200;
    this.statusMessage = statusMessage;

    this.body = body;
    this.bodyFileName = bodyFileName;

    this.headers = headers;
    this.additionalProxyRequestHeaders = additionalProxyRequestHeaders;
    this.removeProxyRequestHeaders = removeProxyRequestHeaders;
    this.fixedDelayMilliseconds = fixedDelayMilliseconds;
    this.delayDistribution = delayDistribution;
    this.chunkedDribbleDelay = chunkedDribbleDelay;
    this.proxyBaseUrl = proxyBaseUrl == null ? null : proxyBaseUrl.trim();
    this.proxyUrlPrefixToRemove = proxyUrlPrefixToRemove;
    this.fault = fault;
    this.transformers = transformers;
    this.transformerParameters = transformerParameters;
    this.wasConfigured = wasConfigured == null ? true : wasConfigured;
  }

  /**
   * Instantiates a new Response definition.
   *
   * @param statusCode the status code
   * @param bodyContent the body content
   */
  public ResponseDefinition(final int statusCode, final String bodyContent) {
    this(
        statusCode,
        null,
        Body.fromString(bodyContent),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        Collections.emptyList(),
        Parameters.empty(),
        true);
  }

  /**
   * Instantiates a new Response definition.
   *
   * @param statusCode the status code
   * @param bodyContent the body content
   */
  public ResponseDefinition(final int statusCode, final byte[] bodyContent) {
    this(
        statusCode,
        null,
        Body.fromBytes(bodyContent),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        Collections.emptyList(),
        Parameters.empty(),
        true);
  }

  /** Instantiates a new Response definition. */
  public ResponseDefinition() {
    this(
        HTTP_OK,
        null,
        Body.none(),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        Collections.emptyList(),
        Parameters.empty(),
        true);
  }

  /**
   * Not found response definition.
   *
   * @return the response definition
   */
  public static ResponseDefinition notFound() {
    return new ResponseDefinition(HTTP_NOT_FOUND, (byte[]) null);
  }

  /**
   * Ok response definition.
   *
   * @return the response definition
   */
  public static ResponseDefinition ok() {
    return new ResponseDefinition(HTTP_OK, (byte[]) null);
  }

  /**
   * Ok empty json response definition.
   *
   * @return the response definition
   */
  public static ResponseDefinition okEmptyJson() {
    return ResponseDefinitionBuilder.okForEmptyJson().build();
  }

  /**
   * Ok for json response definition.
   *
   * @param <T> the type parameter
   * @param body the body
   * @return the response definition
   */
  public static <T> ResponseDefinition okForJson(T body) {
    return ResponseDefinitionBuilder.okForJson(body).build();
  }

  /**
   * Created response definition.
   *
   * @return the response definition
   */
  public static ResponseDefinition created() {
    return new ResponseDefinition(HTTP_CREATED, (byte[]) null);
  }

  /**
   * No content response definition.
   *
   * @return the response definition
   */
  public static ResponseDefinition noContent() {
    return new ResponseDefinition(HTTP_NO_CONTENT, (byte[]) null);
  }

  /**
   * Bad request response definition.
   *
   * @param errors the errors
   * @return the response definition
   */
  public static ResponseDefinition badRequest(Errors errors) {
    return ResponseDefinitionBuilder.responseDefinition()
        .withStatus(400)
        .withHeader(CONTENT_TYPE, "application/json")
        .withBody(Json.write(errors))
        .build();
  }

  /**
   * Bad request entity response definition.
   *
   * @param errors the errors
   * @return the response definition
   */
  public static ResponseDefinition badRequestEntity(Errors errors) {
    return ResponseDefinitionBuilder.responseDefinition()
        .withStatus(422)
        .withHeader(CONTENT_TYPE, "application/json")
        .withBody(Json.write(errors))
        .build();
  }

  /**
   * Redirect to response definition.
   *
   * @param path the path
   * @return the response definition
   */
  public static ResponseDefinition redirectTo(String path) {
    return new ResponseDefinitionBuilder()
        .withHeader(LOCATION, path)
        .withStatus(HTTP_MOVED_TEMP)
        .build();
  }

  /**
   * Not configured response definition.
   *
   * @return the response definition
   */
  public static ResponseDefinition notConfigured() {
    final ResponseDefinition response = new ResponseDefinition(HTTP_NOT_FOUND, (byte[]) null);
    response.wasConfigured = false;
    return response;
  }

  /**
   * Not authorised response definition.
   *
   * @return the response definition
   */
  public static ResponseDefinition notAuthorised() {
    return new ResponseDefinition(HTTP_UNAUTHORIZED, (byte[]) null);
  }

  /**
   * Not permitted response definition.
   *
   * @param message the message
   * @return the response definition
   */
  public static ResponseDefinition notPermitted(String message) {
    return notPermitted(Errors.single(40, message));
  }

  /**
   * Not permitted response definition.
   *
   * @param errors the errors
   * @return the response definition
   */
  public static ResponseDefinition notPermitted(Errors errors) {
    return ResponseDefinitionBuilder.jsonResponse(errors, HTTP_FORBIDDEN);
  }

  /**
   * Browser proxy response definition.
   *
   * @param originalRequest the original request
   * @return the response definition
   */
  public static ResponseDefinition browserProxy(Request originalRequest) {
    final ResponseDefinition response = new ResponseDefinition();
    response.browserProxyUrl = originalRequest.getAbsoluteUrl();
    return response;
  }

  /**
   * Copy of response definition.
   *
   * @param original the original
   * @return the response definition
   */
  public static ResponseDefinition copyOf(ResponseDefinition original) {
    return original.copy();
  }

  /**
   * Copy response definition.
   *
   * @return the response definition
   */
  public ResponseDefinition copy() {
    ResponseDefinition newResponseDef =
        new ResponseDefinition(
            this.status,
            this.statusMessage,
            this.body,
            this.bodyFileName,
            this.headers,
            this.additionalProxyRequestHeaders,
            this.removeProxyRequestHeaders,
            this.fixedDelayMilliseconds,
            this.delayDistribution,
            this.chunkedDribbleDelay,
            this.proxyBaseUrl,
            this.proxyUrlPrefixToRemove,
            this.fault,
            this.transformers,
            this.transformerParameters,
            this.wasConfigured);
    return newResponseDef;
  }

  /**
   * Gets headers.
   *
   * @return the headers
   */
  public HttpHeaders getHeaders() {
    return headers;
  }

  /**
   * Gets additional proxy request headers.
   *
   * @return the additional proxy request headers
   */
  public HttpHeaders getAdditionalProxyRequestHeaders() {
    return additionalProxyRequestHeaders;
  }

  /**
   * Gets remove proxy request headers.
   *
   * @return the remove proxy request headers
   */
  public List<String> getRemoveProxyRequestHeaders() {
    return removeProxyRequestHeaders;
  }

  /**
   * Gets status.
   *
   * @return the status
   */
  public int getStatus() {
    return status;
  }

  /**
   * Gets status message.
   *
   * @return the status message
   */
  public String getStatusMessage() {
    return statusMessage;
  }

  /**
   * Gets body.
   *
   * @return the body
   */
  public String getBody() {
    return (!body.isBinary() && !body.isJson()) ? body.asString() : null;
  }

  /**
   * Gets text body.
   *
   * @return the text body
   */
  @JsonIgnore
  public String getTextBody() {
    return !body.isBinary() ? body.asString() : null;
  }

  /**
   * Get byte body byte [ ].
   *
   * @return the byte [ ]
   */
  @JsonIgnore
  public byte[] getByteBody() {
    return body.asBytes();
  }

  /**
   * Gets base 64 body.
   *
   * @return the base 64 body
   */
  public String getBase64Body() {
    return body.isBinary() ? body.asBase64() : null;
  }

  /**
   * Gets reponse body.
   *
   * @return the reponse body
   */
  @JsonIgnore
  public Body getReponseBody() {
    return body;
  }

  /**
   * Gets json body.
   *
   * @return the json body
   */
  public JsonNode getJsonBody() {

    return body.isJson() ? body.asJson() : null;
  }

  /**
   * Gets body file name.
   *
   * @return the body file name
   */
  public String getBodyFileName() {
    return bodyFileName;
  }

  /**
   * Was configured boolean.
   *
   * @return the boolean
   */
  public boolean wasConfigured() {
    return wasConfigured == null || wasConfigured;
  }

  /**
   * Is from configured stub boolean.
   *
   * @return the boolean
   */
  public Boolean isFromConfiguredStub() {
    return wasConfigured == null || wasConfigured ? null : false;
  }

  /**
   * Gets fixed delay milliseconds.
   *
   * @return the fixed delay milliseconds
   */
  public Integer getFixedDelayMilliseconds() {
    return fixedDelayMilliseconds;
  }

  /**
   * Gets delay distribution.
   *
   * @return the delay distribution
   */
  public DelayDistribution getDelayDistribution() {
    return delayDistribution;
  }

  /**
   * Gets chunked dribble delay.
   *
   * @return the chunked dribble delay
   */
  public ChunkedDribbleDelay getChunkedDribbleDelay() {
    return chunkedDribbleDelay;
  }

  /**
   * Gets proxy url.
   *
   * @return the proxy url
   */
  @JsonIgnore
  public String getProxyUrl() {
    if (browserProxyUrl != null) {
      return browserProxyUrl;
    }

    String originalRequestUrl =
        Optional.ofNullable(originalRequest).map(Request::getUrl).orElse("");
    return proxyBaseUrl + removeStart(originalRequestUrl, proxyUrlPrefixToRemove);
  }

  /**
   * Gets proxy base url.
   *
   * @return the proxy base url
   */
  public String getProxyBaseUrl() {
    return proxyBaseUrl;
  }

  /**
   * Gets proxy url prefix to remove.
   *
   * @return the proxy url prefix to remove
   */
  public String getProxyUrlPrefixToRemove() {
    return proxyUrlPrefixToRemove;
  }

  /**
   * Specifies body file boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean specifiesBodyFile() {
    return bodyFileName != null && body.isAbsent();
  }

  /**
   * Specifies body content boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean specifiesBodyContent() {
    return body.isPresent();
  }

  /**
   * Specifies text body content boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean specifiesTextBodyContent() {
    return body.isPresent() && !body.isBinary();
  }

  /**
   * Specifies binary body content boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean specifiesBinaryBodyContent() {
    return (body.isPresent() && body.isBinary());
  }

  /**
   * Is proxy response boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean isProxyResponse() {
    return browserProxyUrl != null || proxyBaseUrl != null;
  }

  /**
   * Gets original request.
   *
   * @return the original request
   */
  @JsonIgnore
  public Request getOriginalRequest() {
    return originalRequest;
  }

  /**
   * Sets original request.
   *
   * @param originalRequest the original request
   */
  public void setOriginalRequest(final Request originalRequest) {
    this.originalRequest = originalRequest;
  }

  /**
   * Gets fault.
   *
   * @return the fault
   */
  public Fault getFault() {
    return fault;
  }

  /**
   * Gets transformers.
   *
   * @return the transformers
   */
  @JsonInclude(NON_EMPTY)
  public List<String> getTransformers() {
    return transformers;
  }

  /**
   * Gets transformer parameters.
   *
   * @return the transformer parameters
   */
  @JsonInclude(NON_EMPTY)
  public Parameters getTransformerParameters() {
    return transformerParameters;
  }

  /**
   * Has transformer boolean.
   *
   * @param transformer the transformer
   * @return the boolean
   */
  public boolean hasTransformer(Extension transformer) {
    return transformers != null && transformers.contains(transformer.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResponseDefinition that = (ResponseDefinition) o;
    return status == that.status
        && Objects.equals(statusMessage, that.statusMessage)
        && Objects.equals(body, that.body)
        && Objects.equals(bodyFileName, that.bodyFileName)
        && Objects.equals(headers, that.headers)
        && Objects.equals(additionalProxyRequestHeaders, that.additionalProxyRequestHeaders)
        && Objects.equals(removeProxyRequestHeaders, that.removeProxyRequestHeaders)
        && Objects.equals(fixedDelayMilliseconds, that.fixedDelayMilliseconds)
        && Objects.equals(delayDistribution, that.delayDistribution)
        && Objects.equals(chunkedDribbleDelay, that.chunkedDribbleDelay)
        && Objects.equals(proxyBaseUrl, that.proxyBaseUrl)
        && Objects.equals(proxyUrlPrefixToRemove, that.proxyUrlPrefixToRemove)
        && fault == that.fault
        && Objects.equals(transformers, that.transformers)
        && Objects.equals(transformerParameters, that.transformerParameters)
        && Objects.equals(browserProxyUrl, that.browserProxyUrl)
        && Objects.equals(wasConfigured, that.wasConfigured);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        status,
        statusMessage,
        body,
        bodyFileName,
        headers,
        additionalProxyRequestHeaders,
        removeProxyRequestHeaders,
        fixedDelayMilliseconds,
        delayDistribution,
        chunkedDribbleDelay,
        proxyBaseUrl,
        proxyUrlPrefixToRemove,
        fault,
        transformers,
        transformerParameters,
        browserProxyUrl,
        wasConfigured);
  }

  @Override
  public String toString() {
    return this.wasConfigured ? Json.write(this) : "(no response definition configured)";
  }
}
