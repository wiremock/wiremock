/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.client;

import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_ENCODING;
import static com.github.tomakehurst.wiremock.common.ContentTypes.TRANSFER_ENCODING;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toUnmodifiableList;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.apache.hc.client5.http.entity.GzipCompressingEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicHeader;

public class ApacheBackedHttpClient implements HttpClient {

  private final CloseableHttpClient apacheHttpClient;

  public ApacheBackedHttpClient(CloseableHttpClient apacheHttpClient) {
    this.apacheHttpClient = apacheHttpClient;
  }

  @Override
  public <T> T execute(Request request, Function<Response, T> responseHandler) throws IOException {
    ClassicHttpRequest apacheRequest = createApacheRequest(request);
    return apacheHttpClient.execute(
        apacheRequest,
        apacheResponse -> responseHandler.apply(toWireMockHttpResponse(apacheResponse)));
  }

  private static ClassicHttpRequest createApacheRequest(Request request) {
    ContentType contentType =
        request.contentTypeHeader().isPresent()
            ? ContentType.parse(request.contentTypeHeader().firstValue())
            : ContentType.APPLICATION_OCTET_STREAM.withCharset(UTF_8);

    final ClassicRequestBuilder requestBuilder =
        ClassicRequestBuilder.create(request.getMethod().getName())
            .setUri(request.getAbsoluteUrl())
            .setHeaders(
                request.getHeaders().all().stream()
                    .filter(
                        header -> !FORBIDDEN_REQUEST_HEADERS.contains(header.key().toLowerCase()))
                    .flatMap(
                        header ->
                            header.values().stream()
                                .map(headerValue -> new BasicHeader(header.key(), headerValue)))
                    .toArray(Header[]::new));

    if (request.getBody() != null) {
      HttpEntity entity =
          request.containsHeader(TRANSFER_ENCODING)
                  && request.header(TRANSFER_ENCODING).firstValue().equals("chunked")
              ? new InputStreamEntity(new ByteArrayInputStream(request.getBody()), -1, contentType)
              : new ByteArrayEntity(
                  request.getBody(), request.contentTypeHeader().isPresent() ? contentType : null);

      requestBuilder.setEntity(applyGzipWrapperIfRequired(request, entity));
    }

    ClassicHttpRequest apacheRequest = requestBuilder.build();
    return apacheRequest;
  }

  private static HttpEntity applyGzipWrapperIfRequired(
      Request originalRequest, HttpEntity content) {
    if (originalRequest.containsHeader(CONTENT_ENCODING)
        && originalRequest.header(CONTENT_ENCODING).firstValue().contains("gzip")) {
      return new GzipCompressingEntity(content);
    }

    return content;
  }

  private static Response toWireMockHttpResponse(ClassicHttpResponse apacheResponse)
      throws IOException {
    final List<HttpHeader> headers =
        Arrays.stream(apacheResponse.getHeaders())
            .collect(groupingBy(NameValuePair::getName))
            .entrySet()
            .stream()
            .map(
                entry ->
                    new HttpHeader(
                        entry.getKey(),
                        entry.getValue().stream()
                            .map(Header::getValue)
                            .collect(toUnmodifiableList())))
            .collect(toUnmodifiableList());

    final Response.Builder responseBuilder =
        response()
            .status(apacheResponse.getCode())
            .headers(new HttpHeaders(headers))
            .protocol(apacheResponse.getVersion().toString());

    final HttpEntity entity = apacheResponse.getEntity();
    if (entity != null) {
      responseBuilder.body(EntityUtils.toByteArray(entity));
    }

    if (apacheResponse.getReasonPhrase() != null) {
      responseBuilder.statusMessage(apacheResponse.getReasonPhrase());
    }

    return responseBuilder.build();
  }
}
