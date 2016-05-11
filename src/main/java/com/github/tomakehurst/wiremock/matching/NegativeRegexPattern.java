package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NegativeRegexPattern extends AbstractRegexPattern {

    public NegativeRegexPattern(@JsonProperty("doesNotMatch") String regex) {
        super(regex);
    }

    public String getDoesNotMatch() {
        return expectedValue;
    }

    @Override
    public MatchResult match(String value) {
        return invert(super.match(value));
    }

    private MatchResult invert(final MatchResult matchResult) {
        return new MatchResult() {

            @Override
            public boolean isExactMatch() {
                return !matchResult.isExactMatch();
            }

            @Override
            public double getDistance() {
                return 1.0 - matchResult.getDistance();
            }
        };
    }
}
