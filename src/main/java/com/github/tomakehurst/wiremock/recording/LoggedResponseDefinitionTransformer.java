package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.http.*;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.google.common.collect.Iterables.filter;

/**
 * Transforms a LoggedResponse into a ResponseDefinition, which will be used to construct a StubMapping
 */
public class LoggedResponseDefinitionTransformer implements Function<LoggedResponse, ResponseDefinition> {
    @Override
    public ResponseDefinition apply(LoggedResponse response) {
        final ResponseDefinitionBuilder responseDefinitionBuilder = new ResponseDefinitionBuilder()
            .withStatus(response.getStatus());

        if (response.getBody() != null && response.getBody().length > 0) {
            if (
                response.getHeaders() != null
                && ContentTypes.determineIsTextFromMimeType(response.getHeaders().getContentTypeHeader().mimeTypePart())
            ) {
                responseDefinitionBuilder.withBody(response.getBodyAsString());
            } else {
                responseDefinitionBuilder.withBody(response.getBody());
            }
        }

        if (response.getHeaders() != null) {
            responseDefinitionBuilder.withHeaders(withoutContentEncodingAndContentLength(response));
        }

        return responseDefinitionBuilder.build();
    }

    private HttpHeaders withoutContentEncodingAndContentLength(LoggedResponse response) {
        return new HttpHeaders(filter(response.getHeaders().all(), new Predicate<HttpHeader>() {
            public boolean apply(HttpHeader header) {
                return !header.keyEquals("Content-Encoding") && !header.keyEquals("Content-Length");
            }
        }));
    }
}
