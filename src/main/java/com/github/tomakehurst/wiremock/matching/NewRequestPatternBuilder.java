package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.http.RequestMethod;

public class NewRequestPatternBuilder {

    private UrlPattern url;
    private RequestMethod method;

    public NewRequestPatternBuilder() {
    }

    public NewRequestPatternBuilder(RequestMethod method, UrlPattern url) {
        this.method = method;
        this.url = url;
    }

    public static NewRequestPatternBuilder newRequestPattern(RequestMethod method, UrlPattern url) {
        return new NewRequestPatternBuilder(method, url);
    }

    public static NewRequestPatternBuilder newRequestPattern() {
        return new NewRequestPatternBuilder();
    }

    public NewRequestPatternBuilder withUrl(String url) {
        this.url = UrlPattern.equals(url);
        return this;
    }

    public NewRequestPattern build() {
        return new NewRequestPattern(url, method);
    }
}
