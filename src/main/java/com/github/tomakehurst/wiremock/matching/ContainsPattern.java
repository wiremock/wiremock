package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContainsPattern extends StringValuePattern {

    public ContainsPattern(@JsonProperty("contains") String expectedValue) {
        super(expectedValue);
    }

    public String getContains() {
        return expectedValue;
    }

    @Override
    public MatchResult match(String value) {
        return MatchResult.of(value.contains(expectedValue));
    }
}
