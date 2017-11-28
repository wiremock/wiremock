/*
 * Copyright (C) 2017 Arjan Duijzer
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
package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Part;

import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.io.ByteStreams.toByteArray;

public class MultipartValuePattern implements ValueMatcher<Part> {

    public enum MatchingType {
        ALL("ALL"),
        ANY("ANY");

        private final String value;

        @JsonValue
        public String getValue() {
            return value;
        }

        MatchingType(String type) {
            value = type;
        }
    }

    private final Map<String, List<MultiValuePattern>> multipartHeaders;
    private final List<ContentPattern<?>> bodyPatterns;
    private final MatchingType matchingType;

    @JsonCreator
    public MultipartValuePattern(@JsonProperty("matchingType") MatchingType type,
                                 @JsonProperty("multipartHeaders") Map<String, List<MultiValuePattern>> headers,
                                 @JsonProperty("bodyPatterns") List<ContentPattern<?>> body) {
        this.matchingType = type;
        this.multipartHeaders = headers;
        this.bodyPatterns = body;
    }

    @JsonIgnore
    public boolean isMatchAny() {
        return matchingType == MatchingType.ANY;
    }

    @JsonIgnore
    public boolean isMatchAll() {
        return matchingType == MatchingType.ALL;
    }

    @Override
    public MatchResult match(final Part value) {
        if (multipartHeaders != null || bodyPatterns != null) {
            return MatchResult.aggregate(
                    multipartHeaders != null ? matchHeaderPatterns(value) : MatchResult.exactMatch(),
                    bodyPatterns != null ? matchBodyPatterns(value) : MatchResult.exactMatch()
            );
        }

        return MatchResult.exactMatch();
    }

    private MatchResult matchHeaderPatterns(final Part value) {
        return MatchResult.aggregate(
                from(multipartHeaders.entrySet()).transform(new Function<Map.Entry<String, List<MultiValuePattern>>, MatchResult>() {
                    @Override
                    public MatchResult apply(final Map.Entry<String, List<MultiValuePattern>> input) {
                        return MatchResult.aggregate(
                                from(input.getValue()).transform(new Function<MultiValuePattern, MatchResult>() {
                                    @Override
                                    public MatchResult apply(MultiValuePattern pattern) {
                                        return pattern.match(header(input.getKey(), value));
                                    }
                                }).toList()
                        );
                    }
                }).toList()
        );
    }

    private MatchResult matchBodyPatterns(final Part value) {
        return MatchResult.aggregate(
                from(bodyPatterns).transform(new Function<ContentPattern, MatchResult>() {
                    @Override
                    public MatchResult apply(ContentPattern bodyPattern) {
                        return matchBody(value, bodyPattern);
                    }
                }).toList()
        );
    }

    private static MatchResult matchBody(Part value, ContentPattern<?> bodyPattern) {
        final byte[] body;
        try {
            body = toByteArray(value.getInputStream());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        if (StringValuePattern.class.isAssignableFrom(bodyPattern.getClass())) {
            HttpHeader contentTypeHeader = header(ContentTypeHeader.KEY, value);
            Charset charset = (contentTypeHeader != null) ? new ContentTypeHeader(contentTypeHeader.firstValue()).charset() : null;
            if (charset == null) {
                charset = Charsets.UTF_8;
            }
            return ((StringValuePattern) bodyPattern).match(stringFromBytes(body, charset));
        }

        return ((BinaryEqualToPattern) bodyPattern).match(body);
    }

    private static HttpHeader header(String name, Part value) {
        Collection<String> headerNames = value.getHeaderNames();
        for (String currentKey : headerNames) {
            if (currentKey.toLowerCase().equals(name.toLowerCase())) {
                Collection<String> valueList = value.getHeaders(currentKey);
                if (valueList.isEmpty()) {
                    return HttpHeader.empty(name);
                }

                return new HttpHeader(name, valueList);
            }
        }

        return HttpHeader.absent(name);
    }

    public MatchingType getMatchingType() {
        return matchingType;
    }

    public Map<String, List<MultiValuePattern>> getMultipartHeaders() {
        return multipartHeaders;
    }

    public List<ContentPattern<?>> getBodyPatterns() {
        return bodyPatterns;
    }
}
