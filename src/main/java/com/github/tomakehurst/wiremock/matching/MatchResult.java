package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.google.common.collect.Iterables.all;
import static java.util.Arrays.asList;

public abstract class MatchResult implements Comparable<MatchResult> {

    @JsonCreator
    public static MatchResult partialMatch(@JsonProperty("distance") double distance) {
        return new EagerMatchResult(distance);
    }

    public static MatchResult exactMatch() {
        return new EagerMatchResult(0);
    }

    public static MatchResult noMatch() {
        return new EagerMatchResult(1);
    }

    public static MatchResult of(boolean isMatch) {
        return isMatch ? exactMatch() : noMatch();
    }

    public static MatchResult aggregate(final List<MatchResult> matchResults) {
        return new MatchResult() {
            @Override
            public boolean isExactMatch() {
                return all(matchResults, ARE_EXACT_MATCH);
            }

            @Override
            public double getDistance() {
                double totalDistance = 0;
                for (MatchResult matchResult: matchResults) {
                    totalDistance += matchResult.getDistance();
                }

                return (totalDistance / matchResults.size());
            }
        };
    }

    public static MatchResult aggregate(MatchResult... matches) {
        return aggregate(asList(matches));
    }

    @JsonIgnore
    public abstract boolean isExactMatch();

    public abstract double getDistance();
    @Override
    public int compareTo(MatchResult other) {
        return Double.compare(other.getDistance(), getDistance());
    }

    public static final Predicate<MatchResult> ARE_EXACT_MATCH = new Predicate<MatchResult>() {
        @Override
        public boolean apply(MatchResult matchResult) {
            return matchResult.isExactMatch();
        }
    };
}
