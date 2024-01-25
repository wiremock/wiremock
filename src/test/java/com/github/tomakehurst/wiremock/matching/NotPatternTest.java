/*
 * Copyright (C) 2023-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.common.Json;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class NotPatternTest {

  @Test
  public void serializesCorrectly() throws JSONException {
    String expectedJson = "{\"not\": {\"absent\": null}}";
    JSONAssert.assertEquals(expectedJson, Json.write(new NotPattern(AbsentPattern.ABSENT)), true);
  }

  @Test
  void deserializesCorrectly() {
    StringValuePattern stringValuePattern =
        Json.read("{ \"not\": { \"absent\": null } }", StringValuePattern.class);

    assertThat(stringValuePattern, instanceOf(NotPattern.class));
    assertThat(((NotPattern) stringValuePattern).getNot(), instanceOf(AbsentPattern.class));
  }

  @Test
  void shouldReturnExactMatchWhenValueIsNull() {
    MatchResult matchResult = not(containing("thing")).match(null);
    boolean result = matchResult.isExactMatch();

    assertTrue(result);
  }

  @Test
  void shouldReturnNoMatchWhenValueIsContainedInTestValue() {
    MatchResult matchResult = not(containing("thing")).match("otherthings");
    boolean result = matchResult.isExactMatch();
    double distance = matchResult.getDistance();

    assertFalse(result);
    assertThat(distance, is(1.0));
  }

  @Test
  void shouldReturnExactMatchWhenValueIsNotContainedInTestValue() {
    MatchResult matchResult = not(containing("thing")).match("otherstuff");
    boolean result = matchResult.isExactMatch();

    assertTrue(result);
  }
}
