package com.github.tomakehurst.wiremock.matching;

import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EqualToJsonTest {

    @Test
    public void returns0DistanceForExactMatchForSingleLevelObject() {
        assertThat(StringValuePattern.equalToJson(
            "{                  \n" +
            "   \"one\":    1,  \n" +
            "   \"two\":    2,  \n" +
            "   \"three\":  3,  \n" +
            "   \"four\":   4   \n" +
            "}                  \n"
        ).match(
            "{                  \n" +
            "   \"one\":    1,  \n" +
            "   \"two\":    2,  \n" +
            "   \"three\":  3,  \n" +
            "   \"four\":   4   \n" +
            "}                  \n"
        ).getDistance(), is(0.0));
    }

    @Test
    public void returnsNon0DistanceForPartialMatchForSingleLevelObject() {
        assertThat(StringValuePattern.equalToJson(
            "{                  \n" +
                "   \"one\":    1,  \n" +
                "   \"two\":    2,  \n" +
                "   \"three\":  3,  \n" +
                "   \"four\":   4   \n" +
                "}                  \n"
        ).match(
            "{                  \n" +
                "   \"one\":    1,  \n" +
                "   \"two\":    2,  \n" +
                "   \"three\":  7,  \n" +
                "   \"four\":   8   \n" +
                "}                  \n"
        ).getDistance(), is(0.5));
    }

    @Test
    public void returnsLargeDistanceForTotallyDifferentDocuments() {
        assertThat(StringValuePattern.equalToJson(
            "{                  \n" +
            "   \"one\":    1,  \n" +
            "   \"two\":    2,  \n" +
            "   \"three\":  3,  \n" +
            "   \"four\":   4   \n" +
            "}                  \n"
        ).match(
            "[1, 2, 3]"
        ).getDistance(), is(1.0));
    }

    @Test
    public void returnsLargeDistanceWhenActualDocIsAnEmptyObject() {
        assertThat(StringValuePattern.equalToJson(
            "{                  \n" +
            "   \"one\":    1,  \n" +
            "   \"two\":    2,  \n" +
            "   \"three\":  3,  \n" +
            "   \"four\":   4   \n" +
            "}                  \n"
        ).match(
            "{}"
        ).getDistance(), is(1.0));
    }

    @Test
    public void returnsLargeDistanceWhenActualDocIsAnEmptyArray() {
        assertThat(StringValuePattern.equalToJson(
            "{                  \n" +
            "   \"one\":    1,  \n" +
            "   \"two\":    2,  \n" +
            "   \"three\":  3,  \n" +
            "   \"four\":   4   \n" +
            "}                  \n"
        ).match(
            "[]"
        ).getDistance(), is(1.0));
    }

    @Test
    public void returnsLargeDistanceWhenExpectedDocIsAnEmptyObject() {
        assertThat(StringValuePattern.equalToJson(
            "{}"
        ).match(
            "{                  \n" +
                "   \"one\":    1,  \n" +
                "   \"two\":    2,  \n" +
                "   \"three\":  3,  \n" +
                "   \"four\":   4   \n" +
                "}                  \n"
        ).getDistance(), is(1.0));
    }

    @Test
    public void returnsLargeDistanceWhenExpectedDocIsAnEmptyArray() {
        assertThat(StringValuePattern.equalToJson(
            "[]"
        ).match(
            "{                  \n" +
                "   \"one\":    1,  \n" +
                "   \"two\":    2,  \n" +
                "   \"three\":  3,  \n" +
                "   \"four\":   4   \n" +
                "}                  \n"
        ).getDistance(), is(1.0));
    }

    @Test
    public void returnsMediumDistanceWhenSubtreeIsMissingFromActual() {
        assertThat(StringValuePattern.equalToJson(
            "{\n" +
                "    \"one\": \"GET\",          \n" +
                "    \"two\": 2,                \n" +
                "    \"three\": {               \n" +
                "        \"four\": \"FOUR\",    \n" +
                "        \"five\": [            \n" +
                "            {                  \n" +
                "                \"six\": 6,    \n" +
                "                \"seven\": 7   \n" +
                "            },                 \n" +
                "            {                  \n" +
                "                \"eight\": 8,  \n" +
                "                \"nine\": 9    \n" +
                "            }                  \n" +
                "        ]                      \n" +
                "    }                          \n" +
                "}"
        ).match(
            "{                          \n" +
            "   \"one\":    \"GET\",    \n" +
            "   \"two\":    2,          \n" +
            "   \"three\":  {           \n" +
            "       \"four\":   \"FOUR\"\n" +
            "   }                       \n" +
            "}                          \n"
        ).getDistance(), closeTo(0.54, 0.01));
    }

}
