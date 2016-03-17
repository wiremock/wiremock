package com.github.tomakehurst.wiremock.matching;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StringValuePatternTest {

    @Test
    public void returnsANonZeroScoreForPartialMatchOnEquals() {
        StringValuePattern pattern = StringValuePattern.equalTo("matchthis");
        assertThat(pattern.match("matchthisbadlydone").getDistance(), is(0.5));
    }

    @Test
    public void returns1ForNoMatchOnEquals() {
        StringValuePattern pattern = StringValuePattern.equalTo("matchthis");
        assertThat(pattern.match("924387348975923").getDistance(), is(1.0));
    }

    @Test
    public void returns0ForExactMatchOnEquals() {
        StringValuePattern pattern = StringValuePattern.equalTo("matchthis");
        assertThat(pattern.match("matchthis").getDistance(), is(0.0));
    }

}
