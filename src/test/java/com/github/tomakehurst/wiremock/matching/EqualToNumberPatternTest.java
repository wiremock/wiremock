/*
 * Copyright (C) 2016-2026 Thomas Akehurst
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

public class EqualToNumberPatternTest {

  @Test
  public void failsForNoMatchOnEqualsInt() {
    StringValuePattern pattern = WireMock.equalToNumber(5);
    assertFalse(pattern.match("1").isExactMatch());
    assertThat(pattern.match("1").getDistance(), is(0.01));
  }

  @Test
  public void failsForNoMatchOnEqualsIntContains() {
    StringValuePattern pattern = WireMock.equalToNumber(1);
    assertFalse(pattern.match("1111").isExactMatch());
    assertThat(pattern.match("1111").getDistance(), is(0.04));
  }

  @Test
  public void failsForNoMatchOnEqualsFloat() {
    StringValuePattern pattern = WireMock.equalToNumber(5.5);
    assertFalse(pattern.match("1.1").isExactMatch());
    assertThat(pattern.match("1.1").getDistance(), is(0.01));
  }

  @Test
  public void succeedsForExactMatchOnEqualsInt() {
    StringValuePattern pattern = WireMock.equalToNumber(1);
    assertTrue(pattern.match("1").isExactMatch());
    assertThat(pattern.match("1").getDistance(), is(0.0));
  }

  @Test
  public void succeedsForExactMatchOnEqualsFloat() {
    StringValuePattern pattern = WireMock.equalToNumber(1.1111);
    assertTrue(pattern.match("1.1111").isExactMatch());
    assertThat(pattern.match("1.1111").getDistance(), is(0.0));
  }

  @Test
  public void succeedsForExactMatchOnEqualsFloatWithExtraDecimals() {
    StringValuePattern pattern = WireMock.equalToNumber(1.1111);
    assertTrue(pattern.match("0001.1111000").isExactMatch());
    assertThat(pattern.match("0001.1111000").getDistance(), is(0.0));
  }

  @Test
  public void correctlyDeserialisesEqualToNumberWithStringValueFromJson() {
    StringValuePattern stringValuePattern =
        Json.read("{ \"equalToNumber\": \"1\" }", StringValuePattern.class);

    assertThat(stringValuePattern, instanceOf(EqualToNumberPattern.class));
    assertThat(stringValuePattern.getValue(), is("1.0"));
  }

  @Test
  public void correctlyDeserialisesEqualToNumberWithNumberValueFromJson() {
    StringValuePattern stringValuePattern =
        Json.read("{ \"equalToNumber\": 1 }", StringValuePattern.class);

    assertThat(stringValuePattern, instanceOf(EqualToNumberPattern.class));
    assertThat(stringValuePattern.getValue(), is("1.0"));
  }

  @Test
  public void correctlySerialisesToJson() throws Exception {
    assertEquals("{ \"equalToNumber\": 1 }", Json.write(new EqualToNumberPattern(1)), false);
  }

  @Test
  public void noMatchOnNullValue() {
    assertThat(WireMock.equalToNumber(1).match(null).isExactMatch(), is(false));
  }

  @Test
  public void noMatchOnStringValue() {
    assertThat(WireMock.equalToNumber(1).match("a string").isExactMatch(), is(false));
  }

  @Test
  public void objectsShouldBeEqualOnSameExpectedValue() {
    var a = new EqualToNumberPattern(1);
    var b = new EqualToNumberPattern(1);
    var c = new EqualToNumberPattern(2);

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
