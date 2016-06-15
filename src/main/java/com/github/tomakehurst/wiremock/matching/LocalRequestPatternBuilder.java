package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.BasicCredentials;

public class LocalRequestPatternBuilder {

    private final RequestPatternBuilder requestPatternBuilder;

    private LocalRequestPatternBuilder(RequestPatternBuilder requestPatternBuilder) {
        this.requestPatternBuilder = requestPatternBuilder;
    }

    public static LocalRequestPatternBuilder forCustomMatcher(RequestMatcher customMatcher) {
        return new LocalRequestPatternBuilder(RequestPatternBuilder.forCustomMatcher(customMatcher));
    }

    public LocalRequestPatternBuilder withHeader(String key, StringValuePattern headerPattern) {
        requestPatternBuilder.withHeader(key, headerPattern);
        return this;
    }

    public LocalRequestPatternBuilder withoutHeader(String key) {
        requestPatternBuilder.withoutHeader(key);
        return this;
    }

    public LocalRequestPatternBuilder withQueryParam(String key, StringValuePattern queryParamPattern) {
        requestPatternBuilder.withQueryParam(key, queryParamPattern);
        return this;
    }

    public LocalRequestPatternBuilder withCookie(String key, StringValuePattern cookiePAttern) {
        requestPatternBuilder.withCookie(key, cookiePAttern);
        return this;
    }

    public LocalRequestPatternBuilder withBasicAuth(BasicCredentials basicCredentials) {
        requestPatternBuilder.withBasicAuth(basicCredentials);
        return this;
    }

    public LocalRequestPatternBuilder withRequestBody(StringValuePattern bodyPattern) {
        requestPatternBuilder.withRequestBody(bodyPattern);
        return this;
    }

    public RequestPatternBuilder getUnderlyingBuilder() {
        return requestPatternBuilder;
    }
}
