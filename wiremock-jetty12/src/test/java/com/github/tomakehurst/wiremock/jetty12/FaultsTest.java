/*
 * Copyright (C) 2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.hc.core5.http.MalformedChunkCodingException;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class FaultsTest {

  @RegisterExtension
  static WireMockExtension wm =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  static WireMockTestClient testClient;

  @BeforeAll
  static void init() {
    testClient = new WireMockTestClient(wm.getPort());
    WireMock.configureFor(wm.getPort());
  }

  @Test
  public void connectionResetByPeerFault() {
    stubFor(
        get(urlEqualTo("/connection/reset"))
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

    RuntimeException runtimeException =
        assertThrows(RuntimeException.class, () -> testClient.get("/connection/reset"));
    assertThat(runtimeException.getMessage(), is("java.net.SocketException: Connection reset"));
  }

  @Test
  public void emptyResponseFault() {
    stubFor(
        get(urlEqualTo("/empty/response")).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

    getAndAssertUnderlyingExceptionInstanceClass("/empty/response", NoHttpResponseException.class);
  }

  @Test
  public void malformedResponseChunkFault() {
    stubFor(
        get(urlEqualTo("/malformed/response"))
            .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

    getAndAssertUnderlyingExceptionInstanceClass(
        "/malformed/response", MalformedChunkCodingException.class);
  }

  @Test
  public void randomDataOnSocketFault() {
    stubFor(
        get(urlEqualTo("/random/data"))
            .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

    getAndAssertUnderlyingExceptionInstanceClass("/random/data", NoHttpResponseException.class);
  }

  private void getAndAssertUnderlyingExceptionInstanceClass(String url, Class<?> expectedClass) {
    boolean thrown = false;
    try {
      WireMockResponse response = testClient.get(url);
      response.content();
    } catch (Exception e) {
      assertThat(e.getCause(), instanceOf(expectedClass));
      thrown = true;
    }

    assertTrue(thrown, "No exception was thrown");
  }
}
