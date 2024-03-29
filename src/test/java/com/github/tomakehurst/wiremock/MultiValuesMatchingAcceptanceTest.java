/*
 * Copyright (C) 2016-2024 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.common.ContentTypes.COOKIE;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.Arrays;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MultiValuesMatchingAcceptanceTest extends AcceptanceTestBase {
  private static final String EXPECTED = "param1: [value1, value2]\n" + "param2: [value1]";
  private WireMockTestClient client;

  @BeforeEach
  public void init() {
    WireMock.reset();
    stubFor(get(WireMock.anyUrl()).willReturn(WireMock.ok()));
    client = new WireMockTestClient(wireMockServer.port());
  }

  @Test
  public void formRequests() {
    client.post(
        "/form",
        new UrlEncodedFormEntity(
            Arrays.asList(
                new BasicNameValuePair("param1", "value1"),
                new BasicNameValuePair("param1", "value2"),
                new BasicNameValuePair("param2", "value1"))));

    assertThat(findAll(anyRequestedFor(WireMock.anyUrl())).get(0).getFormParameters().summary())
        .isEqualTo(EXPECTED);
  }

  @Test
  public void cookies() {
    client.get("/cookie", withHeader(COOKIE, "param1=value1; param1=value2; param2=value1"));

    assertThat(findAll(anyRequestedFor(WireMock.anyUrl())).get(0).getCookies().summary())
        .isEqualTo(EXPECTED);
  }

  @Test
  public void queries() {
    client.get("/queries?param1=value1&param1=value2&param2=value1");

    assertThat(findAll(anyRequestedFor(WireMock.anyUrl())).get(0).getQueryParams().summary())
        .isEqualTo(EXPECTED);
  }
}
