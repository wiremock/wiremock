package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonValue;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.base.Objects;

public class UrlPattern implements ValueMatcher<String> {

    protected final StringValuePattern pattern;
    private final boolean regex;

    public UrlPattern(StringValuePattern pattern, boolean regex) {
        this.pattern = pattern;
        this.regex = regex;
    }

    public static UrlPattern fromOneOf(String url,
                                       String urlPattern,
                                       String urlPath,
                                       String urlPathPattern) {
        if (url != null) {
            return WireMock.urlEqualTo(url);
        } else if (urlPattern != null) {
            return WireMock.urlMatching(urlPattern);
        } else if (urlPath != null) {
            return WireMock.urlPathEqualTo(urlPath);
        } else if (urlPathPattern != null) {
            return WireMock.urlPathMatching(urlPathPattern);
        } else {
            return WireMock.anyUrl();
        }
    }

    @Override
    public MatchResult match(String url) {
        return pattern.match(url);
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
    public String getExpected() {
        return pattern.expectedValue;
    }

    @Override
    public String toString() {
        return "path and query " + pattern.toString();
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

    public boolean isSpecified() {
        return pattern.getClass() != AnythingPattern.class;
    }
}
