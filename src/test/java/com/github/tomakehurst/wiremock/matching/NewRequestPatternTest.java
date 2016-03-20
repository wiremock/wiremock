package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class NewRequestPatternTest {

    @Test
    public void matchesExactlyWith0DistanceWhenUrlAndMethodAreExactMatch() {
        NewRequestPattern requestPattern = NewRequestPatternBuilder
            .newRequestPattern(PUT, UrlPathPattern.equals("/my/url"))
            .build();

        MatchResult matchResult = requestPattern.match(mockRequest().method(PUT).url("/my/url"));
        assertThat(matchResult.getDistance(), is(0.0));
        assertTrue(matchResult.isExactMatch());
    }

    @Test
    public void returnsNon0DistanceWhenUrlDoesNotMatch() {
        NewRequestPattern requestPattern = NewRequestPatternBuilder
            .newRequestPattern(PUT, UrlPathPattern.equals("/my/url"))
            .withUrl("/my/url")
            .build();

        MatchResult matchResult = requestPattern.match(mockRequest().url("/totally/other/url"));
        assertThat(matchResult.getDistance(), greaterThan(0.0));
        assertFalse(matchResult.isExactMatch());
    }


}
