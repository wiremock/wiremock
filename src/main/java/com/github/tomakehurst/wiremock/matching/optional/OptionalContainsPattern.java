package com.github.tomakehurst.wiremock.matching.optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.github.tomakehurst.wiremock.matching.MatchResult;

public class OptionalContainsPattern extends OptionalPattern {

    public OptionalContainsPattern(@JsonProperty("containsOrAbsent") String expectedValue) {
        super(new ContainsPattern(expectedValue));
    }

    public String getContainsOrAbsent() {
        return expectedValue;
    }
}
