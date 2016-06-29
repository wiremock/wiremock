package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static java.util.Arrays.asList;

public abstract class MatchResult implements Comparable<MatchResult> {

    @JsonCreator
    public static EagerMatchResult partialMatch(@JsonProperty("distance") double distance) {
        return new EagerMatchResult(distance);
    }

    public static EagerMatchResult exactMatch() {
        return new EagerMatchResult(0);
    }

    public static EagerMatchResult noMatch() {
        return new EagerMatchResult(1);
    }

    public static EagerMatchResult of(boolean isMatch) {
        return isMatch ? exactMatch() : noMatch();
    }

    public static EagerMatchResult aggregate(List<MatchResult> matches) {
        double totalDistance = 0;
        for (MatchResult matchResult: matches) {
            totalDistance += matchResult.getDistance();
        }

        return partialMatch(totalDistance / matches.size());
    }

    public static EagerMatchResult aggregate(MatchResult... matches) {
        return aggregate(asList(matches));
    }

    @JsonIgnore
    public abstract boolean isExactMatch();
    public abstract double getDistance();


    @Override
    public int compareTo(MatchResult other) {
        return Double.compare(other.getDistance(), getDistance());
    }
}
