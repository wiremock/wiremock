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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.jupiter.api.Test;

public class TemplatedMatcherAcceptanceTest extends AcceptanceTestBase {

  @Test
  void matchesQueryParameterAgainstAnotherQueryParameter() {
    stubFor(
        get(urlPathEqualTo("/test"))
            .withQueryParam("param2", new EqualToPattern("{{request.query.param1}}", null, true))
            .willReturn(ok()));

    // Should NOT match: param1=foo, but param2=bar (not equal)
    assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(404));

    // Should match: param1=foo and param2=foo (equal)
    assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(200));
  }
}
