/*
 * Copyright (C) 2023 Thomas Akehurst
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
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class PathTemplatePatternTest {

  @Test
  void returns_exact_match_when_path_matches_template() {
    PathTemplatePattern pattern = new PathTemplatePattern("/one/{id}/two");
    assertThat(pattern.match("/one/3/two").isExactMatch(), is(true));
  }

  @Test
  void returns_no_match_and_low_distance_when_path_almost_matches_template() {
    PathTemplatePattern pattern = new PathTemplatePattern("/one/{id}/two");

    MatchResult matchResult = pattern.match("/on/3/two");

    assertThat(matchResult.isExactMatch(), is(false));
    assertThat(matchResult.getDistance(), closeTo(0.2, 0.05));
  }

  @Test
  void returns_no_match_and_high_distance_when_path_is_very_different_from_template() {
    PathTemplatePattern pattern = new PathTemplatePattern("/one/{id}/two");

    MatchResult matchResult = pattern.match("/totally/different/stuff/and/length");

    assertThat(matchResult.isExactMatch(), is(false));
    assertThat(matchResult.getDistance(), closeTo(0.9, 0.05));
  }
}
