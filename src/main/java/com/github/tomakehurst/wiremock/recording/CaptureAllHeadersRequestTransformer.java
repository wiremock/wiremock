package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.google.common.base.Function;

public class CaptureAllHeadersRequestTransformer implements Function<Request, RequestPatternBuilder> {
    private final RequestBodyPatternFactory bodyPatternFactory;

    public CaptureAllHeadersRequestTransformer(RequestBodyPatternFactory bodyPatternFactory) {
        this.bodyPatternFactory = bodyPatternFactory;
    }

    @Override
    public RequestPatternBuilder apply(Request request) {
        RequestPatternBuilder builder =
            new RequestPatternBuilder(request.getMethod(), WireMock.urlEqualTo(request.getUrl()));

        for (String header : request.getAllHeaderKeys()) {
            builder.withHeader(header, new EqualToPattern(request.getHeader(header), true));
        }
        byte[] body = request.getBody();
        if (bodyPatternFactory != null && body != null && body.length > 0) {
            builder.withRequestBody(bodyPatternFactory.forRequest(request));
        }
        return builder;
    }
}
