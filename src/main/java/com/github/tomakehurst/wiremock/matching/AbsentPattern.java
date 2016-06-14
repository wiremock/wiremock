package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AbsentPattern extends StringValuePattern {

    public AbsentPattern(@JsonProperty("absent") String expectedValue) {
        super(expectedValue);
    }

    @Override
    public MatchResult match(String value) {
        return MatchResult.of(value == null);
    }
}
