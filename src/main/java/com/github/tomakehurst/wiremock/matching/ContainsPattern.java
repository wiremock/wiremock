package com.github.tomakehurst.wiremock.matching;

public class ContainsPattern extends StringValuePattern {

    public ContainsPattern(String expectedValue) {
        super(expectedValue);
    }

    @Override
    public MatchResult match(String value) {
        return MatchResult.of(value.contains(expectedValue));
    }
}
