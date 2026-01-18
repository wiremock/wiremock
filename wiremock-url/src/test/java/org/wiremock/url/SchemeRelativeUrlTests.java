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
import static org.wiremock.url.Scheme.https;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SchemeRelativeUrlTests {

  @Nested
  class Transform {

    @Test
    void can_transform_a_scheme_relative_url() {
      SchemeRelativeUrl url = SchemeRelativeUrl.parse("//example.com/path");
      Url transformed = url.transform(b -> b.setScheme(https).setQuery(Query.parse("a=b")));
      assertThat(transformed)
          .isInstanceOf(AbsoluteUrl.class)
          .hasToString("https://example.com/path?a=b");
    }

    @Test
    void can_pointlessly_set_scheme_to_null() {
      SchemeRelativeUrl url = SchemeRelativeUrl.parse("//example.com/path#fragment");
      Url transformed = url.transform(it -> it.setScheme(null));
      assertThat(transformed).isInstanceOf(SchemeRelativeUrl.class).isEqualTo(url);
    }

    @Test
    void can_set_authority_to_null() {
      SchemeRelativeUrl url = SchemeRelativeUrl.parse("//example.com/path#fragment");
      Url transformed = url.transform(it -> it.setAuthority(null));
      assertThat(transformed)
          .isInstanceOf(RelativeUrl.class)
          .isEqualTo(RelativeUrl.parse("/path#fragment"));
    }
  }
}
