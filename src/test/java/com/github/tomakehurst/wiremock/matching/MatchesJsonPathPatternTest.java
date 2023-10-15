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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.SubEvent.WARNING;
import static com.github.tomakehurst.wiremock.testsupport.ServeEventChecks.checkMessage;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.testsupport.ServeEventChecks;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class MatchesJsonPathPatternTest {

  @Test
  public void matchesABasicJsonPathWhenTheExpectedElementIsPresent() {
    StringValuePattern pattern = WireMock.matchingJsonPath("$.one");
    assertTrue(
        pattern.match("{ \"one\": 1 }").isExactMatch(),
        "Expected match when JSON attribute is present");
  }

  @Test
  public void doesNotMatchABasicJsonPathWhenTheExpectedElementIsNotPresent() {
    StringValuePattern pattern = WireMock.matchingJsonPath("$.one");
    assertFalse(
        pattern.match("{ \"two\": 2 }").isExactMatch(),
        "Expected no match when JSON attribute is absent");
  }

  @Test
  public void matchesOnJsonPathsWithFilters() {
    StringValuePattern pattern = WireMock.matchingJsonPath("$.numbers[?(@.number == '2')]");

    assertTrue(
        pattern.match("{ \"numbers\": [ {\"number\": 1}, {\"number\": 2} ]}").isExactMatch(),
        "Expected match when JSON attribute is present");
    assertFalse(
        pattern.match("{ \"numbers\": [{\"number\": 7} ]}").isExactMatch(),
        "Expected no match when JSON attribute is absent");
  }

  @Test
  public void matchesOnJsonPathsWithRegexFilter() {
    StringValuePattern pattern = WireMock.matchingJsonPath("$.numbers[?(@.number =~ /2/i)]");

    assertTrue(
        pattern.match("{ \"numbers\": [ {\"number\": 1}, {\"number\": 2} ]}").isExactMatch(),
        "Expected match when JSON attribute is present");
    assertFalse(
        pattern.match("{ \"numbers\": [{\"number\": 7} ]}").isExactMatch(),
        "Expected no match when JSON attribute is absent");
  }

  @Test
  public void matchesOnJsonPathsWithSizeFilter() {
    StringValuePattern pattern = WireMock.matchingJsonPath("$[?(@.numbers.size() == 2)]");

    assertTrue(
        pattern.match("{ \"numbers\": [ {\"number\": 1}, {\"number\": 2} ]}").isExactMatch(),
        "Expected match when JSON attribute is present");
    assertFalse(
        pattern.match("{ \"numbers\": [{\"number\": 7} ]}").isExactMatch(),
        "Expected no match when JSON attribute is absent");
  }

  @Test
  public void matchesOnJsonPathsWithFiltersOnNestedObjects() {
    StringValuePattern pattern = WireMock.matchingJsonPath("$..thingOne[?(@.innerOne == 11)]");
    assertTrue(
        pattern
            .match("{ \"things\": { \"thingOne\": { \"innerOne\": 11 }, \"thingTwo\": 2 }}")
            .isExactMatch(),
        "Expected match");
  }

  @Test
  public void providesEventMessageWhenJsonMatchFailsDueToInvalidJson() {
    StringValuePattern pattern = WireMock.matchingJsonPath("$.something");
    MatchResult match = pattern.match("Not a JSON document");

    assertFalse(match.isExactMatch(), "Expected the match to fail");
    checkMessage(
        match,
        WARNING,
        "Warning: JSON path expression '$.something' failed to match document 'Not a JSON document' because of error 'Expected to find an object with property ['something'] in path $ but found 'java.lang.String'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.'");
  }

  private static void checkWarningMessageAndEvent(
      Notifier notifier, MatchResult match, String warningMessage) {
    verify(notifier).info(warningMessage);
    checkMessage(match, WARNING, warningMessage);
  }

  @Test
  public void providesEventMessageWhenJsonMatchFailsDueToMissingAttributeJson() {
    StringValuePattern pattern = WireMock.matchingJsonPath("$.something");
    MatchResult matchResult = pattern.match("{ \"nothing\": 1 }");

    assertFalse(matchResult.isExactMatch(), "Expected the match to fail");
    checkMessage(
        matchResult,
        WARNING,
        "Warning: JSON path expression '$.something' failed to match document '{ \"nothing\": 1 }' because of error 'No results for path: $['something']'");
  }

  @Test
  void notifiesWhenMatchingBeingSkippedDueToContentProbablyBeingXml() {
    Notifier notifier = setMockNotifier();

    StringValuePattern pattern = WireMock.matchingJsonPath("$.something");
    MatchResult matchResult = pattern.match("<xml-stuff />");

    assertFalse(matchResult.isExactMatch(), "Expected the match to fail");
    checkWarningMessageAndEvent(
        notifier,
        matchResult,
        "Warning: JSON path expression '$.something' failed to match document '<xml-stuff />' because it's not JSON but probably XML");
  }

  @Test
  void subEventsReturnedBySubMatchersAreAddedToServeEvent() {
    StringValuePattern pattern =
        WireMock.matchingJsonPath("$.something", WireMock.equalToJson("{}"));
    MatchResult matchResult = pattern.match("{ \"something\": \"{ \\\"bad:\" }");

    assertFalse(matchResult.isExactMatch(), "Expected the match to fail");
    ServeEventChecks.checkJsonError(
        matchResult,
        "Unexpected end-of-input in field name\n at [Source: (String)\"{ \"bad:\"; line: 1, column: 8]");
  }

  @Test
  public void doesNotMatchWhenJsonPathWouldResolveToEmptyArray() {
    String json =
        "{\n" + "  \"RequestDetail\" : {\n" + "    \"ClientTag\" : \"test111\"\n" + "  }\n" + "}";

    StringValuePattern pattern = WireMock.matchingJsonPath("$.RequestDetail.?(@=='test222')");
    MatchResult match = pattern.match(json);
    assertFalse(match.isExactMatch());
  }

  @Test
  public void noMatchOnNullValue() {
    assertThat(WireMock.matchingJsonPath("$..*").match(null).isExactMatch(), is(false));
  }

  @Test
  public void matchesNumericExpressionResultAgainstValuePatternWhenSpecified() {
    String json = "{\n" + "    \"volumeControl\": {\n" + "        \"max\": 11\n" + "    }\n" + "}";

    StringValuePattern pattern = WireMock.matchingJsonPath("$.volumeControl.max", equalTo("11"));
    MatchResult match = pattern.match(json);
    assertTrue(match.isExactMatch());
  }

  @Test
  public void matchesStringExpressionResultAgainstValuePatternWhenSpecified() {
    String json =
        "{\n" + "    \"volumeControl\": {\n" + "        \"max\": \"eleven\"\n" + "    }\n" + "}";

    StringValuePattern pattern =
        WireMock.matchingJsonPath("$.volumeControl.max", equalTo("eleven"));
    MatchResult match = pattern.match(json);
    assertTrue(match.isExactMatch());
  }

  @Test
  public void matchesBooleanExpressionResultAgainstValuePatternWhenSpecified() {
    String json =
        "{\n" + "    \"volumeControl\": {\n" + "        \"max\": true\n" + "    }\n" + "}";

    StringValuePattern pattern = WireMock.matchingJsonPath("$.volumeControl.max", equalTo("true"));
    MatchResult match = pattern.match(json);
    assertTrue(match.isExactMatch());
  }

  @Test
  public void matchesObjectExpressionResultAgainstValuePatternWhenSpecified() {
    String json = "{\n" + "    \"volumeControl\": {\n" + "        \"max\": 11\n" + "    }\n" + "}";

    StringValuePattern pattern =
        WireMock.matchingJsonPath(
            "$.volumeControl", WireMock.equalToJson("{\n" + "    \"max\": 11\n" + "}"));
    MatchResult match = pattern.match(json);
    assertTrue(match.isExactMatch());
  }

  @Test
  public void matchesArrayExpressionResultAgainstValuePatternWhenSpecified() {
    String json =
        "{\n" + "    \"volumeControl\": {\n" + "        \"max\": [1, 2, 3, 11]\n" + "    }\n" + "}";

    StringValuePattern pattern =
        WireMock.matchingJsonPath("$.volumeControl.max", WireMock.equalToJson("[1,2,3,11]"));
    MatchResult match = pattern.match(json);
    assertTrue(match.isExactMatch());
  }

  @Test
  public void matchesNotPresentExpressionResultAgainstAbsentValuePattern() {
    String json =
        "{\n" + "    \"volumeControl\": {\n" + "        \"max\": true\n" + "    }\n" + "}";

    StringValuePattern pattern = WireMock.matchingJsonPath("$.volumeControl.min", absent());
    MatchResult match = pattern.match(json);
    assertTrue(match.isExactMatch());
  }

  @Test
  public void matchesNullExpressionResultAgainstAbsentValuePattern() {
    String json =
        "{\n" + "    \"volumeControl\": {\n" + "        \"max\": null\n" + "    }\n" + "}";

    StringValuePattern pattern = WireMock.matchingJsonPath("$.volumeControl.max", absent());
    MatchResult match = pattern.match(json);
    assertTrue(match.isExactMatch());
  }

  @Test
  public void returnsTheDistanceFromTheValueMatcherWhenNotAMatch() {
    String json =
        "{\n" + "    \"volumeControl\": {\n" + "        \"max\": \"eleven\"\n" + "    }\n" + "}";

    StringValuePattern pattern = WireMock.matchingJsonPath("$.volumeControl.max", equalTo("ele"));
    MatchResult match = pattern.match(json);
    assertFalse(match.isExactMatch());
    assertThat(match.getDistance(), is(0.5));
  }

  @Test
  public void correctlySerialises() {
    assertThat(
        Json.write(WireMock.matchingJsonPath("$..thing")),
        equalToJson(
            "{                                \n"
                + "  \"matchesJsonPath\": \"$..thing\"       \n"
                + "}"));
  }

  @Test
  public void correctlySerialisesWithValuePattern() {
    assertThat(
        Json.write(WireMock.matchingJsonPath("$..thing", containing("123"))),
        equalToJson(
            "{                                      \n"
                + "    \"matchesJsonPath\": {              \n"
                + "        \"expression\": \"$..thing\",   \n"
                + "        \"contains\": \"123\"           \n"
                + "    }                                   \n"
                + "}"));
  }

  @Test
  public void correctlyDeserialises() {
    StringValuePattern stringValuePattern =
        Json.read(
            "{                                         \n"
                + "  \"matchesJsonPath\": \"$..thing\"       \n"
                + "}",
            StringValuePattern.class);

    assertThat(stringValuePattern, instanceOf(MatchesJsonPathPattern.class));
    assertThat(stringValuePattern.getExpected(), is("$..thing"));
  }

  @Test
  public void correctlyDeserialisesWithValuePattern() {
    StringValuePattern stringValuePattern =
        Json.read(
            "{                                      \n"
                + "    \"matchesJsonPath\": {              \n"
                + "        \"expression\": \"$..thing\",   \n"
                + "        \"equalTo\": \"the value\"      \n"
                + "    }                                   \n"
                + "}",
            StringValuePattern.class);

    assertThat(stringValuePattern, instanceOf(MatchesJsonPathPattern.class));
    assertThat(stringValuePattern.getExpected(), is("$..thing"));

    ContentPattern<?> subMatcher = ((MatchesJsonPathPattern) stringValuePattern).getValuePattern();
    assertThat(subMatcher, instanceOf(EqualToPattern.class));
    assertThat(subMatcher.getExpected(), is("the value"));
  }

  @Test
  public void correctlyDeserialisesWithAbsentValuePattern() {
    StringValuePattern stringValuePattern =
        Json.read(
            "{                                      \n"
                + "    \"matchesJsonPath\": {              \n"
                + "        \"expression\": \"$..thing\",   \n"
                + "        \"absent\": \"(absent)\"        \n"
                + "    }                                   \n"
                + "}",
            StringValuePattern.class);

    assertThat(stringValuePattern, instanceOf(MatchesJsonPathPattern.class));
    assertThat(stringValuePattern.getExpected(), is("$..thing"));

    ContentPattern<?> subMatcher = ((MatchesJsonPathPattern) stringValuePattern).getValuePattern();
    assertThat(subMatcher, instanceOf(AbsentPattern.class));
    assertThat(((StringValuePattern) subMatcher).nullSafeIsAbsent(), is(true));
  }

  @Test
  public void correctlyDeserialisesWhenSubMatcherHasExtraParameters() {
    StringValuePattern stringValuePattern =
        Json.read(
            "{                                       \n"
                + "    \"matchesJsonPath\": {              \n"
                + "        \"expression\": \"$..thing\",   \n"
                + "        \"equalToJson\": \"{}\",        \n"
                + "        \"ignoreExtraElements\": true,  \n"
                + "        \"ignoreArrayOrder\": true   \n"
                + "    }                                   \n"
                + "}",
            StringValuePattern.class);

    assertThat(stringValuePattern, instanceOf(MatchesJsonPathPattern.class));

    ContentPattern<?> subMatcher = ((MatchesJsonPathPattern) stringValuePattern).getValuePattern();
    assertThat(subMatcher, instanceOf(EqualToJsonPattern.class));
    assertThat(subMatcher.getExpected(), jsonEquals("{}"));
    assertThat(((EqualToJsonPattern) subMatcher).isIgnoreExtraElements(), is(true));
    assertThat(((EqualToJsonPattern) subMatcher).isIgnoreArrayOrder(), is(true));
  }

  @Test
  public void correctlySerialisesWhenSubMatcherHasExtraParameters() {
    StringValuePattern matcher =
        new MatchesJsonPathPattern("$..thing", WireMock.equalToJson("{}", true, true));

    String json = Json.write(matcher);

    assertThat(
        json,
        jsonEquals(
            "{                                       \n"
                + "    \"matchesJsonPath\": {              \n"
                + "        \"expression\": \"$..thing\",   \n"
                + "        \"equalToJson\": \"{}\",        \n"
                + "        \"ignoreExtraElements\": true,  \n"
                + "        \"ignoreArrayOrder\": true      \n"
                + "    }                                   \n"
                + "}"));
  }

  @Test
  public void throwsSensibleErrorOnDeserialisationWhenPatternIsBadlyFormedWithMissingExpression() {
    assertThrows(
        JsonException.class,
        () ->
            Json.read(
                "{                                      \n"
                    + "    \"matchesJsonPath\": {              \n"
                    + "        \"express\": \"$..thing\",      \n"
                    + "        \"equalTo\": \"the value\"      \n"
                    + "    }                                   \n"
                    + "}",
                StringValuePattern.class));
  }

  @Test
  public void
      throwsSensibleErrorOnDeserialisationWhenPatternIsBadlyFormedWithBadValuePatternName() {
    assertThrows(
        JsonException.class,
        () ->
            Json.read(
                "{                                      \n"
                    + "    \"matchesJsonPath\": {              \n"
                    + "        \"expression\": \"$..thing\",   \n"
                    + "        \"badOperator\": \"the value\"  \n"
                    + "    }                                   \n"
                    + "}",
                StringValuePattern.class));
  }

  @Test
  public void equalsIncludesValuePattern() {
    StringValuePattern pattern1 = matchingJsonPath("$.LinkageDetails.AccountId", equalTo("1000"));
    StringValuePattern pattern2 = matchingJsonPath("$.LinkageDetails.AccountId", equalTo("1001"));
    StringValuePattern pattern3 = matchingJsonPath("$.LinkageDetails.AccountId", equalTo("1000"));

    assertThat(pattern1, not(Matchers.equalTo(pattern2)));
    assertThat(pattern1.hashCode(), not(Matchers.equalTo(pattern2.hashCode())));

    assertThat(pattern1, Matchers.equalTo(pattern3));
    assertThat(pattern1.hashCode(), Matchers.equalTo(pattern3.hashCode()));
  }

  @Test
  public void treatsAnEmptyArrayExpressionResultAsAbsent() {
    String json =
        "{\n"
            + "  \"Books\": [\n"
            + "    {\n"
            + "      \"Author\": {\n"
            + "        \"Name\": \"1234567\",\n"
            + "        \"Price\": \"2.2\"\n"
            + "      }\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    MatchResult result = matchingJsonPath("$..[?(@.Author.ISBN)]", absent()).match(json);

    assertTrue(result.isExactMatch());
  }

  @Test
  public void matchesCorrectlyWhenSubMatcherIsUsedAndExpressionReturnsASingleItemArray() {
    String json =
        "{\n"
            + "   \"searchCriteria\": {\n"
            + "      \"customerId\": \"104903\",\n"
            + "      \"date\": \"01/01/2021\"\n"
            + "   }\n"
            + "}";

    MatchResult result =
        matchingJsonPath(
                "$.searchCriteria[?(@.customerId == '104903')].date",
                equalToDateTime("2021-01-01T00:00:00").actualFormat("dd/MM/yyyy"))
            .match(json);

    assertTrue(result.isExactMatch());
  }

  @Test
  public void objectsShouldBeEqualOnSameExpectedValue() {
    MatchesJsonPathPattern a =
        new MatchesJsonPathPattern("$.searchCriteria[?(@.customerId == '104903')].date");
    MatchesJsonPathPattern b =
        new MatchesJsonPathPattern("$.searchCriteria[?(@.customerId == '104903')].date");
    MatchesJsonPathPattern c =
        new MatchesJsonPathPattern("$.searchCriteria[?(@.customerId == '1234')].date");

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(b, a);
    assertEquals(b.hashCode(), a.hashCode());
    assertNotEquals(a, c);
    assertNotEquals(a.hashCode(), c.hashCode());
    assertNotEquals(b, c);
    assertNotEquals(b.hashCode(), c.hashCode());
  }

  private static Notifier setMockNotifier() {
    final Notifier notifier = Mockito.mock(Notifier.class);
    LocalNotifier.set(notifier);
    return notifier;
  }

  @AfterEach
  public void cleanUp() {
    LocalNotifier.set(null);
  }
}
