package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonValue;

public class UrlPattern implements ValueMatcher<String> {

    protected final StringValuePattern pattern;
    private final boolean regex;

    protected UrlPattern(StringValuePattern pattern, boolean regex) {
        this.pattern = pattern;
        this.regex = regex;
    }

    public static UrlPattern fromOneOf(String url,
                                       String urlPattern,
                                       String urlPath,
                                       String urlPathPattern) {
        if (url != null) {
            return UrlPattern.equalTo(url);
        } else if (urlPattern != null) {
            return UrlPattern.matching(urlPattern);
        } else if (urlPath != null) {
            return UrlPathPattern.equalTo(urlPath);
        } else {
            return UrlPathPattern.matching(urlPathPattern);
        }
    }

    @Override
    public MatchResult match(String url) {
        return pattern.match(url);
    }

    public static UrlPattern equalTo(String testUrl) {
        return new UrlPattern(StringValuePattern.equalTo(testUrl), false);
    }

    public static UrlPattern matching(String urlRegex) {
        return new UrlPattern(StringValuePattern.matches(urlRegex), true);
    }

    @Override
    public String getName() {
        return pattern.getName();
    }

    public boolean isRegex() {
        return regex;
    }

    @JsonValue
    public StringValuePattern getPattern() {
        return pattern;
    }
}
