/*
 * Copyright (C) 2022 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LimitTest {

  @Test
  void indicates_value_exceeds_limit() {
    Limit limit = new Limit(5);
    assertThat(limit.isExceededBy(4), is(false));
    assertThat(limit.isExceededBy(5), is(false));
    assertThat(limit.isExceededBy(6), is(true));
  }

  @Test
  void indicates_unlimited() {
    assertTrue(Limit.UNLIMITED.isUnlimited());
  }
}
