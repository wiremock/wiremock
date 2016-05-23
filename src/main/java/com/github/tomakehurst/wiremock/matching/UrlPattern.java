package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Objects;

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
        } else if (urlPathPattern != null) {
            return UrlPathPattern.matching(urlPathPattern);
        } else {
            return null;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlPattern that = (UrlPattern) o;
        return regex == that.regex &&
            Objects.equal(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pattern, regex);
    }
}
