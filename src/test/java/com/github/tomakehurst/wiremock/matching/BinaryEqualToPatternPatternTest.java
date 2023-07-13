/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BinaryEqualToPatternPatternTest {

  @Test
  void returns1ForNonMatch() {
    ValueMatcher<byte[]> pattern = WireMock.binaryEqualTo(new byte[] {1, 2, 3});
    byte[] actual = {4, 5, 6};

    MatchResult match = pattern.match(actual);

    assertThat(match.getDistance(), is(1.0));
    assertThat(match.isExactMatch(), is(false));
  }

  @Test
  void returns0WhenExactlyEqual() {
    ValueMatcher<byte[]> pattern = WireMock.binaryEqualTo(new byte[] {1, 2, 3});
    byte[] actual = {1, 2, 3};

    MatchResult match = pattern.match(actual);

    assertThat(match.getDistance(), is(0.0));
    assertThat(match.isExactMatch(), is(true));
  }

  @Test
  void returnsNonMatchWheActualIsNull() {
    ValueMatcher<byte[]> pattern = WireMock.binaryEqualTo(new byte[] {1, 2, 3});
    byte[] actual = null;

    MatchResult match = pattern.match(actual);

    assertThat(match.getDistance(), is(1.0));
    assertThat(match.isExactMatch(), is(false));
  }

  @Test
  void serialisesCorrectly() throws Exception {
    byte[] expected = {5, 5, 5, 5};
    String base64Expected = Base64.getEncoder().encodeToString(expected);
    String expectedJson =
        "{                                                   \n"
            + "  \"binaryEqualTo\": \""
            + base64Expected
            + "\"     \n"
            + "}";
    assertEquals(expectedJson, Json.write(new BinaryEqualToPattern(expected)), true);
  }

  @Test
  @SuppressWarnings("unchecked")
  void deserializesCorrectly() {
    String base64Expected = Base64.getEncoder().encodeToString(new byte[] {1, 2, 3});

    ContentPattern<byte[]> pattern =
        Json.read(
            "{                                              \n"
                + "  \"binaryEqualTo\": \""
                + base64Expected
                + "\"    \n"
                + "}",
            ContentPattern.class);

    assertThat(pattern, instanceOf(BinaryEqualToPattern.class));
    assertThat(pattern.getExpected(), is(base64Expected));
  }

  @Test
  void objectsShouldBeEqualOnSameExpectedValue() {
    BinaryEqualToPattern a = new BinaryEqualToPattern(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
    BinaryEqualToPattern b = new BinaryEqualToPattern(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
    BinaryEqualToPattern c = new BinaryEqualToPattern(new byte[] {0, 8, 15});

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
