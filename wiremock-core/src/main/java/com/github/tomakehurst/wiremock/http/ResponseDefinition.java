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
package com.github.tomakehurst.wiremock.http;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static com.github.tomakehurst.wiremock.common.ContentTypes.LOCATION;
import static java.net.HttpURLConnection.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.Parameters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;

@JsonInclude(Include.NON_NULL)
public class ResponseDefinition {

  private final int status;
  private final String statusMessage;
  private final Body body;
  private final String bodyFileName;
  @NonNull private final HttpHeaders headers;
  @NonNull private final HttpHeaders additionalProxyRequestHeaders;
  @NonNull private final List<String> removeProxyRequestHeaders;
  private final Integer fixedDelayMilliseconds;
  private final DelayDistribution delayDistribution;
  private final ChunkedDribbleDelay chunkedDribbleDelay;
  private final String proxyBaseUrl;
  private final String proxyUrlPrefixToRemove;
  private final Fault fault;
  @NonNull private final List<String> transformers;
  @NonNull private final Parameters transformerParameters;

  private final String browserProxyUrl;
  private final Boolean wasConfigured;

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
        null,
        wasConfigured);
  }

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
      String browserProxyUrl,
      Boolean wasConfigured) {
    this.status = status > 0 ? status : 200;
    this.statusMessage = statusMessage;

    this.body = body;
    this.bodyFileName = bodyFileName;

    this.headers = headers != null ? headers : new HttpHeaders();
    this.additionalProxyRequestHeaders =
        additionalProxyRequestHeaders != null ? additionalProxyRequestHeaders : new HttpHeaders();
    this.removeProxyRequestHeaders =
        removeProxyRequestHeaders != null ? List.copyOf(removeProxyRequestHeaders) : List.of();
    this.fixedDelayMilliseconds = fixedDelayMilliseconds;
    this.delayDistribution = delayDistribution;
    this.chunkedDribbleDelay = chunkedDribbleDelay;
    this.proxyBaseUrl = proxyBaseUrl == null ? null : proxyBaseUrl.trim();
    this.proxyUrlPrefixToRemove = proxyUrlPrefixToRemove;
    this.fault = fault;
    this.transformers = transformers != null ? List.copyOf(transformers) : List.of();
    this.transformerParameters =
        transformerParameters != null ? transformerParameters : Parameters.empty();
    this.browserProxyUrl = browserProxyUrl;
    this.wasConfigured = wasConfigured == null || wasConfigured;
  }

  public static ResponseDefinition notFound() {
    return new Builder().setStatus(HTTP_NOT_FOUND).build();
  }

  public static ResponseDefinition ok() {
    return new Builder().setStatus(HTTP_OK).build();
  }

  public static ResponseDefinition okEmptyJson() {
    return ResponseDefinitionBuilder.okForEmptyJson().build();
  }

  public static <T> ResponseDefinition okForJson(T body) {
    return ResponseDefinitionBuilder.okForJson(body).build();
  }

  public static ResponseDefinition created() {
    return new Builder().setStatus(HTTP_CREATED).build();
  }

  public static ResponseDefinition noContent() {
    return new Builder().setStatus(HTTP_NO_CONTENT).build();
  }

  public static ResponseDefinition badRequest(Errors errors) {
    return ResponseDefinitionBuilder.responseDefinition()
        .withStatus(400)
        .withHeader(CONTENT_TYPE, "application/json")
        .withBody(Json.write(errors))
        .build();
  }

  public static ResponseDefinition badRequestEntity(Errors errors) {
    return ResponseDefinitionBuilder.responseDefinition()
        .withStatus(422)
        .withHeader(CONTENT_TYPE, "application/json")
        .withBody(Json.write(errors))
        .build();
  }

  public static ResponseDefinition redirectTo(String path) {
    return new ResponseDefinitionBuilder()
        .withHeader(LOCATION, path)
        .withStatus(HTTP_MOVED_TEMP)
        .build();
  }

  public static ResponseDefinition notConfigured() {
    return new Builder().setStatus(HTTP_NOT_FOUND).setWasConfigured(false).build();
  }

  public static ResponseDefinition notAuthorised() {
    return new Builder().setStatus(HTTP_UNAUTHORIZED).build();
  }

  public static ResponseDefinition notPermitted(String message) {
    return notPermitted(Errors.single(40, message));
  }

  public static ResponseDefinition notPermitted(Errors errors) {
    return ResponseDefinitionBuilder.jsonResponse(errors, HTTP_FORBIDDEN);
  }

  public static ResponseDefinition serverError() {
    return ResponseDefinitionBuilder.responseDefinition().withStatus(HTTP_INTERNAL_ERROR).build();
  }

  public static ResponseDefinition browserProxy(Request originalRequest) {
    return new Builder()
        .setBrowserProxyUrl(originalRequest.getTypedAbsoluteUrl().toString())
        .build();
  }

  public static ResponseDefinition copyOf(ResponseDefinition original) {
    return original.copy();
  }

  public ResponseDefinition copy() {
    return new ResponseDefinition(
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
        null,
        this.wasConfigured);
  }

  public ResponseDefinition transform(Consumer<Builder> transformer) {
    final Builder builder = toBuilder();
    transformer.accept(builder);
    return builder.build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @JsonInclude(Include.NON_EMPTY)
  @NonNull
  public HttpHeaders getHeaders() {
    return headers;
  }

  @JsonInclude(Include.NON_EMPTY)
  @NonNull
  public HttpHeaders getAdditionalProxyRequestHeaders() {
    return additionalProxyRequestHeaders;
  }

  @JsonInclude(Include.NON_EMPTY)
  @NonNull
  public List<String> getRemoveProxyRequestHeaders() {
    return removeProxyRequestHeaders;
  }

  public int getStatus() {
    return status;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public String getBody() {
    return (!body.isBinary() && !body.isJson()) ? body.asString() : null;
  }

  @JsonIgnore
  public String getTextBody() {
    return !body.isBinary() ? body.asString() : null;
  }

  @JsonIgnore
  public byte[] getByteBody() {
    return body.asBytes();
  }

  @JsonIgnore
  public byte[] getByteBodyIfBinary() {
    return body.isBinary() ? body.asBytes() : null;
  }

  public String getBase64Body() {
    return body.isBinary() ? body.asBase64() : null;
  }

  @JsonIgnore
  public Body getReponseBody() {
    return body;
  }

  public JsonNode getJsonBody() {

    return body.isJson() ? body.asJson() : null;
  }

  public String getBodyFileName() {
    return bodyFileName;
  }

  public boolean wasConfigured() {
    return wasConfigured == null || wasConfigured;
  }

  public Boolean isFromConfiguredStub() {
    return wasConfigured == null || wasConfigured ? null : false;
  }

  public Integer getFixedDelayMilliseconds() {
    return fixedDelayMilliseconds;
  }

  public DelayDistribution getDelayDistribution() {
    return delayDistribution;
  }

  public ChunkedDribbleDelay getChunkedDribbleDelay() {
    return chunkedDribbleDelay;
  }

  public String getProxyBaseUrl() {
    return proxyBaseUrl;
  }

  public String getProxyUrlPrefixToRemove() {
    return proxyUrlPrefixToRemove;
  }

  @JsonIgnore
  public boolean specifiesBodyFile() {
    return bodyFileName != null && body.isAbsent();
  }

  @JsonIgnore
  public boolean specifiesBodyContent() {
    return body.isPresent();
  }

  @JsonIgnore
  public boolean specifiesTextBodyContent() {
    return body.isPresent() && !body.isBinary();
  }

  @JsonIgnore
  public boolean specifiesBinaryBodyContent() {
    return (body.isPresent() && body.isBinary());
  }

  @JsonIgnore
  public boolean isProxyResponse() {
    return browserProxyUrl != null || proxyBaseUrl != null;
  }

  @JsonIgnore
  public String getBrowserProxyUrl() {
    return browserProxyUrl;
  }

  public Fault getFault() {
    return fault;
  }

  @JsonInclude(NON_EMPTY)
  @NonNull
  public List<String> getTransformers() {
    return transformers;
  }

  @JsonInclude(NON_EMPTY)
  @NonNull
  public Parameters getTransformerParameters() {
    return transformerParameters;
  }

  public boolean hasTransformer(Extension transformer) {
    return transformers.contains(transformer.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
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

  @SuppressWarnings("UnusedReturnValue")
  public static class Builder {
    private int status = 200;
    private String statusMessage;
    private Body body = Body.none();
    private String bodyFileName;
    @NonNull private HttpHeaders headers = new HttpHeaders();
    @NonNull private HttpHeaders additionalProxyRequestHeaders = new HttpHeaders();
    @NonNull private List<String> removeProxyRequestHeaders = new ArrayList<>();
    private Integer fixedDelayMilliseconds;
    private DelayDistribution delayDistribution;
    private ChunkedDribbleDelay chunkedDribbleDelay;
    private String proxyBaseUrl;
    private String proxyUrlPrefixToRemove;
    private Fault fault;
    @NonNull private List<String> transformers = new ArrayList<>();
    @NonNull private Parameters transformerParameters = Parameters.empty();
    private String browserProxyUrl;
    private Boolean wasConfigured = true;
    private Request originalRequest;

    public Builder() {}

    public Builder(ResponseDefinition original) {
      this.status = original.status;
      this.statusMessage = original.statusMessage;
      this.body = original.body;
      this.bodyFileName = original.bodyFileName;
      this.headers = original.headers;
      this.additionalProxyRequestHeaders = original.additionalProxyRequestHeaders;
      this.removeProxyRequestHeaders.addAll(original.removeProxyRequestHeaders);
      this.fixedDelayMilliseconds = original.fixedDelayMilliseconds;
      this.delayDistribution = original.delayDistribution;
      this.chunkedDribbleDelay = original.chunkedDribbleDelay;
      this.proxyBaseUrl = original.proxyBaseUrl;
      this.proxyUrlPrefixToRemove = original.proxyUrlPrefixToRemove;
      this.fault = original.fault;
      this.transformers.addAll(original.transformers);
      this.transformerParameters = original.transformerParameters;
      this.browserProxyUrl = original.browserProxyUrl;
      this.wasConfigured = original.wasConfigured;
    }

    public int getStatus() {
      return status;
    }

    public String getStatusMessage() {
      return statusMessage;
    }

    public Body getBody() {
      return body;
    }

    public String getBodyFileName() {
      return bodyFileName;
    }

    @NonNull
    public HttpHeaders getHeaders() {
      return headers;
    }

    @NonNull
    public HttpHeaders getAdditionalProxyRequestHeaders() {
      return additionalProxyRequestHeaders;
    }

    @NonNull
    public List<String> getRemoveProxyRequestHeaders() {
      return removeProxyRequestHeaders;
    }

    public Integer getFixedDelayMilliseconds() {
      return fixedDelayMilliseconds;
    }

    public DelayDistribution getDelayDistribution() {
      return delayDistribution;
    }

    public ChunkedDribbleDelay getChunkedDribbleDelay() {
      return chunkedDribbleDelay;
    }

    public String getProxyBaseUrl() {
      return proxyBaseUrl;
    }

    public String getProxyUrlPrefixToRemove() {
      return proxyUrlPrefixToRemove;
    }

    public Fault getFault() {
      return fault;
    }

    @NonNull
    public List<String> getTransformers() {
      return transformers;
    }

    @NonNull
    public Parameters getTransformerParameters() {
      return transformerParameters;
    }

    public String getBrowserProxyUrl() {
      return browserProxyUrl;
    }

    public Boolean getWasConfigured() {
      return wasConfigured;
    }

    public Request getOriginalRequest() {
      return originalRequest;
    }

    public Builder setStatus(int status) {
      this.status = status;
      return this;
    }

    public Builder setStatusMessage(String statusMessage) {
      this.statusMessage = statusMessage;
      return this;
    }

    public Builder setBody(Body body) {
      this.body = body;
      return this;
    }

    public Builder setBodyFileName(String bodyFileName) {
      this.bodyFileName = bodyFileName;
      return this;
    }

    public Builder setHeaders(@NonNull HttpHeaders headers) {
      Objects.requireNonNull(headers);
      this.headers = headers;
      return this;
    }

    public Builder headers(Consumer<HttpHeaders.Builder> transformer) {
      this.headers = headers.transform(transformer);
      return this;
    }

    public Builder setAdditionalProxyRequestHeaders(
        @NonNull HttpHeaders additionalProxyRequestHeaders) {
      Objects.requireNonNull(additionalProxyRequestHeaders);
      this.additionalProxyRequestHeaders = additionalProxyRequestHeaders;
      return this;
    }

    public Builder setRemoveProxyRequestHeaders(@NonNull List<String> removeProxyRequestHeaders) {
      Objects.requireNonNull(removeProxyRequestHeaders);
      this.removeProxyRequestHeaders = removeProxyRequestHeaders;
      return this;
    }

    public Builder setFixedDelayMilliseconds(Integer fixedDelayMilliseconds) {
      this.fixedDelayMilliseconds = fixedDelayMilliseconds;
      return this;
    }

    public Builder setDelayDistribution(DelayDistribution delayDistribution) {
      this.delayDistribution = delayDistribution;
      return this;
    }

    public Builder setChunkedDribbleDelay(ChunkedDribbleDelay chunkedDribbleDelay) {
      this.chunkedDribbleDelay = chunkedDribbleDelay;
      return this;
    }

    public Builder setProxyBaseUrl(String proxyBaseUrl) {
      this.proxyBaseUrl = proxyBaseUrl;
      return this;
    }

    public Builder setProxyUrlPrefixToRemove(String proxyUrlPrefixToRemove) {
      this.proxyUrlPrefixToRemove = proxyUrlPrefixToRemove;
      return this;
    }

    public Builder setFault(Fault fault) {
      this.fault = fault;
      return this;
    }

    public Builder setTransformers(@NonNull List<String> transformers) {
      Objects.requireNonNull(transformers);
      this.transformers = transformers;
      return this;
    }

    public Builder setTransformerParameters(@NonNull Parameters transformerParameters) {
      Objects.requireNonNull(transformerParameters);
      this.transformerParameters = transformerParameters;
      return this;
    }

    public Builder setBrowserProxyUrl(String browserProxyUrl) {
      this.browserProxyUrl = browserProxyUrl;
      return this;
    }

    public Builder setWasConfigured(Boolean wasConfigured) {
      this.wasConfigured = wasConfigured;
      return this;
    }

    public Builder setOriginalRequest(Request originalRequest) {
      this.originalRequest = originalRequest;
      return this;
    }

    public ResponseDefinition build() {
      return new ResponseDefinition(
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
  }
}
