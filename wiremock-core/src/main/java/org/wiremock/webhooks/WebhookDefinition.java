/*
 * Copyright (C) 2021-2026 Thomas Akehurst
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
package org.wiremock.webhooks;

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.entity.EmptyEntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.Format;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.store.Stores;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.wiremock.annotations.PublishedAPI;

@PublishedAPI
public class WebhookDefinition {

  private String method;
  private String url;
  private HttpHeaders headers = new HttpHeaders();

  private EntityDefinition body = EmptyEntityDefinition.INSTANCE;

  private DelayDistribution delay;
  private Parameters parameters;

  public static WebhookDefinition from(Parameters parameters) {
    return Json.mapToObject(parameters, WebhookDefinition.class).withExtraParameters(parameters);
  }

  @JsonCreator
  public WebhookDefinition(
      @JsonProperty("method") String method,
      @JsonProperty("url") String url,
      @JsonProperty("headers") HttpHeaders headers,
      @JsonProperty("body") EntityDefinition body,
      @JsonProperty("bodyFileName") String bodyFileName,
      @JsonProperty("base64Body") String base64Body,
      @JsonProperty("jsonBody") JsonNode jsonBody,
      @JsonProperty("delay") DelayDistribution delay) {
    this(
        method,
        url,
        headers,
        EntityDefinition.resolveFrom(body, jsonBody, base64Body, bodyFileName),
        delay,
        Parameters.empty());
  }

  WebhookDefinition(
      String method,
      String url,
      HttpHeaders headers,
      EntityDefinition body,
      DelayDistribution delay,
      Parameters parameters) {
    this.method = method;
    this.url = url;
    this.headers = getFirstNonNull(headers, new HttpHeaders());
    this.body = body;
    this.delay = delay;
    this.parameters = parameters;
  }

  public WebhookDefinition() {}

  public String getMethod() {
    return method;
  }

  @JsonIgnore
  public RequestMethod getRequestMethod() {
    return RequestMethod.fromString(method);
  }

  public String getUrl() {
    return url;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @NonNull
  public HttpHeaders getHeaders() {
    return headers;
  }

  @JsonIgnore
  public EntityDefinition getBodyEntityDefinition() {
    return body;
  }

  public String getBody() {
    if (!body.isBinary() && body.isInline()) {
      return body.getDataAsString();
    }

    return null;
  }

  @JsonIgnore
  public String getBase64Body() {
    if (body.isBinary() && body.isInline()) {
      return body.getDataAsString();
    }
    return null;
  }

  public String getBodyFileName() {
    return body.getFilePath();
  }

  public DelayDistribution getDelay() {
    return delay;
  }

  @JsonIgnore
  public long getDelaySampleMillis() {
    return delay != null ? delay.sampleMillis() : 0L;
  }

  @JsonIgnore
  public Parameters getExtraParameters() {
    return parameters;
  }

  @SuppressWarnings("unused")
  @JsonIgnore
  public byte[] getBinaryBody() {
    return body.getDataAsBytes();
  }

  @JsonIgnore
  public byte[] getResolvedBody(Stores stores) {
    return body.resolve(stores).getData();
  }

  public WebhookDefinition withMethod(String method) {
    this.method = method;
    return this;
  }

  public WebhookDefinition withMethod(RequestMethod method) {
    this.method = method.getName();
    return this;
  }

  public WebhookDefinition withUrl(URI url) {
    this.url = url.toString();
    return this;
  }

  public WebhookDefinition withUrl(String url) {
    this.url = url;
    return this;
  }

  public WebhookDefinition withHeaders(List<HttpHeader> headers) {
    this.headers = new HttpHeaders(headers);
    return this;
  }

  public WebhookDefinition withHeader(String key, String... values) {
    if (headers == null) {
      headers = HttpHeaders.noHeaders();
    }

    headers = headers.transform(builder -> builder.add(key, values));
    return this;
  }

  public WebhookDefinition withBody(String body) {
    this.body = EntityDefinition.simple(body);
    return this;
  }

  @SuppressWarnings("unused")
  public WebhookDefinition withBinaryBody(byte[] body) {
    this.body = EntityDefinition.builder().setFormat(Format.BINARY).setData(body).build();
    return this;
  }

  public WebhookDefinition withBodyFileName(String bodyFileName) {
    this.body = EntityDefinition.builder().setFilePath(bodyFileName).build();
    return this;
  }

  @SuppressWarnings("unused")
  public WebhookDefinition withBodyEntity(EntityDefinition body) {
    this.body = body;
    return this;
  }

  public WebhookDefinition withFixedDelay(int delayMilliseconds) {
    this.delay = new FixedDelayDistribution(delayMilliseconds);
    return this;
  }

  public WebhookDefinition withDelay(DelayDistribution delay) {
    this.delay = delay;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getOtherFields() {
    return parameters;
  }

  @JsonAnySetter
  public WebhookDefinition withExtraParameter(String key, Object value) {
    if (parameters == null) {
      parameters = Parameters.one(key, value);
    } else {
      parameters = parameters.merge(Parameters.one(key, value));
    }
    return this;
  }

  public WebhookDefinition withExtraParameters(Parameters parameters) {
    this.parameters = parameters;
    return this;
  }

  @JsonIgnore
  public boolean hasBody() {
    return body != null && !body.isAbsent();
  }

  @SuppressWarnings({"EqualsDoesntCheckParameterClass", "unused"})
  public static class EmptyEntityDefinitionFilter {
    @Override
    public boolean equals(Object obj) {
      return EmptyEntityDefinition.INSTANCE.equals(obj);
    }
  }
}
