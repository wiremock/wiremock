package com.github.tomakehurst.wiremock.matching.optional;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.optional.OptionalRegexPattern;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class OptionalRegexPatternTest {

    @Test
    public void shouldMatchWhenCorrectValueIsPresent() {
        //given
        final String regex = "something";
        final OptionalRegexPattern pattern = new OptionalRegexPattern(regex);

        //when
        final MatchResult matchResult = pattern.match("something");

        //then
        assertThat(matchResult.isExactMatch(), is(true));
    }

    @Test
    public void shouldNotMatchWhenIncorrectValueIsPresent() {
        //given
        final String regex = "something";
        final OptionalRegexPattern pattern = new OptionalRegexPattern(regex);

        //when
        final MatchResult matchResult = pattern.match("something else");

        //then
        assertThat(matchResult.isExactMatch(), is(false));
    }
    @Test
    public void shouldMatchWhenValueIsAbsent() {
        //given
        final String regex = "something";
        final OptionalRegexPattern pattern = new OptionalRegexPattern(regex);

        //when
        final MatchResult matchResult = pattern.match(null);

        //then
        assertThat(matchResult.isExactMatch(), is(true));
    }

    @Test
    public void shouldCorrectlySerializeToJson() throws Exception {
        //given

        //when
        final String actual = Json.write(WireMock.optionalMatching("something"));
        System.out.println(actual);

        //then
        JSONAssert.assertEquals(
                "{                                       \n" +
                "  \"matchesOrAbsent\": \"something\"    \n" +
                "}",
                actual,
                true
        );
    }

    @Test
    public void shouldCorrectlyDeserializeFromJson() {
        //given

        //when
        final StringValuePattern stringValuePattern = Json.read(
                "{                                   \n" +
                        "  \"matchesOrAbsent\": \"something\"\n" +
                        "}",
                StringValuePattern.class);

        //then
        assertThat(stringValuePattern, instanceOf(OptionalRegexPattern.class));
        assertThat(stringValuePattern.getValue(), Matchers.is("something"));
    }
}