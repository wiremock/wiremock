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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.JsonException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EqualToPatternTest {

  @Test
  void returnsANonZeroScoreForPartialMatchOnEquals() {
    StringValuePattern pattern = WireMock.equalTo("matchthis");
    assertThat(pattern.match("matchthisbadlydone").getDistance(), is(0.5));
  }

  @Test
  void returns1ForNoMatchOnEquals() {
    StringValuePattern pattern = WireMock.equalTo("matchthis");
    assertThat(pattern.match("924387348975923").getDistance(), is(1.0));
  }

  @Test
  void returns0ForExactMatchOnEquals() {
    StringValuePattern pattern = WireMock.equalTo("matchthis");
    assertThat(pattern.match("matchthis").getDistance(), is(0.0));
  }

  @Test
  void matchesCaseInsensitive() {
    StringValuePattern pattern = WireMock.equalToIgnoreCase("MaTchtHis");
    assertThat(pattern.match("matchthis").isExactMatch(), is(true));
  }

  @Test
  void correctlyDeserialisesEqualToFromJson() {
    StringValuePattern stringValuePattern =
        Json.read(
            "{                               \n" + "  \"equalTo\": \"something\"    \n" + "}",
            StringValuePattern.class);

    assertThat(stringValuePattern, instanceOf(EqualToPattern.class));
    assertThat(stringValuePattern.getValue(), is("something"));
  }

  @Test
  void correctlyDeserialisesEqualToFromJsonWithIgnoreCase() {
    StringValuePattern stringValuePattern =
        Json.read(
            "{                              \n"
                + "  \"equalTo\": \"something\",   \n"
                + "  \"caseInsensitive\": true     \n"
                + "}",
            StringValuePattern.class);

    assertThat(stringValuePattern, instanceOf(EqualToPattern.class));
    assertThat(stringValuePattern.getValue(), is("something"));
    assertThat(((EqualToPattern) stringValuePattern).getCaseInsensitive(), is(true));
  }

  @Test
  void correctlySerialisesToJson() throws Exception {
    assertEquals(
        "{                               \n" + "  \"equalTo\": \"something\"    \n" + "}",
        Json.write(new EqualToPattern("something")),
        false);
  }

  @Test
  void failsWithMeaningfulErrorWhenOperatorNotRecognised() {
    try {
      Json.read(
          "{                               \n" + "  \"munches\": \"something\"    \n" + "}",
          StringValuePattern.class);

      fail();
    } catch (Exception e) {
      assertThat(e, instanceOf(JsonException.class));
      JsonException jsonException = (JsonException) e;
      assertThat(
          jsonException.getErrors().first().getDetail(),
          containsString("{\"munches\":\"something\"} is not a valid match operation"));
    }
  }

  @Test
  void noMatchOnNullValue() {
    assertThat(WireMock.equalTo("this_thing").match(null).isExactMatch(), is(false));
  }

  @Test
  void objectsShouldBeEqualOnSameExpectedValue() {
    EqualToPattern a = new EqualToPattern("someString");
    EqualToPattern b = new EqualToPattern("someString");
    EqualToPattern c = new EqualToPattern("someOtherString");

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
