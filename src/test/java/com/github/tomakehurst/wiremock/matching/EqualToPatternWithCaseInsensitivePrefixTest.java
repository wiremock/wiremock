/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.testsupport.MockWireMockServices;
import org.junit.jupiter.api.Test;

public class EqualToPatternWithCaseInsensitivePrefixTest {

  @Test
  public void matchesTemplatedValueWithCaseInsensitivePrefix() {
    MockRequest request = mockRequest().url("/token");
    ServeContext context = new ServeContext(new MockWireMockServices(), request);
    EqualToPatternWithCaseInsensitivePrefix pattern =
        new EqualToPatternWithCaseInsensitivePrefix("Basic ", "{{request.path}}").templated();

    assertThat(pattern.match("bAsIc /token", context).isExactMatch(), is(true));
  }

  @Test
  public void matchesTemplatedPrefixWithCaseInsensitivePrefix() {
    MockRequest request = mockRequest().url("/token");
    ServeContext context = new ServeContext(new MockWireMockServices(), request);
    EqualToPatternWithCaseInsensitivePrefix pattern =
        new EqualToPatternWithCaseInsensitivePrefix("{{request.path}}", "suffix").templated();

    assertThat(pattern.match("/tokensuffix", context).isExactMatch(), is(true));
  }

  @Test
  public void matchesTemplatedPrefixAndValueWithCaseInsensitivePrefix() {
    MockRequest request = mockRequest().url("/token");
    ServeContext context = new ServeContext(new MockWireMockServices(), request);
    EqualToPatternWithCaseInsensitivePrefix pattern =
        new EqualToPatternWithCaseInsensitivePrefix("{{request.path}}", "{{request.path}}")
            .templated();

    assertThat(pattern.match("/token/token", context).isExactMatch(), is(true));
  }
}
