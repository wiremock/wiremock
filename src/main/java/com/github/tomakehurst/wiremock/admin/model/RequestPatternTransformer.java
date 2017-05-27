package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.google.common.base.Function;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Creates a RequestPatternBuilder from a Request's URL and method, and optional headers.
 * If headers patterns are supplied, the header will be only included in the RequestPatternBuilder if the predicate
 * matches the request
 */
public class RequestPatternTransformer implements Function<Request, RequestPatternBuilder> {
    private final Map<String, MultiValuePattern> headers;

    public RequestPatternTransformer() {
        this.headers = null;
    }

    @JsonCreator
    public RequestPatternTransformer(@JsonProperty("headers") Map<String, MultiValuePattern> headers) {
        this.headers = headers;
    }

    /**
     * Returns a RequestPatternBuilder matching the URL and method of the Request. If header patterns are supplied,
     * match them against the request and include them in the RequestPatternBuilder if there's a match.
     */
    @Override
    public RequestPatternBuilder apply(Request request) {
        final RequestPatternBuilder builder = new RequestPatternBuilder(request.getMethod(), urlEqualTo(request.getUrl()));

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, MultiValuePattern> header : headers.entrySet()) {
                String headerName = header.getKey();
                MultiValuePattern matcher = header.getValue();
                if (matcher.match(request.header(headerName)).isExactMatch()) {
                    StringValuePattern headerMatcher = equalTo(request.getHeader(headerName));
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
                return equalToJson(request.getBodyAsString(), true, true);
            } else if (contentType.mimeTypePart().contains("xml")) {
                return equalToXml(request.getBodyAsString());
            }
        }

        return equalTo(request.getBodyAsString());
    }
}
