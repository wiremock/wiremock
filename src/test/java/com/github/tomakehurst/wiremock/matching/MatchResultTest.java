package com.github.tomakehurst.wiremock.matching;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MatchResultTest {

    @Test
    public void aggregatesLazily() {
        final MatchResult result1 = new ExceptionThrowingMatchResult();
        final MatchResult result2 = new ExceptionThrowingMatchResult();
        final MatchResult result3 = new ExceptionThrowingMatchResult();

        MatchResult.aggregate(result1, result2, result3); // Expecting no exception to be thrown because getDistance is never called
    }

    @Test
    public void aggregatesDistanceCorrectly() {
        MatchResult matchResult = MatchResult.aggregate(
            MatchResult.partialMatch(0.5),
            MatchResult.partialMatch(0.6),
            MatchResult.partialMatch(0.7)
        );

        assertThat(matchResult.getDistance(), is(0.6));
    }

    @Test
    public void aggregatesExactMatchCorrectly() {
        MatchResult matchResult = MatchResult.aggregate(
            MatchResult.exactMatch(),
            MatchResult.exactMatch(),
            MatchResult.exactMatch(),
            MatchResult.exactMatch()
        );

        assertThat(matchResult.isExactMatch(), is(true));
    }

    @Test
    public void aggregatesNonExactMatchCorrectly() {
        MatchResult matchResult = MatchResult.aggregate(
            MatchResult.exactMatch(),
            MatchResult.exactMatch(),
            MatchResult.partialMatch(0.99),
            MatchResult.exactMatch()
        );

        assertThat(matchResult.isExactMatch(), is(false));
    }

    public static class ExceptionThrowingMatchResult extends MatchResult {

        @Override
        public boolean isExactMatch() {
            return false;
        }

        @Override
        public double getDistance() {
            throw new UnsupportedOperationException();
        }
    }
}