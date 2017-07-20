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

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.net.*;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;
import static com.google.common.net.HttpHeaders.TRANSFER_ENCODING;

/**
 * Transforms a LoggedResponse into a ResponseDefinition, which will be used to construct a StubMapping
 */
public class LoggedResponseDefinitionTransformer implements Function<LoggedResponse, ResponseDefinition> {

    private static final List<CaseInsensitiveKey> EXCLUDED_HEADERS = ImmutableList.of(
        CaseInsensitiveKey.from(CONTENT_ENCODING),
        CaseInsensitiveKey.from(CONTENT_LENGTH),
        CaseInsensitiveKey.from(TRANSFER_ENCODING)
    );

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
                return !EXCLUDED_HEADERS.contains(CaseInsensitiveKey.from(header.key()));
            }
        }));
    }
}
