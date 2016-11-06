package com.github.tomakehurst.wiremock.matching.optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.RegexPattern;

public class OptionalRegexPattern extends OptionalPattern {

    public OptionalRegexPattern(@JsonProperty("matchesOrAbsent") String regex) {
        super(new RegexPattern(regex));
    }

    public String getMatchesOrAbsent() {
        return expectedValue;
    }
}
