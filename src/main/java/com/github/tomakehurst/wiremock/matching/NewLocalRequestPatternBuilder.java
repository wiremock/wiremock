package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.BasicCredentials;

public class NewLocalRequestPatternBuilder {

    private final NewRequestPatternBuilder requestPatternBuilder;

    private NewLocalRequestPatternBuilder(NewRequestPatternBuilder requestPatternBuilder) {
        this.requestPatternBuilder = requestPatternBuilder;
    }

    public static NewLocalRequestPatternBuilder forCustomMatcher(RequestMatcher customMatcher) {
        return new NewLocalRequestPatternBuilder(NewRequestPatternBuilder.forCustomMatcher(customMatcher));
    }

    public NewLocalRequestPatternBuilder withHeader(String key, StringValuePattern headerPattern) {
        requestPatternBuilder.withHeader(key, headerPattern);
        return this;
    }

    public NewLocalRequestPatternBuilder withoutHeader(String key) {
        requestPatternBuilder.withoutHeader(key);
        return this;
    }

    public NewLocalRequestPatternBuilder withQueryParam(String key, StringValuePattern queryParamPattern) {
        requestPatternBuilder.withQueryParam(key, queryParamPattern);
        return this;
    }

    public NewLocalRequestPatternBuilder withCookie(String key, StringValuePattern cookiePAttern) {
        requestPatternBuilder.withCookie(key, cookiePAttern);
        return this;
    }

    public NewLocalRequestPatternBuilder withBasicAuth(BasicCredentials basicCredentials) {
        requestPatternBuilder.withBasicAuth(basicCredentials);
        return this;
    }

    public NewLocalRequestPatternBuilder withRequestBody(StringValuePattern bodyPattern) {
        requestPatternBuilder.withRequestBody(bodyPattern);
        return this;
    }

    public NewRequestPatternBuilder getUnderlyingBuilder() {
        return requestPatternBuilder;
    }
}
