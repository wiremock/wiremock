package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import java.util.Map;

public class NewRequestPattern implements ValueMatcher<Request> {

    private final UrlPattern url;
    private final RequestMethod method;

    public NewRequestPattern(@JsonProperty("url") UrlPattern url,
                             @JsonProperty("method") RequestMethod method) {
        this.url = url;
        this.method = method;
    }

    @Override
    public MatchResult match(Request request) {
        return MatchResult.aggregate(
            url.match(request.getUrl()),
            method.match(request.getMethod()));
    }

    public boolean isMatchedBy(Request request, Map<String, RequestMatcherExtension> customMatchers) {
        return match(request).isExactMatch();
    }
}
