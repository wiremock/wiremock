package com.github.tomakehurst.wiremock.matching;

import java.util.List;

import static java.util.Arrays.asList;

public class MatchResult {

    private final double distance;

    private MatchResult(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public boolean isExactMatch() {
        return distance == 0;
    }

    public static MatchResult partialMatch(double distance) {
        return new MatchResult(distance);
    }

    public static MatchResult exactMatch() {
        return new MatchResult(0);
    }

    public static MatchResult noMatch() {
        return new MatchResult(1);
    }

    public static MatchResult of(boolean isMatch) {
        return isMatch ? exactMatch() : noMatch();
    }

    public static MatchResult aggregate(List<MatchResult> matches) {
        double totalDistance = 0;
        for (MatchResult matchResult: matches) {
            totalDistance += matchResult.getDistance();
        }

        return partialMatch(totalDistance / matches.size());
    }

    public static MatchResult aggregate(MatchResult... matches) {
        return aggregate(asList(matches));
    }
}
