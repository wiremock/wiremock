package com.github.tomakehurst.wiremock.matching.optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;

public class OptionalEqualToPattern extends OptionalPattern {

    public OptionalEqualToPattern(@JsonProperty("equalsToOrAbsent") final String value) {
        super(new EqualToPattern(value));
    }

    public String getEqualsToOrAbsent() {
        return expectedValue;
    }
}
