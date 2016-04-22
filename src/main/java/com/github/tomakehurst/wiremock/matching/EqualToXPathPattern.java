package com.github.tomakehurst.wiremock.matching;

public class EqualToXPathPattern extends StringValuePattern {

    public EqualToXPathPattern(String expectedValue) {
        super(expectedValue);
    }

    @Override
    public MatchResult match(String value) {
        return null;
    }
}
