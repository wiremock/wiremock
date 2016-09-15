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
import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MatchesJsonPathPatternTest {

    private Mockery context;

    @Before
    public void init() {
        context = new Mockery();
    }

    @Test
    public void matchesABasicJsonPathWhenTheExpectedElementIsPresent() {
        StringValuePattern pattern = WireMock.matchingJsonPath("$.one");
        assertTrue("Expected match when JSON attribute is present",
            pattern.match("{ \"one\": 1 }").isExactMatch());
    }

    @Test
    public void doesNotMatchABasicJsonPathWhenTheExpectedElementIsNotPresent() {
        StringValuePattern pattern = WireMock.matchingJsonPath("$.one");
        assertFalse("Expected no match when JSON attribute is absent",
            pattern.match("{ \"two\": 2 }").isExactMatch());
    }

    @Test
    public void matchesOnJsonPathsWithFilters() {
        StringValuePattern pattern = WireMock.matchingJsonPath("$.numbers[?(@.number == '2')]");

        assertTrue("Expected match when JSON attribute is present",
            pattern.match("{ \"numbers\": [ {\"number\": 1}, {\"number\": 2} ]}")
                .isExactMatch());
        assertFalse("Expected no match when JSON attribute is absent",
            pattern.match("{ \"numbers\": [{\"number\": 7} ]}")
                .isExactMatch());
    }

    @Test
    public void matchesOnJsonPathsWithRegexFilter() {
        StringValuePattern pattern = WireMock.matchingJsonPath("$.numbers[?(@.number =~ /2/i)]");

        assertTrue("Expected match when JSON attribute is present",
            pattern.match("{ \"numbers\": [ {\"number\": 1}, {\"number\": 2} ]}")
                .isExactMatch());
        assertFalse("Expected no match when JSON attribute is absent",
            pattern.match("{ \"numbers\": [{\"number\": 7} ]}")
                .isExactMatch());
    }

    @Test
    public void matchesOnJsonPathsWithSizeFilter() {
        StringValuePattern pattern = WireMock.matchingJsonPath("$[?(@.numbers.size() == 2)]");

        assertTrue("Expected match when JSON attribute is present",
            pattern.match("{ \"numbers\": [ {\"number\": 1}, {\"number\": 2} ]}")
                .isExactMatch());
        assertFalse("Expected no match when JSON attribute is absent",
            pattern.match("{ \"numbers\": [{\"number\": 7} ]}")
                .isExactMatch());
    }

    @Test
    public void matchesOnJsonPathsWithFiltersOnNestedObjects() {
        StringValuePattern pattern = WireMock.matchingJsonPath("$..thingOne[?(@.innerOne == 11)]");
        assertTrue("Expected match",
            pattern.match("{ \"things\": { \"thingOne\": { \"innerOne\": 11 }, \"thingTwo\": 2 }}")
                .isExactMatch());
    }

    @Test
    public void providesSensibleNotificationWhenJsonMatchFailsDueToInvalidJson() {
        expectInfoNotification("Warning: JSON path expression '$.something' failed to match document 'Not a JSON document' because of error 'Expected to find an object with property ['something'] in path $ but found 'java.lang.String'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.'");

        StringValuePattern pattern = WireMock.matchingJsonPath("$.something");
        assertFalse("Expected the match to fail", pattern.match("Not a JSON document").isExactMatch());
    }

    @Test
    public void providesSensibleNotificationWhenJsonMatchFailsDueToMissingAttributeJson() {
        expectInfoNotification("Warning: JSON path expression '$.something' failed to match document '{ \"nothing\": 1 }' because of error 'No results for path: $['something']'");

        StringValuePattern pattern = WireMock.matchingJsonPath("$.something");
        assertFalse("Expected the match to fail", pattern.match("{ \"nothing\": 1 }").isExactMatch());
    }

    @Test
    public void doesNotMatchWhenJsonPathWouldResolveToEmptyArray() {
        String json = "{\n" +
            "  \"RequestDetail\" : {\n" +
            "    \"ClientTag\" : \"test111\"\n" +
            "  }\n" +
            "}";

        StringValuePattern pattern = WireMock.matchingJsonPath("$.RequestDetail.?(@=='test222')");
        MatchResult match = pattern.match(json);
        assertFalse(match.isExactMatch());
    }

    @Test
    public void noMatchOnNullValue() {
        assertThat(WireMock.matchingJsonPath("$..*").match(null).isExactMatch(), is(false));
    }

    private void expectInfoNotification(final String message) {
        final Notifier notifier = context.mock(Notifier.class);
        context.checking(new Expectations() {{
            one(notifier).info(message);
        }});
        LocalNotifier.set(notifier);
    }

    @After
    public void cleanUp() {
        LocalNotifier.set(null);
    }
}
