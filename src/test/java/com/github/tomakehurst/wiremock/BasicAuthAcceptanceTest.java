/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.http.MultiValue;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

public class BasicAuthAcceptanceTest extends AcceptanceTestBase {

  @Test
  public void matchesPreemptiveBasicAuthWhenCredentialAreCorrect() {
    stubFor(
        get(urlEqualTo("/basic/auth/preemptive"))
            .withBasicAuth("the-username", "thepassword")
            .willReturn(aResponse().withStatus(200)));

    WireMockResponse response =
        testClient.getWithPreemptiveCredentials(
            "/basic/auth/preemptive", wireMockServer.port(), "the-username", "thepassword");

    assertThat(response.statusCode(), is(200));
  }

  @Test
  public void doesNotMatchPreemptiveBasicAuthWhenCredentialsAreIncorrect() {
    stubFor(
        get(urlEqualTo("/basic/auth/preemptive"))
            .withBasicAuth("the-username", "thepassword")
            .willReturn(aResponse().withStatus(200)));

    WireMockResponse response =
        testClient.getWithPreemptiveCredentials(
            "/basic/auth/preemptive", wireMockServer.port(), "the-username", "WRONG!!!");

    assertThat(response.statusCode(), is(404));
  }

  @Test
  public void matcheswhenBASICInHeaderIsAllUpperCase() {
    stubFor(
        get(urlEqualTo("/basic/auth/case-insensitive"))
            .withBasicAuth("tom", "secret")
            .willReturn(aResponse().withStatus(200)));

    assertThat(
        testClient
            .get(
                "/basic/auth/case-insensitive",
                withHeader("Authorization", "BASIC dG9tOnNlY3JldA=="))
            .statusCode(),
        is(200));
  }

  @Test
  public void doesNotMatchWhenBase64UsesIncorrectCase() {
    MultiValuePattern matcher =
        new BasicCredentials("tom", "my-secret").asAuthorizationMultiValuePattern();

    String goodCreds = "dG9tOm15LXNlY3JldA==";
    String badCreds = "dG9tom15LXNlY3JldA==";

    // expect
    assertThat(goodCreds, not(badCreds));
    assertThat(goodCreds, equalToIgnoringCase(badCreds));
    assertTrue(
        matcher
            .match(new MultiValue("Authorization", List.of("Basic " + goodCreds)))
            .isExactMatch());

    assertFalse(
        matcher
            .match(new MultiValue("Authorization", List.of("Basic " + badCreds)))
            .isExactMatch());
  }
}
