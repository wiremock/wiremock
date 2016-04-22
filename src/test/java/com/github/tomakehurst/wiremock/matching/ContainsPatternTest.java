package com.github.tomakehurst.wiremock.matching;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ContainsPatternTest {

    @Test
    public void returnsExactMatchWhenExpectedValueWhollyContainedInTestValue() {
        assertTrue(
            StringValuePattern.containing("thing").match("mythings").isExactMatch()
        );
    }

    @Test
    public void returnsNoMatchWhenExpectedValueNotContainedInTestValue() {
        MatchResult matchResult = StringValuePattern.containing("thing").match("otherstuff");
        assertFalse(matchResult.isExactMatch());
        assertThat(matchResult.getDistance(), is(1.0));
    }

}
