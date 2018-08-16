/*
 * Copyright (C) 2011 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.MultiValue;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.http.HttpHeader.absent;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.http.QueryParameter.queryParam;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.instanceOf;
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
        MatchResult matchResult = MultiValuePattern.of(equalTo("required-value"))
            .match(absent("the-key"));

        assertFalse(matchResult.isExactMatch());
        assertThat(matchResult.getDistance(), is(1.0));
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
    public void returnsTheBestMatchWhenSeveralHeaderValuesAreAvailableAndOneIsExact() {
        assertTrue(
            MultiValuePattern.of(equalTo("required-value"))
                .match(httpHeader("any-key", "require1234567", "required-value", "1234567rrrr"))
                .isExactMatch());
    }

    @Test
    public void returnsTheBestMatchWhenSeveralQueryParamValuesAreAvailableAndOneIsExact() {
        assertTrue(
            MultiValuePattern.of(equalTo("required-value"))
                .match(queryParam("any-key", "require1234567", "required-value", "1234567rrrr"))
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

    @Test
    public void correctlyRendersAbsentAsJson() throws Exception {
        String actual = Json.write(MultiValuePattern.absent());
        System.out.println(actual);
            JSONAssert.assertEquals(
            "{                   \n" +
            "  \"absent\": true   \n" +
            "}",
            actual,
            true
        );
    }

    @Test
    public void correctlyDeserialisesToSingleMatcher() {
        String json =
            "{                              \n" +
            "  \"equalTo\": \"something\"   \n" +
            "}";

        MultiValuePattern deserialisedPattern = Json.read(json, MultiValuePattern.class);
        assertThat(deserialisedPattern, instanceOf(SingleMatchMultiValuePattern.class));
        SingleMatchMultiValuePattern pattern = (SingleMatchMultiValuePattern) deserialisedPattern;

        assertThat(pattern.getValuePattern(), instanceOf(StringValuePattern.class));
        assertThat(pattern.getValuePattern().getExpected(), is("something"));
        assertThat(pattern.getValuePattern().getName(), is("equalTo"));
        assertThat(pattern.match(new MultiValue("whatever", singletonList("something"))).isExactMatch(), is(true));
    }

    @Test
    @Ignore
    public void correctlyDeserialisesToAllMatcher() {
        String json =
            "{                                  \n" +
            "    \"type\": \"ALL\",             \n" +
            "    \"patterns\": [                \n" +
            "        {                          \n" +
            "            \"equalTo\": \"1\"     \n" +
            "        },                         \n" +
            "        {                          \n" +
            "            \"contains\": \"2\"    \n" +
            "        },                         \n" +
            "        {                          \n" +
            "            \"matches\": \"th.*\"  \n" +
            "        }                          \n" +
            "    ]                              \n" +
            "}";

        MultiValuePattern deserialisedPattern = Json.read(json, MultiValuePattern.class);
        assertThat(deserialisedPattern, instanceOf(SingleMatchMultiValuePattern.class));
        SingleMatchMultiValuePattern pattern = (SingleMatchMultiValuePattern) deserialisedPattern;

        assertThat(pattern, instanceOf(AllMatchMultiValuePattern.class));
        assertThat(pattern.getValuePattern(), instanceOf(StringValuePattern.class));
        assertThat(pattern.getValuePattern().getExpected(), is("something"));
        assertThat(pattern.getValuePattern().getName(), is("equalTo"));
        assertThat(pattern.match(new MultiValue("whatever", singletonList("something"))).isExactMatch(), is(true));
    }
}
