/*
 * Copyright (C) 2019-2022 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Json;
import org.junit.jupiter.api.Test;

public class AbsentPatternTest {

  @Test
  public void correctlyDeserializesFromJson() {
    StringValuePattern stringValuePattern =
        Json.read(
            "{                             \n" + "  \"absent\": \"(absent)\"    \n" + "}",
            StringValuePattern.class);

    assertThat(stringValuePattern, instanceOf(AbsentPattern.class));
    assertThat(stringValuePattern.isAbsent(), is(true));
  }

  @Test
  public void objectsShouldBeEqualOnSameExpectedValue() {
    AbsentPattern a = new AbsentPattern("someString");
    AbsentPattern b = new AbsentPattern("someString");
    AbsentPattern c = new AbsentPattern("someOtherString");

    assertThat(a, equalTo(b));
    assertThat(b, equalTo(a));
    assertThat(a, not(equalTo(c)));
    assertThat(b, not(equalTo(c)));
  }
}
