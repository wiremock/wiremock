package com.github.tomakehurst.wiremock.matching;

public class UrlPattern extends StringValuePattern {

    public UrlPattern(String testUrl) {
        super(testUrl);
    }

    @Override
    public MatchResult match(String url) {
        return null;
    }

    public static UrlPattern equals(String testUrl) {

    }
}
