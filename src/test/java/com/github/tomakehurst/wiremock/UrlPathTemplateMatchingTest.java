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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.common.InvalidInputException;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.jupiter.api.Test;

class UrlPathTemplateMatchingTest extends AcceptanceTestBase {

  @Test
  void matches_path_template_without_bound_variable() {
    stubFor(get(urlPathTemplate("/v1/contacts/{contactId}")).willReturn(ok()));

    assertThat(testClient.get("/v1/contacts/12345").statusCode(), is(200));
    assertThat(testClient.get("/v1/contacts/23456").statusCode(), is(200));

    assertThat(testClient.get("/v2/contacts/23456").statusCode(), is(404));
  }

  @Test
  void matches_path_template_with_single_bound_variable() {
    stubFor(
        get(urlPathTemplate("/v1/contacts/{contactId}/addresses/{addressId}"))
            .withPathParam("contactId", equalTo("12345"))
            .withPathParam("addressId", equalTo("99876"))
            .willReturn(ok()));

    assertThat(testClient.get("/v1/contacts/12345/addresses/99876").statusCode(), is(200));

    assertThat(testClient.get("/v1/contacts/12345/addresses/55555").statusCode(), is(404));
    assertThat(testClient.get("/v1/contacts/23456/addresses/99876").statusCode(), is(404));
    assertThat(testClient.get("/v1/contacts/23456/addresses/55555").statusCode(), is(404));
  }

  @Test
  void returns_non_match_without_error_when_request_url_path_does_not_match_template() {
    stubFor(
        get(urlPathTemplate("/contacts/{contactId}/addresses/{addressId}"))
            .withPathParam("contactId", equalTo("123"))
            .willReturn(ok()));

    WireMockResponse response = testClient.get("/contacts/123/addresssssses/1");
    assertThat(response.content(), containsString("Request was not matched"));
    assertThat(response.statusCode(), is(404));
  }

  @Test
  void static_dsl_throws_error_when_attempting_to_use_path_param_matchers_without_path_template() {
    assertThrows(
        InvalidInputException.class,
        () ->
            stubFor(
                get(urlPathEqualTo("/stuff"))
                    .withPathParam("wrong", containing("things"))
                    .willReturn(ok())));
  }

  @Test
  void
      instance_dsl_throws_error_when_attempting_to_use_path_param_matchers_without_path_template() {
    assertThrows(
        InvalidInputException.class,
        () ->
            wm.stubFor(
                get(urlPathEqualTo("/stuff"))
                    .withPathParam("wrong", containing("things"))
                    .willReturn(ok())));
  }
}
