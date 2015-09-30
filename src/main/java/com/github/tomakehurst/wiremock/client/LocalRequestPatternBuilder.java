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
