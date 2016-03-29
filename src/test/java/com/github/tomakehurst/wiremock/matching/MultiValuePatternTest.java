package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static com.github.tomakehurst.wiremock.http.HttpHeader.absent;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.matching.StringValuePattern.equalTo;
import static com.github.tomakehurst.wiremock.matching.StringValuePattern.equalToJson;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MultiValuePatternTest {

    @Test
    public void returnsExactMatchForAbsentHeaderWhenRequiredAbsent() {
        assertTrue(
            MultiValuePattern.absent()
            .match(HttpHeader.absent("any-key"))
            .isExactMatch());
    }

    @Test
    public void returnsNonMatchForPresentHeaderWhenRequiredAbsent() {
        assertFalse(
            MultiValuePattern.absent()
                .match(httpHeader("the-key", "the value"))
                .isExactMatch());
    }

    @Test
    public void returnsExactMatchForPresentHeaderWhenRequiredPresent() {
        assertTrue(
            MultiValuePattern.of(equalTo("required-value"))
                .match(httpHeader("the-key", "required-value"))
                .isExactMatch());
    }

    @Test
    public void returnsNonMatchForAbsentHeaderWhenRequiredPresent() {
        assertFalse(
            MultiValuePattern.of(equalTo("required-value"))
                .match(absent("the-key"))
                .isExactMatch());
    }

    @Test
    public void returnsNonZeroDistanceWhenHeaderValuesAreSimilar() {
        assertThat(
            MultiValuePattern.of(equalTo("required-value"))
                .match(httpHeader("any-key", "require1234567"))
                .getDistance(),
            is(0.5));
    }

    @Test
    public void returnsTheBestMatchWhenSeveralValuesAreAvailableAndNoneAreExact() {
        assertThat(
            MultiValuePattern.of(equalTo("required-value"))
                .match(httpHeader("any-key", "require1234567", "requi12345", "1234567rrrr"))
                .getDistance(),
            is(0.5));
    }

    @Test
    public void returnsTheBestMatchWhenSeveralValuesAreAvailableAndOneIsExact() {
        assertTrue(
            MultiValuePattern.of(equalTo("required-value"))
                .match(httpHeader("any-key", "require1234567", "required-value", "1234567rrrr"))
                .isExactMatch());
    }

    @Test
    public void correctlyRendersEqualToAsJson() throws Exception {
        String actual = Json.write(MultiValuePattern.of(equalTo("something")));
        System.out.println(actual);
        JSONAssert.assertEquals(
            "{                              \n" +
            "  \"equalTo\": \"something\"   \n" +
            "}",
            actual,
            true
        );
    }
}
