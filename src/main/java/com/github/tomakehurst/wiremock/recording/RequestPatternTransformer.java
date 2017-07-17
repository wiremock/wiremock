package com.github.tomakehurst.wiremock.recording;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
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
    private final JsonMatchingFlags jsonMatchingFlags;

    @JsonCreator
    public RequestPatternTransformer(@JsonProperty("headers") Map<String, CaptureHeadersSpec> headers,
                                     @JsonProperty("jsonMatchingFlags") JsonMatchingFlags jsonMatchingFlags) {
        this.headers = headers;
        this.jsonMatchingFlags = jsonMatchingFlags;
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
        if (body != null && !body.isEmpty()) {
            builder.withRequestBody(valuePatternForContentType(request));
        }

        return builder;
    }

    /**
     * If request body was JSON or XML, use "equalToJson" or "equalToXml" (respectively) in the RequestPattern so it's
     * easier to read. Otherwise, just use "equalTo"
     */
    private StringValuePattern valuePatternForContentType(Request request) {
        final ContentTypeHeader contentType = request.getHeaders().getContentTypeHeader();
        if (contentType.mimeTypePart() != null) {
            if (contentType.mimeTypePart().contains("json")) {
                return jsonMatchingFlags == null ?
                    equalToJson(request.getBodyAsString()) :
                    equalToJson(request.getBodyAsString(), jsonMatchingFlags.isIgnoreArrayOrder(), jsonMatchingFlags.isIgnoreExtraElements());
            } else if (contentType.mimeTypePart().contains("xml")) {
                return equalToXml(request.getBodyAsString());
            }
        }

        return equalTo(request.getBodyAsString());
    }
}
