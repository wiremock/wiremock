package com.github.tomakehurst.wiremock.matching;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

public class EqualToPattern extends StringValuePattern {

    public EqualToPattern(String testValue) {
        super(testValue);
    }

    @Override
    public MatchResult match(String value) {
        return value.equals(testValue) ?
            MatchResult.exactMatch() :
            MatchResult.partialMatch(normalisedLevenshteinDistance(testValue, value));
    }

    private double normalisedLevenshteinDistance(String one, String two) {
        double maxDistance = Math.max(one.length(), two.length());
        double actualDistance = getLevenshteinDistance(one, two);
        return (actualDistance / maxDistance);
    }

    @Override
    protected String description() {
        return "equal to";
    }
}
