package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.common.Json;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AbsentPatternTest {

    @Test
    public void correctlyDeserialisesFromJson() {
        StringValuePattern stringValuePattern = Json.read(
                "{                             \n" +
                "  \"absent\": \"(absent)\"    \n" +
                "}",
                StringValuePattern.class);

        assertThat(stringValuePattern, instanceOf(AbsentPattern.class));
        assertThat(stringValuePattern.isAbsent(), is(true));
    }
}
