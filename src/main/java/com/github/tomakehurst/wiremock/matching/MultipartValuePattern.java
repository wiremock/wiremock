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
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.Request;
import com.google.common.base.Function;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.FluentIterable.from;

public class MultipartValuePattern implements ValueMatcher<Request.Part> {

    public enum MatchingType { ALL, ANY }

    private final Map<String, MultiValuePattern> headers;
    private final List<ContentPattern<?>> bodyPatterns;
    private final MatchingType matchingType;

    @JsonCreator
    public MultipartValuePattern(@JsonProperty("matchingType") MatchingType type,
                                 @JsonProperty("headers") Map<String, MultiValuePattern> headers,
                                 @JsonProperty("bodyPatterns") List<ContentPattern<?>> body) {
        this.matchingType = type;
        this.headers = headers;
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
    public MatchResult match(final Request.Part value) {
        if (headers != null || bodyPatterns != null) {
            return MatchResult.aggregate(
                    headers != null ? matchHeaderPatterns(value) : MatchResult.exactMatch(),
                    bodyPatterns != null ? matchBodyPatterns(value) : MatchResult.exactMatch()
            );
        }

        return MatchResult.exactMatch();
    }

    public Map<String, MultiValuePattern> getHeaders() {
        return headers;
    }

    public MatchingType getMatchingType() {
        return matchingType;
    }

    public List<ContentPattern<?>> getBodyPatterns() {
        return bodyPatterns;
    }

    private MatchResult matchHeaderPatterns(final Request.Part part) {
        if (headers != null && !headers.isEmpty()) {
            return MatchResult.aggregate(
                from(headers.entrySet())
                    .transform(new Function<Map.Entry<String, MultiValuePattern>, MatchResult>() {
                        public MatchResult apply(Map.Entry<String, MultiValuePattern> headerPattern) {
                            return headerPattern.getValue().match(part.getHeader(headerPattern.getKey()));
                        }
                    }).toList()
            );
        }

        return MatchResult.exactMatch();
    }

    private MatchResult matchBodyPatterns(final Request.Part value) {
        return MatchResult.aggregate(
            from(bodyPatterns).transform(new Function<ContentPattern, MatchResult>() {
                @Override
                public MatchResult apply(ContentPattern bodyPattern) {
                    return matchBody(value, bodyPattern);
                }
            }).toList()
        );
    }

    private static MatchResult matchBody(Request.Part part, ContentPattern<?> bodyPattern) {
        Body body = part.getBody();
        if (BinaryEqualToPattern.class.isAssignableFrom(bodyPattern.getClass())) {
            return ((BinaryEqualToPattern) bodyPattern).match(body.asBytes());
        }

        return ((StringValuePattern) bodyPattern).match(body.asString());
    }
}
