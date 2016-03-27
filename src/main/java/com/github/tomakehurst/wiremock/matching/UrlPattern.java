package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonValue;

public class UrlPattern implements ValueMatcher<String> {

    protected final StringValuePattern pattern;

    protected UrlPattern(StringValuePattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public MatchResult match(String url) {
        return pattern.match(url);
    }

    public static UrlPattern equalTo(String testUrl) {
        return new UrlPattern(StringValuePattern.equalTo(testUrl));
    }

    public static UrlPattern matching(String urlRegex) {
        return new UrlPattern(StringValuePattern.matches(urlRegex));
    }

    @JsonValue
    public StringValuePattern getPattern() {
        return pattern;
    }
}
