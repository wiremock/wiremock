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

import static com.github.tomakehurst.wiremock.common.Limit.UNLIMITED;
import static com.github.tomakehurst.wiremock.http.HttpHeaders.noHeaders;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

import com.github.tomakehurst.wiremock.common.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class Response {

  private final int status;
  private final String statusMessage;
  private final InputStreamSource bodyStreamSource;
  private final HttpHeaders headers;
  private final boolean configured;
  private final Fault fault;
  private final boolean fromProxy;
  private final long initialDelay;
  private final ChunkedDribbleDelay chunkedDribbleDelay;
  private final String protocol;

  public static Response notConfigured() {
    return new Response(
        HTTP_NOT_FOUND,
        null,
        StreamSources.empty(),
        noHeaders(),
        false,
        null,
        0,
        null,
        false,
        null);
  }

  public static Builder response() {
    return new Builder();
  }

  private Response(
      int status,
      String statusMessage,
      InputStreamSource bodyStreamSource,
      HttpHeaders headers,
      boolean configured,
      Fault fault,
      long initialDelay,
      ChunkedDribbleDelay chunkedDribbleDelay,
      boolean fromProxy,
      String protocol) {
    this.status = status;
    this.statusMessage = statusMessage;
    this.bodyStreamSource = bodyStreamSource;
    this.headers = headers;
    this.configured = configured;
    this.fault = fault;
    this.initialDelay = initialDelay;
    this.chunkedDribbleDelay = chunkedDribbleDelay;
    this.fromProxy = fromProxy;
    this.protocol = protocol;
  }

  public int getStatus() {
    return status;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public byte[] getBody() {
    return getBody(UNLIMITED);
  }

  public byte[] getBody(Limit sizeLimit) {
    return Exceptions.uncheck(() -> getBytesFromStream(bodyStreamSource, sizeLimit), byte[].class);
  }

  private static byte[] getBytesFromStream(InputStreamSource streamSource, Limit limit)
      throws IOException {
    try (InputStream stream = streamSource == null ? null : streamSource.getStream()) {
      if (stream == null) {
        return null;
      }

      return limit != null && !limit.isUnlimited()
          ? stream.readNBytes(limit.getValue())
          : stream.readAllBytes();
    }
  }

  public String getBodyAsString() {
    return Strings.stringFromBytes(getBody(), headers.getContentTypeHeader().charset());
  }

  public InputStream getBodyStream() {
    return bodyStreamSource == null ? null : bodyStreamSource.getStream();
  }

  public boolean hasInlineBody() {
    return StreamSources.ByteArrayInputStreamSource.class.isAssignableFrom(
        bodyStreamSource.getClass());
  }

  public HttpHeaders getHeaders() {
    return headers;
  }

  public Fault getFault() {
    return fault;
  }

  public long getInitialDelay() {
    return initialDelay;
  }

  public ChunkedDribbleDelay getChunkedDribbleDelay() {
    return chunkedDribbleDelay;
  }

  public boolean shouldAddChunkedDribbleDelay() {
    return chunkedDribbleDelay != null;
  }

  public boolean wasConfigured() {
    return configured;
  }

  public boolean isFromProxy() {
    return fromProxy;
  }

  @Override
  public String toString() {
    return protocol + " " + status + "\n" + headers;
  }

  public static class Builder {
    private int status = HTTP_OK;
    private String statusMessage;
    private byte[] bodyBytes;
    private String bodyString;
    private InputStreamSource bodyStream;
    private HttpHeaders headers = new HttpHeaders();
    private boolean configured = true;
    private Fault fault;
    private boolean fromProxy;
    private long initialDelay;
    private ChunkedDribbleDelay chunkedDribbleDelay;
    private String protocol;

    public static Builder like(Response response) {
      Builder responseBuilder = new Builder();
      responseBuilder.status = response.getStatus();
      responseBuilder.statusMessage = response.getStatusMessage();
      responseBuilder.bodyStream = response.bodyStreamSource;
      responseBuilder.headers = response.getHeaders();
      responseBuilder.configured = response.wasConfigured();
      responseBuilder.fault = response.getFault();
      responseBuilder.initialDelay = response.getInitialDelay();
      responseBuilder.chunkedDribbleDelay = response.getChunkedDribbleDelay();
      responseBuilder.fromProxy = response.isFromProxy();
      return responseBuilder;
    }

    public Builder but() {
      return this;
    }

    public Builder status(int status) {
      this.status = status;
      return this;
    }

    public Builder statusMessage(String statusMessage) {
      this.statusMessage = statusMessage;
      return this;
    }

    public Builder body(byte[] body) {
      this.bodyBytes = body;
      this.bodyString = null;
      this.bodyStream = null;
      return this;
    }

    public Builder body(String body) {
      this.bodyBytes = null;
      this.bodyString = body;
      this.bodyStream = null;
      return this;
    }

    public Builder body(InputStreamSource bodySource) {
      this.bodyBytes = null;
      this.bodyString = null;
      this.bodyStream = bodySource;
      return this;
    }

    public Builder headers(HttpHeaders headers) {
      this.headers = headers == null ? noHeaders() : headers;
      return this;
    }

    public Builder configured(boolean configured) {
      this.configured = configured;
      return this;
    }

    public Builder fault(Fault fault) {
      this.fault = fault;
      return this;
    }

    public Builder configureDelay(
        Integer globalFixedDelay,
        DelayDistribution globalDelayDistribution,
        Integer fixedDelay,
        DelayDistribution delayDistribution) {
      addDelayIfSpecifiedGloballyOrIn(fixedDelay, globalFixedDelay);
      addRandomDelayIfSpecifiedGloballyOrIn(delayDistribution, globalDelayDistribution);
      return this;
    }

    private void addDelayIfSpecifiedGloballyOrIn(Integer fixedDelay, Integer globalFixedDelay) {
      Optional<Integer> optionalDelay =
          getDelayFromResponseOrGlobalSetting(fixedDelay, globalFixedDelay);
      optionalDelay.ifPresent(this::incrementInitialDelay);
    }

    private Optional<Integer> getDelayFromResponseOrGlobalSetting(
        Integer fixedDelay, Integer globalFixedDelay) {
      Integer delay = fixedDelay != null ? fixedDelay : globalFixedDelay;

      return Optional.ofNullable(delay);
    }

    private void addRandomDelayIfSpecifiedGloballyOrIn(
        DelayDistribution localDelayDistribution, DelayDistribution globalDelayDistribution) {
      DelayDistribution delayDistribution;

      if (localDelayDistribution != null) {
        delayDistribution = localDelayDistribution;
      } else {
        delayDistribution = globalDelayDistribution;
      }

      if (delayDistribution != null) {
        incrementInitialDelay(delayDistribution.sampleMillis());
      }
    }

    public Builder incrementInitialDelay(long amountMillis) {
      this.initialDelay += amountMillis;
      return this;
    }

    public Builder chunkedDribbleDelay(ChunkedDribbleDelay chunkedDribbleDelay) {
      this.chunkedDribbleDelay = chunkedDribbleDelay;
      return this;
    }

    public Builder fromProxy(boolean fromProxy) {
      this.fromProxy = fromProxy;
      return this;
    }

    public Response build() {
      InputStreamSource bodyStream;
      if (bodyBytes != null) {
        bodyStream = StreamSources.forBytes(bodyBytes);
      } else if (bodyString != null) {
        bodyStream = StreamSources.forString(bodyString, headers.getContentTypeHeader().charset());
      } else if (this.bodyStream != null) {
        bodyStream = this.bodyStream;
      } else {
        bodyStream = StreamSources.empty();
      }

      return new Response(
          status,
          statusMessage,
          bodyStream,
          headers,
          configured,
          fault,
          initialDelay,
          chunkedDribbleDelay,
          fromProxy,
          protocol);
    }

    public Builder protocol(final String protocol) {
      this.protocol = protocol;
      return this;
    }
  }
}
