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
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.entity.CompressionType;
import com.github.tomakehurst.wiremock.common.entity.EmptyEntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.EntityMetadata;
import com.github.tomakehurst.wiremock.common.entity.Format;
import com.github.tomakehurst.wiremock.common.entity.JsonEntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.SimpleStringEntityDefinition;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.Parameters;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.wiremock.url.AbsoluteUrl;
import org.wiremock.url.Path;

@JsonInclude(Include.NON_NULL)
public class ResponseDefinition {

  private final int status;
  private final String statusMessage;

  private final EntityDefinition body;

  private final @NonNull HttpHeaders headers;
  private final @NonNull HttpHeaders additionalProxyRequestHeaders;
  private final @NonNull List<String> removeProxyRequestHeaders;

  private final Integer fixedDelayMilliseconds;
  private final DelayDistribution delayDistribution;
  private final ChunkedDribbleDelay chunkedDribbleDelay;
  private final @Nullable String proxyBaseUrl;
  private final @Nullable Path proxyUrlPrefixToRemove;
  private final Fault fault;
  private final @NonNull List<String> transformers;
  private final @NonNull Parameters transformerParameters;

  private final @Nullable AbsoluteUrl browserProxyUrl;
  private final Boolean wasConfigured;

  @JsonCreator
  public ResponseDefinition(
      @JsonProperty("status") int status,
      @JsonProperty("statusMessage") String statusMessage,
      @JsonProperty("body") EntityDefinition body,
      @JsonProperty("jsonBody") JsonNode jsonBody,
      @JsonProperty("base64Body") String base64Body,
      @JsonProperty("bodyFileName") String bodyFileName,
      @JsonProperty("bodyMetadata") BodyMetadata bodyMetadata,
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
        resolveBody(body, jsonBody, base64Body, bodyFileName, bodyMetadata),
        headers,
        additionalProxyRequestHeaders,
        removeProxyRequestHeaders,
        fixedDelayMilliseconds,
        delayDistribution,
        chunkedDribbleDelay,
        proxyBaseUrl,
        proxyUrlPrefixToRemove != null ? Path.parse(proxyUrlPrefixToRemove) : null,
        fault,
        transformers,
        transformerParameters,
        null,
        wasConfigured);
  }

  private static EntityDefinition resolveBody(
      EntityDefinition body,
      JsonNode jsonBody,
      String base64Body,
      String bodyFileName,
      BodyMetadata bodyMetadata) {
    EntityDefinition entityDefinition = body;
    if (jsonBody != null) {
      entityDefinition = EntityDefinition.json(jsonBody);
    } else if (base64Body != null) {
      entityDefinition = EntityDefinition.fromBase64(base64Body);
    } else if (bodyFileName != null) {
      entityDefinition = EntityDefinition.builder().setFilePath(bodyFileName).build();
    }

    if (entityDefinition != null && bodyMetadata != null) {
      entityDefinition = entityDefinition.transform(bodyMetadata::applyTo);
    }

    return entityDefinition != null ? entityDefinition : EmptyEntityDefinition.INSTANCE;
  }

  ResponseDefinition(
      int status,
      String statusMessage,
      EntityDefinition body,
      HttpHeaders headers,
      HttpHeaders additionalProxyRequestHeaders,
      List<String> removeProxyRequestHeaders,
      Integer fixedDelayMilliseconds,
      DelayDistribution delayDistribution,
      ChunkedDribbleDelay chunkedDribbleDelay,
      @Nullable String proxyBaseUrl,
      @Nullable Path proxyUrlPrefixToRemove,
      Fault fault,
      List<String> transformers,
      Parameters transformerParameters,
      @Nullable AbsoluteUrl browserProxyUrl,
      Boolean wasConfigured) {
    this.status = status > 0 ? status : 200;
    this.statusMessage = statusMessage;

    this.headers = headers != null ? headers : new HttpHeaders();
    this.additionalProxyRequestHeaders =
        additionalProxyRequestHeaders != null ? additionalProxyRequestHeaders : new HttpHeaders();
    this.removeProxyRequestHeaders =
        removeProxyRequestHeaders != null ? List.copyOf(removeProxyRequestHeaders) : List.of();

    this.body = resolveBodyAttributes(this.headers, body);

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

  private static EntityDefinition resolveBodyAttributes(
      HttpHeaders headers, EntityDefinition entityDefinition) {
    if (entityDefinition.isAbsent()) {
      return entityDefinition;
    }

    return entityDefinition.transform(builder -> EntityMetadata.copyFromHeaders(headers, builder));
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
    return new Builder().setBrowserProxyUrl(originalRequest.getTypedAbsoluteUrl()).build();
  }

  public static ResponseDefinition copyOf(ResponseDefinition original) {
    return original.copy();
  }

  public ResponseDefinition copy() {
    return new ResponseDefinition(
        this.status,
        this.statusMessage,
        this.body,
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
        this.browserProxyUrl,
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

  @JsonSerialize(using = BodySerializer.class)
  @JsonInclude(Include.NON_EMPTY)
  @JsonGetter("body")
  public EntityDefinition getBodyForSerialization() {
    return body;
  }

  // for backwards compatibility
  @JsonIgnore
  public String getBody() {
    if (body.isInline()) {
      return body.getDataAsString();
    }

    return null;
  }

  @JsonIgnore
  public EntityDefinition getBodyEntity() {
    return body;
  }

  @JsonIgnore
  public String getTextBody() {
    if (body.getFormat() != Format.BINARY) {
      return body.getDataAsString();
    }

    return null;
  }

  @JsonIgnore
  public byte[] getByteBody() {
    return body.getDataAsBytes();
  }

  @JsonView({Json.PublicView.class, Json.PrivateView.class})
  public String getBase64Body() {
    if (body.isInline() && (body.isBinary() || body.isCompressed())) {
      return body.getDataAsString();
    }

    return null;
  }

  @JsonView({Json.PublicView.class, Json.PrivateView.class})
  public JsonNode getJsonBody() {
    if (body instanceof JsonEntityDefinition jsonEntity) {
      return jsonEntity.getDataAsJson();
    }

    return null;
  }

  @JsonView({Json.PublicView.class, Json.PrivateView.class})
  public String getBodyFileName() {
    if (body.getFilePath() != null) {
      return body.getFilePath();
    }

    return null;
  }

  @JsonView({Json.PublicView.class, Json.PrivateView.class})
  public BodyMetadata getBodyMetadata() {
    if (body.isAbsent()) {
      return null;
    }

    Format format = body.getFormatForSerialization();
    CompressionType compression = body.getCompression();
    Charset charset = body.getCharset();

    boolean isDefault =
        format == null
            && (compression == null || compression == CompressionType.NONE)
            && (charset == null || charset.equals(UTF_8));

    if (isDefault) {
      return null;
    }

    return new BodyMetadata(format, compression, charset);
  }

  public boolean wasConfigured() {
    return wasConfigured == null || wasConfigured;
  }

  @SuppressWarnings("unused")
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

  public @Nullable String getProxyBaseUrl() {
    return proxyBaseUrl;
  }

  public @Nullable Path getProxyUrlPrefixToRemove() {
    return proxyUrlPrefixToRemove;
  }

  @JsonIgnore
  public boolean specifiesTextBodyContent() {
    return body.getFormat() != Format.BINARY;
  }

  @JsonIgnore
  public boolean specifiesBinaryBodyContent() {
    return body.getFormat() == Format.BINARY;
  }

  @JsonIgnore
  public boolean isProxyResponse() {
    return browserProxyUrl != null || proxyBaseUrl != null;
  }

  @JsonIgnore
  public @Nullable AbsoluteUrl getBrowserProxyUrl() {
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

  static class BodySerializer extends StdSerializer<EntityDefinition> {

    public BodySerializer() {
      super(EntityDefinition.class);
    }

    @Override
    public void serialize(EntityDefinition value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      Class<?> activeView = provider.getActiveView();
      boolean isV4 = activeView != null && Json.V4StyleView.class.isAssignableFrom(activeView);

      if (isV4) {
        if (value instanceof SimpleStringEntityDefinition ssd) {
          EntityDefinition full = EntityDefinition.full(ssd.getText());
          provider.defaultSerializeValue(full, gen);
        } else {
          provider.defaultSerializeValue(value, gen);
        }
      } else {
        gen.writeString(value.getDataAsString());
      }
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, EntityDefinition value) {
      if (value instanceof EmptyEntityDefinition) {
        return true;
      }

      Class<?> activeView = provider.getActiveView();
      boolean isV4 = activeView != null && Json.V4StyleView.class.isAssignableFrom(activeView);

      if (isV4) {
        return false;
      }

      return !isTextBody(value);
    }

    private static boolean isTextBody(EntityDefinition body) {
      return body.isInline()
          && !body.isBinary()
          && !body.isCompressed()
          && !(body instanceof JsonEntityDefinition);
    }
  }

  @SuppressWarnings({"UnusedReturnValue", "unused"})
  public static class Builder {
    private int status = 200;
    private String statusMessage;
    private EntityDefinition body = EmptyEntityDefinition.INSTANCE;
    private String bodyFileName;
    private @NonNull HttpHeaders headers = new HttpHeaders();
    private @NonNull HttpHeaders additionalProxyRequestHeaders = new HttpHeaders();
    private @NonNull List<String> removeProxyRequestHeaders = new ArrayList<>();
    private Integer fixedDelayMilliseconds;
    private DelayDistribution delayDistribution;
    private ChunkedDribbleDelay chunkedDribbleDelay;
    private @Nullable String proxyBaseUrl;
    private @Nullable Path proxyUrlPrefixToRemove = null;
    private Fault fault;
    private @NonNull List<String> transformers = new ArrayList<>();
    private @NonNull Parameters transformerParameters = Parameters.empty();
    private @Nullable AbsoluteUrl browserProxyUrl;
    private Boolean wasConfigured = true;
    private Request originalRequest;

    public Builder() {}

    public Builder(ResponseDefinition original) {
      this.status = original.status;
      this.statusMessage = original.statusMessage;
      this.body = original.body;
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

    public EntityDefinition getBody() {
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

    public @Nullable String getProxyBaseUrl() {
      return proxyBaseUrl;
    }

    public @Nullable Path getProxyUrlPrefixToRemove() {
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

    public @Nullable AbsoluteUrl getBrowserProxyUrl() {
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

    public Builder setBody(String body) {
      this.body = this.body.transform(builder -> builder.setData(body));
      return this;
    }

    public Builder setBody(byte[] body) {
      this.body = this.body.transform(builder -> builder.setData(body));
      return this;
    }

    public Builder setBody(EntityDefinition body) {
      this.body = body;
      return this;
    }

    public Builder setBodyFileName(String bodyFileName) {
      if (bodyFileName != null) {
        this.body = new EntityDefinition.Builder().setFilePath(bodyFileName).build();
      }
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

    public Builder setProxyBaseUrl(AbsoluteUrl proxyBaseUrl) {
      this.proxyBaseUrl = proxyBaseUrl.toString();
      return this;
    }

    public Builder setProxyBaseUrl(String proxyBaseUrl) {
      this.proxyBaseUrl = proxyBaseUrl;
      return this;
    }

    public Builder setProxyUrlPrefixToRemove(@Nullable String proxyUrlPrefixToRemove) {
      Path prefix = proxyUrlPrefixToRemove != null ? Path.parse(proxyUrlPrefixToRemove) : null;
      return setProxyUrlPrefixToRemove(prefix);
    }

    public Builder setProxyUrlPrefixToRemove(@Nullable Path proxyUrlPrefixToRemove) {
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

    public Builder setBrowserProxyUrl(@Nullable AbsoluteUrl browserProxyUrl) {
      this.browserProxyUrl = browserProxyUrl;
      return this;
    }

    public Builder setBrowserProxyUrl(@Nullable String browserProxyUrl) {
      AbsoluteUrl url = browserProxyUrl != null ? AbsoluteUrl.parse(browserProxyUrl) : null;
      return setBrowserProxyUrl(url);
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
