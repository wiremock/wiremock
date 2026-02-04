/*
 * Copyright (C) 2017-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.recording;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.havingExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.*;
import java.util.Map;
import java.util.function.Function;
import org.wiremock.url.PathAndQuery;

/**
 * Creates a RequestPatternBuilder from a Request's URL, method, body (if present), and optionally
 * headers from a whitelist or all headers.
 */
class RequestPatternTransformer implements Function<Request, RequestPatternBuilder> {
  private final Map<String, CaptureHeadersSpec> headers;
  private final boolean captureAllHeaders;
  private final RequestBodyPatternFactory bodyPatternFactory;

  RequestPatternTransformer(
      Map<String, CaptureHeadersSpec> headers, RequestBodyPatternFactory bodyPatternFactory) {
    this(headers, false, bodyPatternFactory);
  }

  RequestPatternTransformer(
      Map<String, CaptureHeadersSpec> headers,
      boolean captureAllHeaders,
      RequestBodyPatternFactory bodyPatternFactory) {
    this.headers = headers;
    this.captureAllHeaders = captureAllHeaders;
    this.bodyPatternFactory = bodyPatternFactory;
  }

  /** Returns a RequestPatternBuilder matching a given Request */
  @Override
  public RequestPatternBuilder apply(Request request) {
    PathAndQuery pathAndQuery = request.getPathAndQueryWithoutPrefix();
    var queryParameters = pathAndQuery.getQueryOrEmpty().asDecodedMap();
    // urlEqualTo is used when there are no query parameters to be as least disruptive to existing
    // behaviour as possible.
    // TODO: could be changed to always use urlPathEqualTo in next major release.
    var urlMatcher =
        queryParameters.isEmpty()
            ? urlEqualTo(pathAndQuery)
            : urlPathEqualTo(pathAndQuery.getPath());
    final RequestPatternBuilder builder =
        new RequestPatternBuilder(request.getMethod(), urlMatcher);

    queryParameters.forEach(
        (name, parameters) -> {
          var decodedValues = parameters.toArray(String[]::new);
          builder.withQueryParam(
              name,
              decodedValues.length == 1
                  ? MultiValuePattern.of(equalTo(decodedValues[0]))
                  : havingExactly(decodedValues));
        });

    if (captureAllHeaders) {
      for (String headerName : request.getAllHeaderKeys()) {
        CaptureHeadersSpec spec = (headers != null) ? headers.get(headerName) : null;
        Boolean caseInsensitive = (spec != null) ? spec.getCaseInsensitive() : null;
        StringValuePattern headerMatcher =
            new EqualToPattern(request.getHeader(headerName), caseInsensitive);
        builder.withHeader(headerName, headerMatcher);
      }
    } else if (headers != null && !headers.isEmpty()) {
      for (Map.Entry<String, CaptureHeadersSpec> header : headers.entrySet()) {
        String headerName = header.getKey();
        if (request.containsHeader(headerName)) {
          CaptureHeadersSpec spec = header.getValue();
          StringValuePattern headerMatcher =
              new EqualToPattern(request.getHeader(headerName), spec.getCaseInsensitive());
          builder.withHeader(headerName, headerMatcher);
        }
      }
    }

    byte[] body = request.getBody();
    if (bodyPatternFactory != null && body != null && body.length > 0) {
      builder.withRequestBody(bodyPatternFactory.forRequest(request));
    }

    return builder;
  }
}
