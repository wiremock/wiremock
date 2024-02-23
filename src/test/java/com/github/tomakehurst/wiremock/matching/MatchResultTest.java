/*
 * Copyright (C) 2016-2024 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.matching;

import static com.github.tomakehurst.wiremock.matching.WeightedMatchResult.weight;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class MatchResultTest {

  @Test
  public void aggregatesLazily() {
    final MatchResult result1 = new ExceptionThrowingMatchResult();
    final MatchResult result2 = new ExceptionThrowingMatchResult();
    final MatchResult result3 = new ExceptionThrowingMatchResult();

    MatchResult.aggregate(
        result1, result2,
        result3); // Expecting no exception to be thrown because getDistance is never called
  }

  @Test
  void aggregateWeightedIsLazy() {
    MatchResult match1 = Mockito.spy(MatchResult.exactMatch());
    MatchResult match2 = Mockito.spy(MatchResult.exactMatch());
    MatchResult match3 = Mockito.spy(MatchResult.exactMatch());
    MatchResult nonMatch1 = Mockito.spy(MatchResult.noMatch());
    MatchResult nonMatch2 = Mockito.spy(MatchResult.noMatch());

    MatchResult matchResult =
        MatchResult.aggregateWeighted(
            weight(match1, 5),
            weight(match2, 2),
            weight(nonMatch1, 5),
            weight(match3, 3),
            weight(nonMatch2, 2));

    boolean isExactMatch = matchResult.isExactMatch();

    assertThat(isExactMatch, is(false));
    Mockito.verify(match1).isExactMatch();
    Mockito.verify(match2).isExactMatch();
    Mockito.verify(nonMatch1).isExactMatch();
    Mockito.verifyNoInteractions(match3);
    Mockito.verifyNoInteractions(nonMatch2);
  }

  @Test
  void subEventsAreAggregatedForWeightedMatchResultsWhenIsNotAnOverallExactMatch() {
    MatchResult match1 = MatchResult.exactMatch(SubEvent.info("1"));
    MatchResult match2 = MatchResult.exactMatch(SubEvent.info("2"));
    MatchResult match3 = MatchResult.exactMatch(SubEvent.info("3"));
    MatchResult nonMatch1 = MatchResult.noMatch(SubEvent.info("4"));
    MatchResult nonMatch2 = MatchResult.noMatch(SubEvent.info("5"));

    MatchResult matchResult =
        MatchResult.aggregateWeighted(
            weight(match1, 5),
            weight(match2, 2),
            weight(nonMatch1, 5),
            weight(match3, 3),
            weight(nonMatch2, 2));

    assertThat(matchResult.getSubEvents().size(), is(3));
  }

  @Test
  public void aggregatesDistanceCorrectly() {
    MatchResult matchResult =
        MatchResult.aggregate(
            MatchResult.partialMatch(0.5),
            MatchResult.partialMatch(0.6),
            MatchResult.partialMatch(0.7));

    assertThat(matchResult.getDistance(), is(0.6));
  }

  @Test
  public void aggregatesExactMatchCorrectly() {
    MatchResult matchResult =
        MatchResult.aggregate(
            MatchResult.exactMatch(),
            MatchResult.exactMatch(),
            MatchResult.exactMatch(),
            MatchResult.exactMatch());

    assertThat(matchResult.isExactMatch(), is(true));
  }

  @Test
  public void aggregatesNonExactMatchCorrectly() {
    MatchResult matchResult =
        MatchResult.aggregate(
            MatchResult.exactMatch(),
            MatchResult.exactMatch(),
            MatchResult.partialMatch(0.99),
            MatchResult.exactMatch());

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
