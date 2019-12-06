package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.common.Json;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class EqualToJsonPattern2Test {

    @Test
    public void returns0DistanceForExactMatchForSingleLevelObject() {
        assertThat(new EqualToJsonPattern(
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
        assertThat(new EqualToJsonPattern(
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
        ).getDistance(), is(0.4));
    }

    @Test
    public void returnsLargeDistanceForTotallyDifferentDocuments() {
        assertThat(new EqualToJsonPattern(
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
        assertThat(new EqualToJsonPattern(
                "{                  \n" +
                        "   \"one\":    1,  \n" +
                        "   \"two\":    2,  \n" +
                        "   \"three\":  3,  \n" +
                        "   \"four\":   4   \n" +
                        "}                  \n"
        ).match(
                "{}"
        ).getDistance(), is(0.8));
    }

    @Test
    public void returnsLargeDistanceWhenActualDocIsAnEmptyArray() {
        assertThat(new EqualToJsonPattern(
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
        assertThat(new EqualToJsonPattern(
                "{}"
        ).match(
                "{                  \n" +
                        "   \"one\":    1,  \n" +
                        "   \"two\":    2,  \n" +
                        "   \"three\":  3,  \n" +
                        "   \"four\":   4   \n" +
                        "}                  \n"
        ).getDistance(), is(0.8));
    }

    @Test
    public void returnsLargeDistanceWhenExpectedDocIsAnEmptyArray() {
        assertThat(new EqualToJsonPattern(
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
        assertThat(new EqualToJsonPattern(
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
        ).getDistance(), closeTo(0.56, 0.01));
    }

    @Test
    public void returnsExactMatchWhenObjectPropertyOrderDiffers() {
        assertTrue(new EqualToJsonPattern(
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
        assertFalse(new EqualToJsonPattern(
                "[1, 2, 3, 4]"
        ).match(
                "[1, 3, 2, 4]"
        ).isExactMatch());
    }

    @Test
    public void ignoresArrayOrderDifferenceWhenConfigured() {
        assertTrue(new EqualToJsonPattern(
                "[1, 2, 3, 4]",
                true, false)
                .match(
                        "[1, 3, 2, 4]"
                ).isExactMatch());
    }

    @Test
    public void ignoresNestedArrayOrderDifferenceWhenConfigured() {
        assertTrue(new EqualToJsonPattern(
                "{\n" +
                        "    \"one\": 1,\n" +
                        "    \"two\": [\n" +
                        "        { \"val\": 1 },\n" +
                        "        { \"val\": 2 },\n" +
                        "        { \"val\": 3 }\n" +
                        "    ]\n" +
                        "}",
                true, false)
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
        assertTrue(new EqualToJsonPattern(
                "{                  \n" +
                        "   \"one\":    1,  \n" +
                        "   \"two\":    2,  \n" +
                        "   \"three\":  3,  \n" +
                        "   \"four\":   4   \n" +
                        "}                  \n",
                false, true
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
        assertTrue(new EqualToJsonPattern(
                "{                          \n" +
                        "   \"one\":    1,          \n" +
                        "   \"two\":    2,          \n" +
                        "   \"three\":  3,          \n" +
                        "   \"four\":   [1, 2, 3]   \n" +
                        "}                  \n",
                true, true
        ).match(
                "{                          \n" +
                        "   \"one\":    1,          \n" +
                        "   \"three\":  3,          \n" +
                        "   \"two\":    2,          \n" +
                        "   \"four\":   [3, 1, 2],  \n" +
                        "   \"five\":   5,          \n" +
                        "   \"six\":    6           \n" +
                        "}                          \n"
        ).isExactMatch());
    }

    @Test
    public void correctlyDeserialisesFromJsonStringWhenAdditionalParamsPresent() {
        StringValuePattern pattern = Json.read(
                "{\n" +
                        "    \"equalToJson\": \"2\",\n" +
                        "    \"ignoreArrayOrder\": true,\n" +
                        "    \"ignoreExtraElements\": true\n" +
                        "}",
                StringValuePattern.class
        );

        assertThat(pattern, instanceOf(EqualToJsonPattern.class));
        assertThat(((EqualToJsonPattern) pattern).isIgnoreArrayOrder(), is(true));
        assertThat(((EqualToJsonPattern) pattern).isIgnoreExtraElements(), is(true));
        assertThat(pattern.getExpected(), is("2"));
    }

    @Test
    public void correctlyDeserialisesFromJsonValueWhenAdditionalParamsPresent() throws JSONException {
        String expectedJson = "{ \"someKey\": \"someValue\" }";
        String serializedJson =
                "{                                           \n" +
                        "    \"equalToJson\": " + expectedJson + ",  \n" +
                        "    \"ignoreArrayOrder\": true,             \n" +
                        "    \"ignoreExtraElements\": true           \n" +
                        "}                                             ";
        StringValuePattern pattern = Json.read(serializedJson, StringValuePattern.class);

        assertThat(pattern, instanceOf(EqualToJsonPattern.class));
        assertThat(((EqualToJsonPattern) pattern).isIgnoreArrayOrder(), is(true));
        assertThat(((EqualToJsonPattern) pattern).isIgnoreExtraElements(), is(true));
        JSONAssert.assertEquals(pattern.getExpected(), expectedJson, false);
    }

    @Test
    public void correctlySerialisesToJsonValueWhenAdditionalParamsPresentAndConstructedWithJsonValue() throws JSONException {
        String expectedJson = "{ \"someKey\": \"someValue\" }";
        EqualToJsonPattern pattern = new EqualToJsonPattern(Json.node(expectedJson), true, true);

        String serialised = Json.write(pattern);
        String expected =
                "{                                           \n" +
                        "    \"equalToJson\": " + expectedJson + ",  \n" +
                        "    \"ignoreArrayOrder\": true,             \n" +
                        "    \"ignoreExtraElements\": true           \n" +
                        "}                                             ";
        JSONAssert.assertEquals(expected, serialised, false);
    }

    @Test
    public void correctlySerialisesToJsonWhenAdditionalParamsPresentAndConstructedWithString() throws JSONException {
        EqualToJsonPattern pattern = new EqualToJsonPattern("4444", true, true);

        String serialised = Json.write(pattern);
        JSONAssert.assertEquals(
                "{\n" +
                        "    \"equalToJson\": \"4444\",\n" +
                        "    \"ignoreArrayOrder\": true,\n" +
                        "    \"ignoreExtraElements\": true\n" +
                        "}",
                serialised,
                false);
    }

    @Test
    public void correctlyDeserialisesFromJsonStringWhenAdditionalParamsAbsent() {
        StringValuePattern pattern = Json.read(
                "{\n" +
                        "    \"equalToJson\": \"2\"\n" +
                        "}",
                StringValuePattern.class
        );

        assertThat(pattern, instanceOf(EqualToJsonPattern.class));
        assertThat(((EqualToJsonPattern) pattern).isIgnoreArrayOrder(), is(nullValue()));
        assertThat(((EqualToJsonPattern) pattern).isIgnoreExtraElements(), is(nullValue()));
        assertThat(pattern.getExpected(), is("2"));
    }

    @Test
    public void correctlyDeserialisesFromJsonValueWhenAdditionalParamsAbsent() throws JSONException {
        String expectedJson = "[ 1, 2, \"value\" ]";
        StringValuePattern pattern = Json.read("{ \"equalToJson\": " + expectedJson + " }", StringValuePattern.class);

        assertThat(pattern, instanceOf(EqualToJsonPattern.class));
        assertThat(((EqualToJsonPattern) pattern).isIgnoreArrayOrder(), is(nullValue()));
        assertThat(((EqualToJsonPattern) pattern).isIgnoreExtraElements(), is(nullValue()));
        JSONAssert.assertEquals(pattern.getExpected(), expectedJson, false);
    }

    @Test
    public void correctlySerialisesToJsonWhenAdditionalParamsAbsentAndConstructedWithJsonValue() throws JSONException {
        String expectedJson = "[ 1, 2, \"value\" ]";
        EqualToJsonPattern pattern = new EqualToJsonPattern(Json.node(expectedJson), null, null);

        String serialised = Json.write(pattern);
        JSONAssert.assertEquals("{ \"equalToJson\": " + expectedJson + " }", serialised, false);
    }

    @Test
    public void correctlySerialisesToJsonWhenAdditionalParamsAbsent() throws JSONException {
        EqualToJsonPattern pattern = new EqualToJsonPattern("4444", null, null);

        String serialised = Json.write(pattern);
        JSONAssert.assertEquals(
                "{\n" +
                        "    \"equalToJson\": \"4444\"\n" +
                        "}",
                serialised,
                false);
    }

    @Test
    public void returnsNoExactMatchForVerySimilarNestedDocs() {
        assertFalse(
                new EqualToJsonPattern(
                        "{\n" +
                                "    \"outer\": {\n" +
                                "        \"inner:\": {\n" +
                                "            \"wrong\": 1\n" +
                                "        }\n" +
                                "    }\n" +
                                "}", false, false
                ).match(
                        "{\n" +
                                "    \"outer\": {\n" +
                                "        \"inner:\": {\n" +
                                "            \"thing\": 1\n" +
                                "        }\n" +
                                "    }\n" +
                                "}"
                ).isExactMatch()
        );
    }

    @Test
    public void doesNotMatchWhenValueIsNull() {
        MatchResult match = new EqualToJsonPattern(
                "{\n" +
                        "    \"outer\": {\n" +
                        "        \"inner:\": {\n" +
                        "            \"wrong\": 1\n" +
                        "        }\n" +
                        "    }\n" +
                        "}", false, false
        ).match(null);

        assertFalse(match.isExactMatch());
        assertThat(match.getDistance(), is(1.0));
    }

    @Test
    public void doesNotMatchWhenValueIsEmptyString() {
        MatchResult match = new EqualToJsonPattern(
                "{\n" +
                        "    \"outer\": {\n" +
                        "        \"inner:\": {\n" +
                        "            \"wrong\": 1\n" +
                        "        }\n" +
                        "    }\n" +
                        "}", false, false
        ).match("");

        assertFalse(match.isExactMatch());
        assertThat(match.getDistance(), is(1.0));
    }

    @Test
    public void doesNotMatchWhenValueIsNotJson() {
        MatchResult match = new EqualToJsonPattern(
                "{\n" +
                        "    \"outer\": {\n" +
                        "        \"inner:\": {\n" +
                        "            \"wrong\": 1\n" +
                        "        }\n" +
                        "    }\n" +
                        "}", false, false
        ).match("<some-xml />");

        assertFalse(match.isExactMatch());
        assertThat(match.getDistance(), is(1.0));
    }

    @Test
    public void doesNotBreakWhenComparingNestedArraysOfDifferentSizes() {
        String expected = "{\"columns\": [{\"name\": \"agreementnumber\",\"a\": 1},{\"name\": \"utilizerstatus\",\"b\": 2}]}";
        String actual = "{\"columns\": [{\"name\": \"x\",\"y\": 3},{\"name\": \"agreementnumber\",\"a\": 1},{\"name\": \"agreementstatus\",\"b\": 2}]}";

        MatchResult match = new EqualToJsonPattern(expected, false, false).match(actual);

        assertFalse(match.isExactMatch());
    }

    @Test
    public void doesNotBreakWhenComparingTopLevelArraysOfDifferentSizesWithCommonElements() {
        String expected = "[    \n" +
                "  { \"one\": 1 },  \n" +
                "  { \"two\": 2 },  \n" +
                "  { \"three\": 3 } \n" +
                "]";
        String actual = "[      \n" +
                "  { \"zero\": 0 }, \n" +
                "  { \"one\": 1 },  \n" +
                "  { \"two\": 2 },  \n" +
                "  { \"four\": 4 }  \n" +
                "]";

        MatchResult match = new EqualToJsonPattern(expected, false, false).match(actual);

        assertFalse(match.isExactMatch());
    }

    @Test
    public void ignoresExtraElementsWhenParameterIsPresentsWithoutIgnoreArrayOrder() {
        StringValuePattern pattern = Json.read(
                "{\n" +
                        "    \"equalToJson\": { \"one\": 1 },\n" +
                        "    \"ignoreExtraElements\": true\n" +
                        "}",
                StringValuePattern.class
        );

        assertThat(pattern.match("{\n" +
                "    \"one\": 1,\n" +
                "    \"two\": 2\n" +
                "}").isExactMatch(), is(true));
    }

    @Test
    public void doesNotMatchEmptyArraysWhenNotIgnoringExtraElements() {
        String expected = "{\"client\":\"AAA\",\"name\":\"BBB\"}";
        String actual = "{\"client\":\"AAA\", \"name\":\"BBB\", \"addresses\": [ ]}";

        MatchResult match = new EqualToJsonPattern(expected, false, false).match(actual);

        assertFalse(match.isExactMatch());
    }

    @Test
    public void doesNotMatchEmptyArrayWhenIgnoringExtraArrayElementsAndNotIgnoringExtraElements() {
        String expected = "{\"client\":\"AAA\",\"name\":\"BBB\"}";
        String actual = "{\"client\":\"AAA\", \"name\":\"BBB\", \"addresses\": [ ]}";

        MatchResult match = new EqualToJsonPattern(expected, true, false).match(actual);

        assertFalse(match.isExactMatch());
    }

    @Test
    public void doesNotMatchEmptyObjectWhenIgnoringExtraArrayElementsAndNotIgnoringExtraElements() {
        String expected = "{\"client\":\"AAA\",\"name\":\"BBB\"}";
        String actual = "{\"client\":\"AAA\", \"name\":\"BBB\", \"addresses\": { }}";

        MatchResult match = new EqualToJsonPattern(expected, true, false).match(actual);

        assertFalse(match.isExactMatch());
    }

    @Test
    public void treatsTwoTopLevelsArraysWithDifferingOrderAsSameWhenIgnoringOrder() {
        String expected = "[\"a\",\"b\", \"c\",\"d\",\"e\",\"f\",\"g\",\"h\"]";
        String actual   = "[\"b\",\"a\", \"d\",\"c\",\"e\",\"f\",\"g\",\"h\"]";

        EqualToJsonPattern pattern = new EqualToJsonPattern(expected, true, true);
        final MatchResult result = pattern.match(actual);

        assertTrue(result.isExactMatch());
    }
}
