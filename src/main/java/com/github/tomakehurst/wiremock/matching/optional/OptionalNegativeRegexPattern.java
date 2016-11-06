package com.github.tomakehurst.wiremock.matching.optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.NegativeRegexPattern;

public class OptionalNegativeRegexPattern extends OptionalPattern {
    public OptionalNegativeRegexPattern(@JsonProperty("doesNotMatchOrAbsent") final String regex) {
        super(new NegativeRegexPattern(regex));
    }

    public String getDoesNotMatchOrAbsent() {
        return expectedValue;
    }

}
