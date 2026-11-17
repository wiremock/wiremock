/*
 * Copyright (C) 2026 Thomas Akehurst
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
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class EqualToPatternWithCaseInsensitivePrefixTest {

  private final StringValuePattern pattern =
      new EqualToPatternWithCaseInsensitivePrefix("Basic ", "dXNlcjpwYXNz");

  @Test
  public void doesNotMatchWhenValueIsShorterThanThePrefix() {
    assertThat(pattern.match("abc").isExactMatch(), is(false));
  }

  @Test
  public void doesNotMatchWhenValueIsEmpty() {
    assertThat(pattern.match("").isExactMatch(), is(false));
  }

  @Test
  public void matchesWhenPrefixIsEqualIgnoringCaseAndSuffixMatches() {
    assertThat(pattern.match("BASIC dXNlcjpwYXNz").isExactMatch(), is(true));
  }

  @Test
  public void doesNotMatchWhenValueIsExactlyThePrefixLength() {
    assertThat(pattern.match("Basic ").isExactMatch(), is(false));
  }

  @Test
  public void doesNotMatchWhenValueIsNull() {
    assertThat(pattern.match(null).isExactMatch(), is(false));
  }

  @Test
  public void doesNotMatchWhenPrefixMatchesButSuffixDiffers() {
    assertThat(pattern.match("Basic wrongvalue").isExactMatch(), is(false));
  }
}
