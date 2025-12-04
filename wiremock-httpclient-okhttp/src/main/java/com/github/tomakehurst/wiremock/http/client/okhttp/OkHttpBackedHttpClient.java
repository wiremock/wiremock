/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.client.okhttp;

import static com.github.tomakehurst.wiremock.http.Response.response;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

public class OkHttpBackedHttpClient implements HttpClient {

  private final OkHttpClient okHttpClient;
  private final boolean preserveUserAgentProxyHeader;

  public OkHttpBackedHttpClient(OkHttpClient okHttpClient, boolean preserveUserAgentProxyHeader) {
    this.okHttpClient = okHttpClient;
    this.preserveUserAgentProxyHeader = preserveUserAgentProxyHeader;
  }

  @Override
  public Response execute(Request request) throws IOException {
    okhttp3.Request okHttpRequest = createOkHttpRequest(request, preserveUserAgentProxyHeader);
    try (okhttp3.Response okHttpResponse = okHttpClient.newCall(okHttpRequest).execute()) {
      return toWireMockHttpResponse(okHttpResponse);
    }
  }

  private static okhttp3.Request createOkHttpRequest(
      Request request, boolean preserveUserAgentProxyHeader) {
    MediaType mediaType =
        request.contentTypeHeader().isPresent()
            ? MediaType.parse(request.contentTypeHeader().firstValue())
            : MediaType.parse("application/octet-stream; charset=utf-8");

    okhttp3.Request.Builder requestBuilder =
        new okhttp3.Request.Builder().url(request.getAbsoluteUrl());

    // Add headers
    request.getHeaders().all().stream()
        .filter(
            header ->
                !FORBIDDEN_REQUEST_HEADERS.contains(header.key().toLowerCase())
                    || (preserveUserAgentProxyHeader && header.key().equalsIgnoreCase(USER_AGENT)))
        .forEach(
            header -> {
              for (String value : header.values()) {
                requestBuilder.addHeader(header.key(), value);
              }
            });

    // Build request body
    RequestBody requestBody = null;
    if (request.getBody() != null) {
      requestBody = RequestBody.create(request.getBody(), mediaType);

      // Apply gzip compression if needed
      if (request.containsHeader(CONTENT_ENCODING)
          && request.header(CONTENT_ENCODING).firstValue().contains("gzip")) {
        requestBody = new GzipRequestBody(requestBody);
      }
    }

    // Set method and body
    requestBuilder.method(request.getMethod().getName(), requestBody);

    return requestBuilder.build();
  }

  private static Response toWireMockHttpResponse(okhttp3.Response okHttpResponse)
      throws IOException {
    // Convert headers
    Map<String, List<String>> headerMap = okHttpResponse.headers().toMultimap();
    List<HttpHeader> headers =
        headerMap.entrySet().stream()
            .map(entry -> new HttpHeader(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

    Response.Builder responseBuilder =
        response()
            .status(okHttpResponse.code())
            .headers(new HttpHeaders(headers))
            .protocol(okHttpResponse.protocol().toString());

    // Get response body
    if (okHttpResponse.body() != null) {
      responseBuilder.body(okHttpResponse.body().bytes());
    }

    // Set status message if present
    if (okHttpResponse.message() != null && !okHttpResponse.message().isEmpty()) {
      responseBuilder.statusMessage(okHttpResponse.message());
    }

    return responseBuilder.build();
  }

  private static class GzipRequestBody extends RequestBody {
    private final RequestBody requestBody;

    GzipRequestBody(RequestBody requestBody) {
      this.requestBody = requestBody;
    }

    @Override
    public MediaType contentType() {
      return requestBody.contentType();
    }

    @Override
    public long contentLength() {
      return -1; // We don't know the compressed length in advance
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
      BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
      requestBody.writeTo(gzipSink);
      gzipSink.close();
    }
  }
}
