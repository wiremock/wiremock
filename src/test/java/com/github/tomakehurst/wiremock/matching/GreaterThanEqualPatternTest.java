/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GreaterThanEqualPatternTest {

  @Test
  public void failsForNoMatchOnGreaterThanInt() {
    StringValuePattern pattern = WireMock.greaterThanEqual("5");
    assertFalse(pattern.match("1").isExactMatch());
    assertThat(pattern.match("1").getDistance(), is(0.01));
  }

  @Test
  public void succeedsForNoMatchEqualOnGreaterThanInt() {
    StringValuePattern pattern = WireMock.greaterThanEqual("1");
    assertTrue(pattern.match("1").isExactMatch());
    assertThat(pattern.match("1").getDistance(), is(0.00));
  }

  @Test
  public void failsForNoMatchOnGreaterThanFloat() {
    StringValuePattern pattern = WireMock.greaterThanEqual("5.5");
    assertFalse(pattern.match("1.1").isExactMatch());
    assertThat(pattern.match("1.1").getDistance(), is(0.01));
  }

  @Test
  public void succeedsForNoMatchEqualOnGreaterThanFloat() {
    StringValuePattern pattern = WireMock.greaterThanEqual("1.1");
    assertTrue(pattern.match("1.1").isExactMatch());
    assertThat(pattern.match("1.1").getDistance(), is(0.00));
  }

  @Test
  public void succeedsForExactMatchOnGreaterThanInt() {
    StringValuePattern pattern = WireMock.greaterThanEqual("1");
    assertTrue(pattern.match("2").isExactMatch());
    assertThat(pattern.match("2").getDistance(), is(0.0));
  }

  @Test
  public void succeedsForExactMatchOnGreaterThanFloat() {
    StringValuePattern pattern = WireMock.greaterThanEqual("1.1111");
    assertTrue(pattern.match("1.1112").isExactMatch());
    assertThat(pattern.match("1.1112").getDistance(), is(0.0));
  }

  @Test
  public void correctlyDeserialisesGreaterThanEqualFromJson() {
    StringValuePattern stringValuePattern =
        Json.read("{ \"greaterThanEqual\": \"1\" }", StringValuePattern.class);

    assertThat(stringValuePattern, instanceOf(GreaterThanEqualPattern.class));
    assertThat(stringValuePattern.getValue(), is("1"));
  }

  @Test
  public void correctlySerialisesToJson() throws Exception {
    assertEquals(
        "{ \"greaterThanEqual\": \"1\" }", Json.write(new GreaterThanEqualPattern("1")), false);
  }

  @Test
  public void noMatchOnNullValue() {
    assertThat(WireMock.greaterThanEqual("1").match(null).isExactMatch(), is(false));
  }

  @Test
  public void noMatchOnStringValue() {
    assertThat(WireMock.greaterThanEqual("1").match("a string").isExactMatch(), is(false));
  }

  @Test
  public void noMatchOnExpectedString() {
    assertThat(WireMock.greaterThanEqual("a string").match("1").isExactMatch(), is(false));
  }

  @Test
  public void objectsShouldBeEqualOnSameExpectedValue() {
    var a = new GreaterThanEqualPattern("1");
    var b = new GreaterThanEqualPattern("1");
    var c = new GreaterThanEqualPattern("2");

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
