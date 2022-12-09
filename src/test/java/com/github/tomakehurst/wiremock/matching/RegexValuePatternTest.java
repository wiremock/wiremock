/*
 * Copyright (C) 2016-2022 Thomas Akehurst
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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class RegexValuePatternTest {

  @Test
  public void correctlySerialisesMatchesAsJson() throws Exception {
    String actual = Json.write(WireMock.matching("something"));
    System.out.println(actual);
    JSONAssert.assertEquals(
        "{                               \n" + "  \"matches\": \"something\"    \n" + "}",
        actual,
        true);
  }

  @Test
  public void correctlyDeserialisesMatchesFromJson() {
    StringValuePattern stringValuePattern =
        Json.read(
            "{                               \n" + "  \"matches\": \"something\"    \n" + "}",
            StringValuePattern.class);

    assertThat(stringValuePattern, instanceOf(RegexPattern.class));
    assertThat(stringValuePattern.getValue(), is("something"));
  }

  @Test
  public void noMatchWhenValueIsNull() {
    assertThat(WireMock.matching(".*").match(null).isExactMatch(), is(false));
  }

  @Test
  public void objectsShouldBeEqualOnSameExpectedValue() {
    RegexPattern a = new RegexPattern("test");
    RegexPattern b = new RegexPattern("test");
    RegexPattern c = new RegexPattern("anotherTest");

    assertEquals(a, b);
    assertEquals(b, a);
    assertNotEquals(a, c);
    assertNotEquals(b, c);
  }
}
