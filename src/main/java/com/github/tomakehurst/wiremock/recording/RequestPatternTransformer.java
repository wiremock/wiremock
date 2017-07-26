/*
 * Copyright (C) 2011 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.*;
import com.google.common.base.Function;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Creates a RequestPatternBuilder from a Request's URL and method, and optionally headers from a whitelist.
 * If headers patterns are supplied, the header will be only included in the RequestPatternBuilder if the predicate
 * matches the request
 */
public class RequestPatternTransformer implements Function<Request, RequestPatternBuilder> {
    private final Map<String, CaptureHeadersSpec> headers;
    private final RequestBodyPatternFactory bodyPatternFactory;

    public RequestPatternTransformer(
        Map<String, CaptureHeadersSpec> headers,
        RequestBodyPatternFactory bodyPatternFactory) {
        this.headers = headers;
        this.bodyPatternFactory = bodyPatternFactory;
    }

    /**
     * Returns a RequestPatternBuilder matching the URL and method of the Request. If header patterns are supplied,
     * this will match them against the request and include them in the RequestPatternBuilder if there's a match.
     */
    @Override
    public RequestPatternBuilder apply(Request request) {
        final RequestPatternBuilder builder = new RequestPatternBuilder(request.getMethod(), urlEqualTo(request.getUrl()));

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, CaptureHeadersSpec> header : headers.entrySet()) {
                String headerName = header.getKey();
                if (request.containsHeader(headerName)) {
                    CaptureHeadersSpec spec = header.getValue();
                    StringValuePattern headerMatcher = new EqualToPattern(request.getHeader(headerName), spec.getCaseInsensitive());
                    builder.withHeader(headerName, headerMatcher);
                }
            }
        }

        String body = request.getBodyAsString();
        if (bodyPatternFactory != null && body != null && !body.isEmpty()) {
            builder.withRequestBody(bodyPatternFactory.forRequest(request));
        }

        return builder;
    }
}
