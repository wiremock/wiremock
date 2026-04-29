/*
 * Copyright (C) 2016-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ContentTypes.OCTET_STREAM;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.common.Limit;
import com.github.tomakehurst.wiremock.common.Strings;
import java.nio.charset.Charset;
import java.util.function.Consumer;

public class LoggedResponse {

  private final int status;
  private final HttpHeaders headers;
  private final byte[] body;
  private final Fault fault;
  private final boolean fromProxy;

  public LoggedResponse(
      @JsonProperty("status") int status,
      @JsonProperty("headers") HttpHeaders headers,
      @JsonProperty("bodyAsBase64") String bodyAsBase64,
      @JsonProperty("fault") Fault fault,
      @JsonProperty("body") String ignoredBodyOnlyUsedForBinding,
      @JsonProperty("fromProxy") boolean fromProxy) {
    this(status, headers, Encoding.decodeBase64(bodyAsBase64), fault, fromProxy);
  }

  private LoggedResponse(
      int status, HttpHeaders headers, byte[] body, Fault fault, boolean fromProxy) {
    this.status = status;
    this.headers = headers;
    this.body = body;
    this.fault = fault;
    this.fromProxy = fromProxy;
  }

  public static LoggedResponse from(Response response, Limit responseBodySizeLimit) {
    return new LoggedResponse(
        response.getStatus(),
        response.getHeaders() == null || response.getHeaders().all().isEmpty()
            ? null
            : response.getHeaders(),
        response.getBody(responseBodySizeLimit),
        response.getFault(),
        response.isFromProxy());
  }

  public int getStatus() {
    return status;
  }

  public HttpHeaders getHeaders() {
    return headers;
  }

  /**
   * Retrieve body as a String encoded in the charset in the "Content-Type" header, or, if that's
   * not present, the default character set (UTF-8)
   *
   * @return Encoded string
   */
  @JsonProperty("body")
  public String getBodyAsString() {
    if (body == null) {
      return "";
    }

    return Strings.stringFromBytes(body, getCharset());
  }

  @JsonIgnore
  public String getMimeType() {
    return headers == null || headers.getContentTypeHeader() == null
        ? OCTET_STREAM
        : headers.getContentTypeHeader().mimeTypePart();
  }

  @JsonIgnore
  public Charset getCharset() {
    return headers == null ? UTF_8 : headers.getContentTypeHeader().charset();
  }

  @JsonIgnore
  public byte[] getBody() {
    return body;
  }

  @JsonProperty("bodyAsBase64")
  public String getBodyAsBase64() {
    return Encoding.encodeBase64(body);
  }

  public Fault getFault() {
    return fault;
  }

  public boolean isFromProxy() {
    return fromProxy;
  }

  public LoggedResponse transform(Consumer<Builder> transformer) {
    Builder builder = toBuilder();
    transformer.accept(builder);
    return builder.build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder {
    private int status;
    private HttpHeaders headers;
    private byte[] body;
    private Fault fault;
    private boolean fromProxy;

    public Builder(LoggedResponse original) {
      this.status = original.status;
      this.headers = original.headers;
      this.body = original.body;
      this.fault = original.fault;
      this.fromProxy = original.fromProxy;
    }

    public Builder withStatus(int status) {
      this.status = status;
      return this;
    }

    public Builder withHeaders(HttpHeaders headers) {
      this.headers = headers;
      return this;
    }

    public Builder withBody(byte[] body) {
      this.body = body;
      return this;
    }

    public Builder withFault(Fault fault) {
      this.fault = fault;
      return this;
    }

    public Builder withFromProxy(boolean fromProxy) {
      this.fromProxy = fromProxy;
      return this;
    }

    public int getStatus() {
      return status;
    }

    public HttpHeaders getHeaders() {
      return headers;
    }

    public byte[] getBody() {
      return body;
    }

    public Fault getFault() {
      return fault;
    }

    public boolean isFromProxy() {
      return fromProxy;
    }

    public LoggedResponse build() {
      return new LoggedResponse(status, headers, body, fault, fromProxy);
    }
  }
}
