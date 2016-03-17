package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.http.RequestMethod;

public class NewRequestPatternBuilder {

    private StringValuePattern url = StringValuePattern.equalTo("/");
    private RequestMethod method = RequestMethod.ANY;

    public static NewRequestPatternBuilder newRequestPattern() {
        return new NewRequestPatternBuilder();
    }

    public NewRequestPatternBuilder withUrl(String url) {
        this.url = StringValuePattern.equalTo(url);
        return this;
    }

    public NewRequestPattern build() {
        return new NewRequestPattern(url, method);
    }
}
