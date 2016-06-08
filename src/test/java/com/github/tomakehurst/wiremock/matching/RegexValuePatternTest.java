package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RegexValuePatternTest {

    @Test
    public void correctlySerialisesMatchesAsJson() throws Exception {
        String actual = Json.write(WireMock.matching("something"));
        System.out.println(actual);
        JSONAssert.assertEquals(
            "{                               \n" +
            "  \"matches\": \"something\"    \n" +
            "}",
            actual,
            true
        );
    }

    @Test
    public void correctlyDeserialisesMatchesFromJson() {
        StringValuePattern stringValuePattern = Json.read(
            "{                               \n" +
            "  \"matches\": \"something\"    \n" +
            "}",
            StringValuePattern.class);

        assertThat(stringValuePattern, instanceOf(RegexPattern.class));
        assertThat(stringValuePattern.getValue(), is("something"));
    }

}
