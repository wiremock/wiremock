package com.github.tomakehurst.wiremock.matching;

import com.flipkart.zjsonpatch.JsonDiff;
import com.github.tomakehurst.wiremock.common.Json;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.matching.EqualToJsonPattern.Parameter.IGNORE_ARRAY_ORDER;
import static com.github.tomakehurst.wiremock.matching.EqualToJsonPattern.Parameter.IGNORE_EXTRA_ELEMENTS;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void returnsExactMatchWhenObjectPropertyOrderDiffers() {
        assertTrue(StringValuePattern.equalToJson(
            "{                  \n" +
                "   \"one\":    1,  \n" +
                "   \"two\":    2,  \n" +
                "   \"three\":  3,  \n" +
                "   \"four\":   4   \n" +
                "}                  \n"
        ).match(
            "{                  \n" +
                "   \"one\":    1,  \n" +
                "   \"three\":  3,  \n" +
                "   \"two\":    2,  \n" +
                "   \"four\":   4   \n" +
                "}                  \n"
        ).isExactMatch());
    }

    @Test
    public void returnsNonMatchWhenArrayOrderDiffers() {
        assertFalse(StringValuePattern.equalToJson(
            "[1, 2, 3, 4]"
        ).match(
            "[1, 3, 2, 4]"
        ).isExactMatch());
    }

    @Test
    public void ignoresArrayOrderDifferenceWhenConfigured() {
        assertTrue(StringValuePattern.equalToJson(
            "[1, 2, 3, 4]",
            IGNORE_ARRAY_ORDER)
        .match(
            "[1, 3, 2, 4]"
        ).isExactMatch());
    }

    @Test
    public void ignoresNestedArrayOrderDifferenceWhenConfigured() {
        assertTrue(StringValuePattern.equalToJson(
                "{\n" +
                "    \"one\": 1,\n" +
                "    \"two\": [\n" +
                "        { \"val\": 1 },\n" +
                "        { \"val\": 2 },\n" +
                "        { \"val\": 3 }\n" +
                "    ]\n" +
                "}",
                IGNORE_ARRAY_ORDER)
            .match(
                "{\n" +
                "    \"one\": 1,\n" +
                "    \"two\": [\n" +
                "        { \"val\": 3 },\n" +
                "        { \"val\": 2 },\n" +
                "        { \"val\": 1 }\n" +
                "    ]\n" +
                "}"
            ).isExactMatch());
    }

    @Test
    public void ignoresExtraObjectAttributesWhenConfigured() {
        assertTrue(StringValuePattern.equalToJson(
            "{                  \n" +
                "   \"one\":    1,  \n" +
                "   \"two\":    2,  \n" +
                "   \"three\":  3,  \n" +
                "   \"four\":   4   \n" +
                "}                  \n",
            IGNORE_EXTRA_ELEMENTS
        ).match(
            "{                  \n" +
                "   \"one\":    1,  \n" +
                "   \"three\":  3,  \n" +
                "   \"two\":    2,  \n" +
                "   \"four\":   4,  \n" +
                "   \"five\":   5,  \n" +
                "   \"six\":    6   \n" +
                "}                  \n"
        ).isExactMatch());
    }

    @Test
    public void ignoresExtraObjectAttributesAndArrayOrderWhenConfigured() {
        assertTrue(StringValuePattern.equalToJson(
            "{                          \n" +
            "   \"one\":    1,          \n" +
            "   \"two\":    2,          \n" +
            "   \"three\":  3,          \n" +
            "   \"four\":   [1, 2, 3]   \n" +
            "}                  \n",
            IGNORE_ARRAY_ORDER, IGNORE_EXTRA_ELEMENTS
        ).match(
            "{                          \n" +
            "   \"one\":    1,          \n" +
            "   \"three\":  3,          \n" +
            "   \"two\":    2,          \n" +
            "   \"four\":   [2, 1, 2],  \n" +
            "   \"five\":   5,          \n" +
            "   \"six\":    6           \n" +
            "}                          \n"
        ).isExactMatch());
    }

}
