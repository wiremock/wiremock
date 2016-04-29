package com.github.tomakehurst.wiremock.matching;

public class NegativeRegexPattern extends RegexPattern {

    public NegativeRegexPattern(String regex) {
        super(regex);
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
