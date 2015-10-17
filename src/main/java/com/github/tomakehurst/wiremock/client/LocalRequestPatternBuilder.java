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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.matching.RequestMatcher;

public class LocalRequestPatternBuilder {

    private final RequestPatternBuilder requestPatternBuilder;

    private LocalRequestPatternBuilder(RequestPatternBuilder requestPatternBuilder) {
        this.requestPatternBuilder = requestPatternBuilder;
    }

    public static LocalRequestPatternBuilder forCustomMatcher(RequestMatcher customMatcher) {
        return new LocalRequestPatternBuilder(RequestPatternBuilder.forCustomMatcher(customMatcher));
    }

    public LocalRequestPatternBuilder withHeader(String key, ValueMatchingStrategy headerMatchingStrategy) {
        requestPatternBuilder.withHeader(key, headerMatchingStrategy);
        return this;
    }

    public LocalRequestPatternBuilder withQueryParam(String key, ValueMatchingStrategy queryParamMatchingStrategy) {
        requestPatternBuilder.withQueryParam(key, queryParamMatchingStrategy);
        return this;
    }

    public LocalRequestPatternBuilder withoutHeader(String key) {
        requestPatternBuilder.withoutHeader(key);
        return this;
    }

    public LocalRequestPatternBuilder withRequestBody(ValueMatchingStrategy bodyMatchingStrategy) {
        requestPatternBuilder.withRequestBody(bodyMatchingStrategy);
        return this;
    }

    public RequestPatternBuilder getUnderlyingBuilder() {
        return requestPatternBuilder;
    }

}
