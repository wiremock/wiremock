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
package org.wiremock.url;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BaseUrlTests {

  @Nested
  class Parse {

    @Test
    void non_base_path_rejected() {

      IllegalBaseUrl exception =
          assertThatExceptionOfType(IllegalBaseUrl.class)
              .isThrownBy(() -> BaseUrl.parse("https://example.com/leaf"))
              .actual();

      assertThat(exception)
          .hasMessage(
              "Illegal base url: `https://example.com/leaf`; path must be a base path (empty or end in `/`), query must be null, fragment must be null")
          .hasNoCause();
      assertThat(exception.getIllegalValue()).isEqualTo("https://example.com/leaf");
    }
  }

  @Nested
  class Resolve {

    @Test
    void relative_path_appended_to_empty_path() {

      BaseUrl baseUrl = BaseUrl.parse("https://example.com");

      AbsoluteUrl resolved = baseUrl.resolve("some/path");

      assertThat(resolved).isEqualTo(AbsoluteUrl.parse("https://example.com/some/path"));
    }

    @Test
    void relative_path_appended_to_base_path() {

      BaseUrl baseUrl = BaseUrl.parse("https://example.com/base/path/");

      AbsoluteUrl resolved = baseUrl.resolve("some/path");

      assertThat(resolved).isEqualTo(AbsoluteUrl.parse("https://example.com/base/path/some/path"));
    }
  }
}
