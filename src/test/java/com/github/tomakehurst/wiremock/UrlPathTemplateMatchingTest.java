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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class UrlPathTemplateMatchingTest extends AcceptanceTestBase {

  @Test
  void matches_path_template_without_bound_variable() {
    stubFor(get(urlPathTemplate("/v1/contacts/{contactId}")).willReturn(ok()));

    assertThat(testClient.get("/v1/contacts/12345").statusCode(), is(200));
    assertThat(testClient.get("/v1/contacts/23456").statusCode(), is(200));
  }

  @Test
  void matches_path_template_with_single_bound_variable() {
    stubFor(
        get(urlPathTemplate("/v1/contacts/{contactId}/addresses/{addressId}"))
            .withPathParam("contactId", equalTo("12345"))
            .withPathParam("addressId", equalTo("99876"))
            .willReturn(ok()));

    assertThat(testClient.get("/v1/contacts/12345/addresses/99876").statusCode(), is(200));
    assertThat(testClient.get("/v1/contacts/23456/addresses/55555").statusCode(), is(404));
  }
}
