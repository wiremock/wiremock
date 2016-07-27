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

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EqualToJsonTest {

    @Test
    public void returns0DistanceForExactMatchForSingleLevelObject() {
        assertThat(WireMock.equalToJson(
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
        assertThat(WireMock.equalToJson(
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
        assertThat(WireMock.equalToJson(
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
        assertThat(WireMock.equalToJson(
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
        assertThat(WireMock.equalToJson(
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
        assertThat(WireMock.equalToJson(
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
        assertThat(WireMock.equalToJson(
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
        assertThat(WireMock.equalToJson(
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
        assertTrue(WireMock.equalToJson(
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
        assertFalse(WireMock.equalToJson(
            "[1, 2, 3, 4]"
        ).match(
            "[1, 3, 2, 4]"
        ).isExactMatch());
    }

    @Test
    public void ignoresArrayOrderDifferenceWhenConfigured() {
        assertTrue(WireMock.equalToJson(
            "[1, 2, 3, 4]",
            true, false)
        .match(
            "[1, 3, 2, 4]"
        ).isExactMatch());
    }

    @Test
    public void ignoresNestedArrayOrderDifferenceWhenConfigured() {
        assertTrue(WireMock.equalToJson(
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
        assertTrue(WireMock.equalToJson(
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
        assertTrue(WireMock.equalToJson(
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
            "   \"four\":   [2, 1, 2],  \n" +
            "   \"five\":   5,          \n" +
            "   \"six\":    6           \n" +
            "}                          \n"
        ).isExactMatch());
    }

    @Test
    public void correctlyDeserialisesFromJsonWhenAdditionalParamsPresent() {
        StringValuePattern pattern = Json.read(
            "{\n" +
            "    \"equalToJson\": \"2\",\n" +
            "    \"ignoreArrayOrder\": true,\n" +
            "    \"ignoreExtraElements\": true\n" +
            "}",
            StringValuePattern.class
        );

        assertThat(pattern, instanceOf(EqualToJsonPattern.class));
    }

    @Test
    public void correctlySerialisesToJsonWhenAdditionalParamsPresent() throws JSONException {
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
    public void correctlyDeserialisesFromJsonWhenAdditionalParamsAbsent() {
        StringValuePattern pattern = Json.read(
            "{\n" +
            "    \"equalToJson\": \"2\"\n" +
            "}",
            StringValuePattern.class
        );

        assertThat(pattern, instanceOf(EqualToJsonPattern.class));
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

}
