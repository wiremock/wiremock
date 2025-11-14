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

public class LessThanEqualPatternTest {

    @Test
    public void failsForNoMatchOnLessThanEqualInt() {
        StringValuePattern pattern = WireMock.lessThanEqual("1");
        assertFalse(pattern.match("5").isExactMatch());
        assertThat(pattern.match("5").getDistance(), is(0.01));
    }

    @Test
    public void succeedsForNoMatchEqualOnLessThanEqualInt() {
        StringValuePattern pattern = WireMock.lessThanEqual("1");
        assertTrue(pattern.match("1").isExactMatch());
        assertThat(pattern.match("1").getDistance(), is(0.00));
    }

    @Test
    public void failsForNoMatchOnLessThanEqualFloat() {
        StringValuePattern pattern = WireMock.lessThanEqual("1.1");
        assertFalse(pattern.match("5.5").isExactMatch());
        assertThat(pattern.match("5.5").getDistance(), is(0.01));
    }

    @Test
    public void succeedsForNoMatchEqualOnLessThanEqualFloat() {
        StringValuePattern pattern = WireMock.lessThanEqual("1.1");
        assertTrue(pattern.match("1.1").isExactMatch());
        assertThat(pattern.match("1.1").getDistance(), is(0.00));
    }

    @Test
    public void succeedsForExactMatchOnLessThanEqualInt() {
        StringValuePattern pattern = WireMock.lessThanEqual("2");
        assertTrue(pattern.match("1").isExactMatch());
        assertThat(pattern.match("1").getDistance(), is(0.0));
    }

    @Test
    public void succeedsForExactMatchOnLessThanEqualFloat() {
        StringValuePattern pattern = WireMock.lessThanEqual("1.1111");
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
        StringValuePattern stringValuePattern = Json.read("{ \"lessThanEqual\": \"1\" }", StringValuePattern.class);

        assertThat(stringValuePattern, instanceOf(LessThanEqualPattern.class));
        assertThat(stringValuePattern.getValue(), is("1"));
    }

    @Test
    public void correctlySerialisesToJson() throws Exception {
        assertEquals("{ \"lessThanEqual\": \"1\" }", Json.write(new LessThanEqualPattern("1")), false);
    }

    @Test
    public void noMatchOnNullValue() {
        assertThat(WireMock.lessThanEqual("1").match(null).isExactMatch(), is(false));
    }

    @Test
    public void noMatchOnStringValue() {
        assertThat(WireMock.lessThanEqual("1").match("a string").isExactMatch(), is(false));
    }

    @Test
    public void noMatchOnExpectedString() {
        assertThat(WireMock.lessThanEqual("a string").match("1").isExactMatch(), is(false));
    }

    @Test
    public void objectsShouldBeEqualOnSameExpectedValue() {
        var a = new LessThanEqualPattern("1");
        var b = new LessThanEqualPattern("1");
        var c = new LessThanEqualPattern("2");

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
