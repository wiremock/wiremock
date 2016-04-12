package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

public class EqualToPattern extends StringValuePattern {

    public EqualToPattern(@JsonProperty("equalTo") String testValue) {
        super(testValue);
    }

    public String getEqualTo() {
        return testValue;
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

}
