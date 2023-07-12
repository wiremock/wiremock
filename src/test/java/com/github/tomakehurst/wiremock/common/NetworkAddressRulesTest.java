/*
 * Copyright (C) 2022 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.net.InetAddress;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class NetworkAddressRulesTest {

  @Test
  void allowsAddressIncludedAndNotExcluded() {
    NetworkAddressRules rules =
        NetworkAddressRules.builder()
            .allow("10.1.1.1-10.2.1.1")
            .allow("192.168.1.1-192.168.2.1")
            .deny("10.1.2.3")
            .deny("10.5.5.5")
            .build();

    assertThat(rules.isAllowed("192.168.1.111"), is(true));

    assertThat(rules.isAllowed("10.1.2.1"), is(true));
    assertThat(rules.isAllowed("10.1.2.3"), is(false));
    assertThat(rules.isAllowed("10.5.5.5"), is(false));
  }

  @Test
  void allowsHostNameIncludedAndNotExcluded() {
    NetworkAddressRules rules =
        NetworkAddressRules.builder()
            .allow("subdomainA.domain.com")
            .allow("*.subdomainB.domain.com")
            .deny("subdomainC.domain.com")
            .deny("*.subdomainD.domain.com")
            .build();

    assertThat(rules.isAllowed(mockInetAddressForHostName("subdomainA.domain.com")), is(true));

    assertThat(rules.isAllowed(mockInetAddressForHostName("host.subdomainB.domain.com")), is(true));
    assertThat(rules.isAllowed(mockInetAddressForHostName("subdomainC.domain.com")), is(false));
    assertThat(
        rules.isAllowed(mockInetAddressForHostName("host.subdomainD.domain.com")), is(false));
  }

  @Test
  void onlyAllowSingleIp() {
    NetworkAddressRules rules = NetworkAddressRules.builder().allow("10.1.1.1").build();

    assertThat(rules.isAllowed("10.1.1.1"), is(true));
    assertThat(rules.isAllowed("10.1.1.0"), is(false));
    assertThat(rules.isAllowed("10.1.1.2"), is(false));
  }

  @Test
  void onlyDenySingleIp() {
    NetworkAddressRules rules = NetworkAddressRules.builder().deny("10.1.1.1").build();

    assertThat(rules.isAllowed("10.1.1.1"), is(false));
    assertThat(rules.isAllowed("10.1.1.0"), is(true));
    assertThat(rules.isAllowed("10.1.1.2"), is(true));
  }

  @Test
  void allowAndDenySingleIps() {
    NetworkAddressRules rules =
        NetworkAddressRules.builder().deny("10.1.1.1").allow("10.1.1.3").build();

    assertThat(rules.isAllowed("10.1.1.0"), is(false));
    assertThat(rules.isAllowed("10.1.1.1"), is(false));
    assertThat(rules.isAllowed("10.1.1.2"), is(false));
    assertThat(rules.isAllowed("10.1.1.3"), is(true));
    assertThat(rules.isAllowed("10.1.1.4"), is(false));
  }

  @Test
  void allowRangeAndDenySingleIp() {
    NetworkAddressRules rules =
        NetworkAddressRules.builder().allow("10.1.1.1-10.1.1.3").deny("10.1.1.2").build();

    assertThat(rules.isAllowed("10.1.1.0"), is(false));
    assertThat(rules.isAllowed("10.1.1.1"), is(true));
    assertThat(rules.isAllowed("10.1.1.2"), is(false));
    assertThat(rules.isAllowed("10.1.1.3"), is(true));
    assertThat(rules.isAllowed("10.1.1.4"), is(false));
  }

  @Test
  void denyRangeAndAllowSingleIp() {
    NetworkAddressRules rules =
        NetworkAddressRules.builder().deny("10.1.1.1-10.1.1.3").allow("10.1.1.2").build();

    assertThat(rules.isAllowed("10.1.1.0"), is(false));
    assertThat(rules.isAllowed("10.1.1.1"), is(false));
    assertThat(rules.isAllowed("10.1.1.2"), is(false));
    assertThat(rules.isAllowed("10.1.1.3"), is(false));
    assertThat(rules.isAllowed("10.1.1.4"), is(false));
  }

  private static InetAddress mockInetAddressForHostName(String hostName) {
    InetAddress inetAddress = Mockito.mock(InetAddress.class);
    Mockito.when(inetAddress.getHostName()).thenReturn(hostName);

    return inetAddress;
  }
}
