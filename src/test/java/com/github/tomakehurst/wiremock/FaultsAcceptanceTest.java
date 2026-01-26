/*
 * Copyright (C) 2024-2026 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.http.Fault;
import java.net.SocketException;
import org.apache.hc.core5.http.MalformedChunkCodingException;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.junit.jupiter.api.Test;

public class FaultsAcceptanceTest extends AcceptanceTestBase {

  @Test
  public void connectionResetByPeerFault() {
    stubFor(
        get(urlEqualTo("/connection/reset"))
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

    SocketException socketException =
        assertThrows(SocketException.class, () -> testClient.get("/connection/reset"));
    assertThat(socketException.getMessage(), is("Connection reset"));
  }

  @Test
  public void emptyResponseFault() {
    stubFor(
        get(urlEqualTo("/empty/response")).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

    assertThrows(NoHttpResponseException.class, () -> testClient.get("/empty/response"));
  }

  @Test
  public void malformedResponseChunkFault() {
    stubFor(
        get(urlEqualTo("/malformed/response"))
            .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

    assertThrows(MalformedChunkCodingException.class, () -> testClient.get("/malformed/response"));
  }

  @Test
  public void randomDataOnSocketFault() {
    stubFor(
        get(urlEqualTo("/random/data"))
            .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

    assertThrows(NoHttpResponseException.class, () -> testClient.get("/random/data"));
  }
}
