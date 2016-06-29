package com.github.tomakehurst.wiremock.matching;

public class EagerMatchResult extends MatchResult {

    private final double distance;

    EagerMatchResult(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public boolean isExactMatch() {
        return distance == 0;
    }
}
