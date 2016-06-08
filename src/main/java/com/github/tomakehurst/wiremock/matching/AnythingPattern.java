package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnythingPattern extends StringValuePattern {

    public AnythingPattern(@JsonProperty("anything") String expectedValue) {
        super(expectedValue);
    }

    public AnythingPattern() {
        this("(always)");
    }

    public String getAnything() {
        return "anything";
    }

    @Override
    public MatchResult match(String value) {
        return MatchResult.exactMatch();
    }

    @Override
    public String toString() {
        return "anything";
    }
}
