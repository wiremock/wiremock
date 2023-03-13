package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

class NotPatternTest {

    @Test
    void shouldReturnExactMatchWhenValueIsNull() {
        MatchResult matchResult = WireMock.not(WireMock.containing("thing")).match(null);
        boolean result = matchResult.isExactMatch();

        assertTrue(result);
    }

    @Test
    void shouldReturnNoMatchWhenValueIsContainedInTestValue() {
        MatchResult matchResult = WireMock.not(WireMock.containing("thing")).match("otherthings");
        boolean result = matchResult.isExactMatch();
        double distance = matchResult.getDistance();

        assertFalse(result);
        assertThat(distance, is(1.0));
    }

    @Test
    void shouldReturnExactMatchWhenValueIsNotContainedInTestValue() {
        MatchResult matchResult = WireMock.not(WireMock.containing("thing")).match("otherstuff");
        boolean result = matchResult.isExactMatch();

        assertTrue(result);
    }
}