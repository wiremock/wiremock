package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
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
    public void matchesOnJsonPathsWithFiltersOnNestedObjects() {
        StringValuePattern pattern = WireMock.matchingJsonPath("$..thingOne[?(@.innerOne == 11)]");
        assertTrue("Expected match",
            pattern.match("{ \"things\": { \"thingOne\": { \"innerOne\": 11 }, \"thingTwo\": 2 }}")
                .isExactMatch());
    }

    @Test
    public void providesSensibleNotificationWhenJsonMatchFailsDueToInvalidJson() {
        expectInfoNotification("Warning: JSON path expression '$.something' failed to match document 'Not a JSON document' because of error 'Property ['something'] not found in path $'");

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
