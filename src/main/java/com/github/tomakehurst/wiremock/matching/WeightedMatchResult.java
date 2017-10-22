package com.github.tomakehurst.wiremock.matching;

public class WeightedMatchResult {

    private final MatchResult matchResult;
    private final double weighting;

    public static WeightedMatchResult weight(MatchResult matchResult, double weighting) {
        return new WeightedMatchResult(matchResult, weighting);
    }

    public static WeightedMatchResult weight(MatchResult matchResult) {
        return new WeightedMatchResult(matchResult);
    }

    public WeightedMatchResult(MatchResult matchResult) {
        this(matchResult, 1.0);
    }

    public WeightedMatchResult(MatchResult matchResult, double weighting) {
        this.matchResult = matchResult;
        this.weighting = weighting;
    }

    public boolean isExactMatch() {
        return matchResult.isExactMatch();
    }

    public double getDistance() {
        return weighting * matchResult.getDistance();
    }

    public double getWeighting() {
        return weighting;
    }
}
