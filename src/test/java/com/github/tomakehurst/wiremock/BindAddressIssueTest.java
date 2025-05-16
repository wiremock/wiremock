/*
 * Copyright (C) 2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.net.InetAddress;
import org.junit.jupiter.api.Test;

public class BindAddressIssueTest {

  @Test
  void dynamicPortGivesYouAGenuinelyUniquePort() {
    var wireMockBoundToLoopback =
        new WireMockServer(
            wireMockConfig()
                .dynamicPort()
                .bindAddress(InetAddress.getLoopbackAddress().getHostAddress()));
    var wireMockBoundToDefaultBindAddress = new WireMockServer(wireMockConfig().dynamicPort());

    try {
      wireMockBoundToLoopback.stubFor(get("/whoami").willReturn(ok("wireMockBoundToLoopback")));
      wireMockBoundToDefaultBindAddress.stubFor(
          get("/whoami").willReturn(ok("wireMockBoundToDefaultBindAddress")));

      wireMockBoundToLoopback.start();
      wireMockBoundToDefaultBindAddress.start();

      String content =
          new WireMockTestClient(wireMockBoundToDefaultBindAddress.port()).get("/whoami").content();

      assertThat(content).isEqualTo("wireMockBoundToDefaultBindAddress");
    } finally {
      wireMockBoundToLoopback.stop();
      wireMockBoundToDefaultBindAddress.stop();
    }
  }
}
