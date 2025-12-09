/*
 * Copyright (C) 2025 Thomas Akehurst
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
package org.wiremock.url;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.wiremock.url.whatwg.WhatWGUrlTestCase;
import org.wiremock.url.whatwg.WhatWGUrlTestManagement;

public class UrlReferenceTests {

  @SuppressWarnings("unused")
  private static final List<WhatWGUrlTestCase> wiremock_valid =
      WhatWGUrlTestManagement.wiremock_valid;

  @ParameterizedTest
  @FieldSource("wiremock_valid")
  void wiremock_valid(WhatWGUrlTestCase testCase) {
    testValid(testCase.input());
  }

  @SuppressWarnings("unused")
  private static final List<WhatWGUrlTestCase> wiremock_invalid =
      WhatWGUrlTestManagement.wiremock_invalid;

  @ParameterizedTest
  @FieldSource("wiremock_invalid")
  void wiremock_invalid(WhatWGUrlTestCase testCase) {
    assertThatExceptionOfType(IllegalUrlReference.class)
        .isThrownBy(() -> UrlReference.parse(testCase.input()));
  }

  // convenience way to test specific cases
  @Test
  void debug() {
    testValid("//foo/bar");
  }

  private static void testValid(String input) {
    var urlReference = UrlReference.parse(input);
    assertThat(urlReference.toString()).isEqualTo(input);
  }
}
