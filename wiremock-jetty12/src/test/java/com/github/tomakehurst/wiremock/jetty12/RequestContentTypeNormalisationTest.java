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
package com.github.tomakehurst.wiremock.jetty12;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RequestContentTypeNormalisationTest {

  static final String SENT_CONTENT_TYPE = "application/json; charset=UTF-8";

  @RegisterExtension
  static WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().httpServerFactory(new Jetty12HttpServerFactory()))
          .build();

  @Test
  public void doesNotMatchUnnormalisedRequestContentTypeHeaderValue() {
    String url = "/unnormalised-content-type";
    wm.stubFor(post(url).withHeader("Content-Type", equalTo(SENT_CONTENT_TYPE)).willReturn(ok()));

    int status = postWithContentType(url).statusCode();

    assertThat(status, is(404));
  }

  @Test
  public void matchesNormalisedRequestContentTypeHeaderValue() {
    String url = "/normalised-content-type";
    wm.stubFor(
        post(url)
            .withHeader("Content-Type", equalTo("application/json; charset=utf-8"))
            .willReturn(ok()));

    int status = postWithContentType(url).statusCode();

    assertThat(status, is(200));
  }

  private static WireMockResponse postWithContentType(String url) {
    return new WireMockTestClient(wm.getPort())
        .post(url, new StringEntity("{}"), withHeader("Content-Type", SENT_CONTENT_TYPE));
  }
}
