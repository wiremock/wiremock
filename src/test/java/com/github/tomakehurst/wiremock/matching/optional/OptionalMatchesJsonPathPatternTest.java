package com.github.tomakehurst.wiremock.matching.optional;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OptionalMatchesJsonPathPatternTest {

    /**
     * OptionalMatchesJsonPathPattern should match when there:
     * - exists json path and it matches to pattern,
     * - json path does not exist (is absent),
     * <p>
     * Only when it exists AND does not match to pattern it should return no match
     */

    private Mockery context;

    @Before
    public void init() {
        context = new Mockery();
    }

    @Test
    public void shouldMatchesWhenExpectedElementIsPresent() {
        //given
        final StringValuePattern pattern = WireMock.optionalMatchingJsonPath("$.one");
        final String json = "{ \"one\": 1 }";

        //when
        final MatchResult matchResult = pattern.match(json);

        //then
        assertTrue("Expected match when JSON attribute is present", matchResult.isExactMatch());
    }

    @Test
    public void shouldMatchesWhenJsonIsEmpty() {
        //given
        final StringValuePattern pattern = WireMock.optionalMatchingJsonPath("$.one");
        final String json = "{}";

        //when
        final MatchResult matchResult = pattern.match(json);

        //then
        assertTrue("Expected match when JSON attribute is present", matchResult.isExactMatch());
    }

    @Test
    public void shouldMatchesWhenExpectedElementIsAbsent() {
        //given
        final StringValuePattern pattern = WireMock.optionalMatchingJsonPath("$.one");
        final String json =
                "{\n" +
                        "  \"two\":2,\n" +
                        "  \"three\":3\n" +
                        "}";

        //when
        final MatchResult matchResult = pattern.match(json);

        //then
        assertTrue("Expected match when JSON attribute is present", matchResult.isExactMatch());
    }

    @Test
    public void shouldMatchesWhenJsonIsNull() {
        //given
        final StringValuePattern pattern = WireMock.optionalMatchingJsonPath("$.one");

        //when
        final MatchResult matchResult = pattern.match(null);

        //then
        assertTrue("Expected match when JSON attribute is present", matchResult.isExactMatch());
    }

    @Test
    public void shouldMatchesWhenJsonPathExistsAndFilterMatches() {
        //given
        final StringValuePattern pattern = WireMock.optionalMatchingJsonPath("$.[?(@.number == '2')]");
        final String json = "{\"number\": 2}";

        //when
        final MatchResult matchResult = pattern.match(json);

        //then
        assertTrue("Expected match when JSON attribute is present", matchResult.isExactMatch());
    }

    @Test
    public void shouldNotMatchesWhenJsonPathExistsAndFiltersDoesNotMatch() {
        //given
        final StringValuePattern pattern = WireMock.optionalMatchingJsonPath("$.[?(@.number == '2')]");
        final String json = "{\"number\": 3}";

        //when
        final MatchResult matchResult = pattern.match(json);

        //then
        assertFalse("Expected no match when JSON attribute is absent", matchResult.isExactMatch());
    }
}