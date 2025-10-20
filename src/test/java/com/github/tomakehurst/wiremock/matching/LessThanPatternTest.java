/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class LessThanPatternTest {

    @Test
    public void failsForNoMatchOnLessThanInt() {
        StringValuePattern pattern = WireMock.lessThan("1");
        assertFalse(pattern.match("5").isExactMatch());
        assertThat(pattern.match("5").getDistance(), is(0.01));
    }

    @Test
    public void failsForNoMatchEqualOnLessThanInt() {
        StringValuePattern pattern = WireMock.lessThan("1");
        assertFalse(pattern.match("1").isExactMatch());
        assertThat(pattern.match("1").getDistance(), is(0.01));
    }

    @Test
    public void failsForNoMatchOnLessThanFloat() {
        StringValuePattern pattern = WireMock.lessThan("1.1");
        assertFalse(pattern.match("5.5").isExactMatch());
        assertThat(pattern.match("5.5").getDistance(), is(0.01));
    }

    @Test
    public void failsForNoMatchEqualOnLessThanFloat() {
        StringValuePattern pattern = WireMock.lessThan("1.1");
        assertFalse(pattern.match("1.1").isExactMatch());
        assertThat(pattern.match("1.1").getDistance(), is(0.01));
    }

    @Test
    public void succeedsForExactMatchOnLessThanInt() {
        StringValuePattern pattern = WireMock.lessThan("2");
        assertTrue(pattern.match("1").isExactMatch());
        assertThat(pattern.match("1").getDistance(), is(0.0));
    }

    @Test
    public void succeedsForExactMatchOnLessThanFloat() {
        StringValuePattern pattern = WireMock.lessThan("1.1111");
        assertTrue(pattern.match("1.111").isExactMatch());
        assertThat(pattern.match("1.111").getDistance(), is(0.0));
    }

    @Test
    public void correctlyDeserialisesEqualToFromJson() {
        StringValuePattern stringValuePattern = Json.read("{ \"equalToNumber\": \"1\" }", StringValuePattern.class);

        assertThat(stringValuePattern, instanceOf(EqualToNumberPattern.class));
        assertThat(stringValuePattern.getValue(), is("1"));
    }

    @Test
    public void correctlyDeserialisesEqualToFromJsonWithIgnoreCase() {
        StringValuePattern stringValuePattern = Json.read("{ \"lessThan\": \"1\" }", StringValuePattern.class);

        assertThat(stringValuePattern, instanceOf(LessThanPattern.class));
        assertThat(stringValuePattern.getValue(), is("1"));
    }

    @Test
    public void correctlySerialisesToJson() throws Exception {
        assertEquals("{ \"lessThan\": \"1\" }", Json.write(new LessThanPattern("1")), false);
    }

    @Test
    public void noMatchOnNullValue() {
        assertThat(WireMock.lessThan("1").match(null).isExactMatch(), is(false));
    }

    @Test
    public void noMatchOnStringValue() {
        assertThat(WireMock.lessThan("1").match("a string").isExactMatch(), is(false));
    }

    @Test
    public void noMatchOnExpectedString() {
        assertThat(WireMock.lessThan("a string").match("1").isExactMatch(), is(false));
    }

    @Test
    public void objectsShouldBeEqualOnSameExpectedValue() {
        var a = new LessThanPattern("1");
        var b = new LessThanPattern("1");
        var c = new LessThanPattern("2");

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());
        Assertions.assertEquals(b, a);
        Assertions.assertEquals(b.hashCode(), a.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a.hashCode(), c.hashCode());
        assertNotEquals(b, c);
        assertNotEquals(b.hashCode(), c.hashCode());
    }
}
