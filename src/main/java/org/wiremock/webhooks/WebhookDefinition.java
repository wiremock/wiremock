/*
 * Copyright (C) 2021-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Encoding.decodeBase64;
import static java.util.Collections.singletonList;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** The type Webhook definition. */
public class WebhookDefinition {

  private String method;
  private String url;
  private List<HttpHeader> headers;
  private Body body = Body.none();
  private DelayDistribution delay;
  private Parameters parameters;

  /**
   * From webhook definition.
   *
   * @param parameters the parameters
   * @return the webhook definition
   */
  public static WebhookDefinition from(Parameters parameters) {
    return new WebhookDefinition(
        parameters.getString("method", "GET"),
        parameters.getString("url"),
        toHttpHeaders(parameters.getMetadata("headers", null)),
        parameters.getString("body", null),
        parameters.getString("base64Body", null),
        getDelayDistribution(parameters.getMetadata("delay", null)),
        parameters);
  }

  private static HttpHeaders toHttpHeaders(Metadata headerMap) {
    if (headerMap == null || headerMap.isEmpty()) {
      return null;
    }

    return new HttpHeaders(
        headerMap.entrySet().stream()
            .map(entry -> new HttpHeader(entry.getKey(), getHeaderValues(entry.getValue())))
            .collect(Collectors.toList()));
  }

  @SuppressWarnings("unchecked")
  private static Collection<String> getHeaderValues(Object obj) {
    if (obj == null) {
      return null;
    }

    if (obj instanceof List) {
      return ((List<String>) obj);
    }

    return singletonList(obj.toString());
  }

  private static DelayDistribution getDelayDistribution(Metadata delayParams) {
    if (delayParams == null) {
      return null;
    }

    return delayParams.as(DelayDistribution.class);
  }

  /**
   * Instantiates a new Webhook definition.
   *
   * @param method the method
   * @param url the url
   * @param headers the headers
   * @param body the body
   * @param base64Body the base 64 body
   * @param delay the delay
   * @param parameters the parameters
   */
  @JsonCreator
  public WebhookDefinition(
      String method,
      String url,
      HttpHeaders headers,
      String body,
      String base64Body,
      DelayDistribution delay,
      Parameters parameters) {
    this.method = method;
    this.url = url;
    this.headers = headers != null ? new ArrayList<>(headers.all()) : null;

    if (body != null) {
      this.body = new Body(body);
    } else if (base64Body != null) {
      this.body = new Body(decodeBase64(base64Body));
    }

    this.delay = delay;
    this.parameters = parameters;
  }

  /** Instantiates a new Webhook definition. */
  public WebhookDefinition() {}

  /**
   * Gets method.
   *
   * @return the method
   */
  public String getMethod() {
    return method;
  }

  /**
   * Gets request method.
   *
   * @return the request method
   */
  @JsonIgnore
  public RequestMethod getRequestMethod() {
    return RequestMethod.fromString(method);
  }

  /**
   * Gets url.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Gets headers.
   *
   * @return the headers
   */
  public HttpHeaders getHeaders() {
    return new HttpHeaders(headers);
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
   * Gets body.
   *
   * @return the body
   */
  public String getBody() {
    return body.isBinary() ? null : body.asString();
  }

  /**
   * Gets delay.
   *
   * @return the delay
   */
  public DelayDistribution getDelay() {
    return delay;
  }

  /**
   * Gets delay sample millis.
   *
   * @return the delay sample millis
   */
  @JsonIgnore
  public long getDelaySampleMillis() {
    return delay != null ? delay.sampleMillis() : 0L;
  }

  /**
   * Gets extra parameters.
   *
   * @return the extra parameters
   */
  @JsonIgnore
  public Parameters getExtraParameters() {
    return parameters;
  }

  /**
   * Get binary body byte [ ].
   *
   * @return the byte [ ]
   */
  @JsonIgnore
  public byte[] getBinaryBody() {
    return body.asBytes();
  }

  /**
   * With method webhook definition.
   *
   * @param method the method
   * @return the webhook definition
   */
  public WebhookDefinition withMethod(String method) {
    this.method = method;
    return this;
  }

  /**
   * With method webhook definition.
   *
   * @param method the method
   * @return the webhook definition
   */
  public WebhookDefinition withMethod(RequestMethod method) {
    this.method = method.getName();
    return this;
  }

  /**
   * With url webhook definition.
   *
   * @param url the url
   * @return the webhook definition
   */
  public WebhookDefinition withUrl(URI url) {
    this.url = url.toString();
    return this;
  }

  /**
   * With url webhook definition.
   *
   * @param url the url
   * @return the webhook definition
   */
  public WebhookDefinition withUrl(String url) {
    this.url = url;
    return this;
  }

  /**
   * With headers webhook definition.
   *
   * @param headers the headers
   * @return the webhook definition
   */
  public WebhookDefinition withHeaders(List<HttpHeader> headers) {
    this.headers = headers;
    return this;
  }

  /**
   * With header webhook definition.
   *
   * @param key the key
   * @param values the values
   * @return the webhook definition
   */
  public WebhookDefinition withHeader(String key, String... values) {
    if (headers == null) {
      headers = new ArrayList<>();
    }

    headers.add(new HttpHeader(key, values));
    return this;
  }

  /**
   * With body webhook definition.
   *
   * @param body the body
   * @return the webhook definition
   */
  public WebhookDefinition withBody(String body) {
    this.body = new Body(body);
    return this;
  }

  /**
   * With binary body webhook definition.
   *
   * @param body the body
   * @return the webhook definition
   */
  public WebhookDefinition withBinaryBody(byte[] body) {
    this.body = new Body(body);
    return this;
  }

  /**
   * With fixed delay webhook definition.
   *
   * @param delayMilliseconds the delay milliseconds
   * @return the webhook definition
   */
  public WebhookDefinition withFixedDelay(int delayMilliseconds) {
    this.delay = new FixedDelayDistribution(delayMilliseconds);
    return this;
  }

  /**
   * With delay webhook definition.
   *
   * @param delay the delay
   * @return the webhook definition
   */
  public WebhookDefinition withDelay(DelayDistribution delay) {
    this.delay = delay;
    return this;
  }

  /**
   * Gets other fields.
   *
   * @return the other fields
   */
  @JsonAnyGetter
  public Map<String, Object> getOtherFields() {
    return parameters;
  }

  /**
   * With extra parameter webhook definition.
   *
   * @param key the key
   * @param value the value
   * @return the webhook definition
   */
  @JsonAnySetter
  public WebhookDefinition withExtraParameter(String key, Object value) {
    if (parameters == null) {
      parameters = new Parameters();
    }

    this.parameters.put(key, value);
    return this;
  }

  /**
   * Has body boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean hasBody() {
    return body != null && body.isPresent();
  }
}
