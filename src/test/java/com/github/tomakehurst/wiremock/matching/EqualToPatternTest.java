package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class EqualToPatternTest {

    @Test
    public void returnsANonZeroScoreForPartialMatchOnEquals() {
        StringValuePattern pattern = WireMock.equalTo("matchthis");
        assertThat(pattern.match("matchthisbadlydone").getDistance(), is(0.5));
    }

    @Test
    public void returns1ForNoMatchOnEquals() {
        StringValuePattern pattern = WireMock.equalTo("matchthis");
        assertThat(pattern.match("924387348975923").getDistance(), is(1.0));
    }

    @Test
    public void returns0ForExactMatchOnEquals() {
        StringValuePattern pattern = WireMock.equalTo("matchthis");
        assertThat(pattern.match("matchthis").getDistance(), is(0.0));
    }

    @Test
    public void correctlyDeserialisesEqualToFromJson() {
        StringValuePattern stringValuePattern = Json.read(
            "{                               \n" +
            "  \"equalTo\": \"something\"    \n" +
            "}",
            StringValuePattern.class);

        assertThat(stringValuePattern, instanceOf(EqualToPattern.class));
        assertThat(stringValuePattern.getValue(), is("something"));
    }

    @Test
    public void correctlySerialisesToJson() throws Exception {
        assertEquals(
            "{                               \n" +
            "  \"equalTo\": \"something\"    \n" +
            "}",
            Json.write(new EqualToPattern("something")),
            false
        );
    }

    @Test
    public void failsWithMeaningfulErrorWhenOperatorNotRecognised() {
        try {
            Json.read(
                "{                               \n" +
                "  \"munches\": \"something\"    \n" +
                "}",
                StringValuePattern.class);

            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(JsonMappingException.class));
            assertThat(e.getMessage(), containsString("{\"munches\":\"something\"} is not a valid comparison"));
        }

    }


}
