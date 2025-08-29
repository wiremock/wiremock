/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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

/** The type Logged response. */
public class LoggedResponse {

  private final int status;
  private final HttpHeaders headers;
  private final byte[] body;
  private final Fault fault;

  /**
   * Instantiates a new Logged response.
   *
   * @param status the status
   * @param headers the headers
   * @param bodyAsBase64 the body as base 64
   * @param fault the fault
   * @param ignoredBodyOnlyUsedForBinding the ignored body only used for binding
   */
  public LoggedResponse(
      @JsonProperty("status") int status,
      @JsonProperty("headers") HttpHeaders headers,
      @JsonProperty("bodyAsBase64") String bodyAsBase64,
      @JsonProperty("fault") Fault fault,
      @JsonProperty("body") String ignoredBodyOnlyUsedForBinding) {
    this(status, headers, Encoding.decodeBase64(bodyAsBase64), fault);
  }

  private LoggedResponse(int status, HttpHeaders headers, byte[] body, Fault fault) {
    this.status = status;
    this.headers = headers;
    this.body = body;
    this.fault = fault;
  }

  /**
   * From logged response.
   *
   * @param response the response
   * @param responseBodySizeLimit the response body size limit
   * @return the logged response
   */
  public static LoggedResponse from(Response response, Limit responseBodySizeLimit) {
    return new LoggedResponse(
        response.getStatus(),
        response.getHeaders() == null || response.getHeaders().all().isEmpty()
            ? null
            : response.getHeaders(),
        response.getBody(responseBodySizeLimit),
        response.getFault());
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
   * Gets headers.
   *
   * @return the headers
   */
  public HttpHeaders getHeaders() {
    return headers;
  }

  /**
   * Retrieve body as a String encoded in the charset in the "Content-Type" header, or, if that's
   * not present, the default character set (UTF-8).
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

  /**
   * Gets mime type.
   *
   * @return the mime type
   */
  @JsonIgnore
  public String getMimeType() {
    return headers == null || headers.getContentTypeHeader() == null
        ? OCTET_STREAM
        : headers.getContentTypeHeader().mimeTypePart();
  }

  /**
   * Gets charset.
   *
   * @return the charset
   */
  @JsonIgnore
  public Charset getCharset() {
    return headers == null ? UTF_8 : headers.getContentTypeHeader().charset();
  }

  /**
   * Get body byte [ ].
   *
   * @return the byte [ ]
   */
  @JsonIgnore
  public byte[] getBody() {
    return body;
  }

  /**
   * Gets body as base 64.
   *
   * @return the body as base 64
   */
  @JsonProperty("bodyAsBase64")
  public String getBodyAsBase64() {
    return Encoding.encodeBase64(body);
  }

  /**
   * Gets fault.
   *
   * @return the fault
   */
  public Fault getFault() {
    return fault;
  }
}
