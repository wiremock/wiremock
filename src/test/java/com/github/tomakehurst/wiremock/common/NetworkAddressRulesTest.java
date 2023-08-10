/*
 * Copyright (C) 2022-2023 Thomas Akehurst
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
import java.net.UnknownHostException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

  @ParameterizedTest
  @CsvSource({"10.1.1.1,false", "10.1.1.2,true"})
  void isHostAllowedReturnsExpectedValueForIpv4AddressWithIpv4DenyRule(
      String host, boolean expectation) throws UnknownHostException {
    FakeDns dns =
        new FakeDns()
            .register("1.example.com", InetAddress.getByName("10.1.1.1"))
            .register("2.example.com", InetAddress.getByName("10.1.1.2"));
    ;
    NetworkAddressRules rules = NetworkAddressRules.builder(dns).deny("10.1.1.1").build();

    assertThat(rules.isHostAllowed(host), is(expectation));
  }

  @ParameterizedTest
  @CsvSource({"1.example.com,false", "2.example.com,true", "3.example.com,false"})
  void isHostAllowedReturnsExpectedValueForHostnameWithIpv4DenyRule(
      String host, boolean expectation) throws UnknownHostException {
    FakeDns dns =
        new FakeDns()
            .register("1.example.com", InetAddress.getByName("10.1.1.1"))
            .register("2.example.com", InetAddress.getByName("10.1.1.2"));

    NetworkAddressRules rules = NetworkAddressRules.builder(dns).deny("10.1.1.1").build();

    assertThat(rules.isHostAllowed(host), is(expectation));
  }

  @ParameterizedTest
  @CsvSource({"1.example.com,false", "2.example.com,true", "3.example.com,false"})
  void isHostAllowedReturnsExpectedValueForHostnameResolvingToMultipleAddressesWithIpv4DenyRule(
      String host, boolean expectation) throws UnknownHostException {
    FakeDns dns =
        new FakeDns()
            .register(
                "1.example.com",
                InetAddress.getByName("10.1.1.0"),
                InetAddress.getByName("10.1.1.1"))
            .register(
                "2.example.com",
                InetAddress.getByName("10.1.1.2"),
                InetAddress.getByName("10.1.1.3"));

    NetworkAddressRules rules = NetworkAddressRules.builder(dns).deny("10.1.1.1").build();

    assertThat(rules.isHostAllowed(host), is(expectation));
  }

  @ParameterizedTest
  @CsvSource({"10.1.1.1,true", "10.1.1.2,false", "3.example.com,false"})
  void isHostAllowedReturnsExpectedValueForIpv4AddressWithIpv4AllowRule(
      String host, boolean expectation) throws UnknownHostException {
    FakeDns dns =
        new FakeDns()
            .register("1.example.com", InetAddress.getByName("10.1.1.1"))
            .register("2.example.com", InetAddress.getByName("10.1.1.2"));
    ;
    NetworkAddressRules rules = NetworkAddressRules.builder(dns).allow("10.1.1.1").build();

    assertThat(rules.isHostAllowed(host), is(expectation));
  }

  @ParameterizedTest
  @CsvSource({"1.example.com,true", "2.example.com,false", "3.example.com,false"})
  void isHostAllowedReturnsExpectedValueForHostnameWithIpv4AllowRule(
      String host, boolean expectation) throws UnknownHostException {
    FakeDns dns =
        new FakeDns()
            .register("1.example.com", InetAddress.getByName("10.1.1.1"))
            .register("2.example.com", InetAddress.getByName("10.1.1.2"));

    NetworkAddressRules rules = NetworkAddressRules.builder(dns).allow("10.1.1.1").build();

    assertThat(rules.isHostAllowed(host), is(expectation));
  }

  @ParameterizedTest
  @CsvSource({"1.example.com,false", "2.example.com,false", "3.example.com,false"})
  void isHostAllowedReturnsExpectedValueForHostnameResolvingToMultipleAddressesWithIpv4AllowRule(
      String host, boolean expectation) throws UnknownHostException {
    FakeDns dns =
        new FakeDns()
            .register(
                "1.example.com",
                InetAddress.getByName("10.1.1.0"),
                InetAddress.getByName("10.1.1.1"))
            .register(
                "2.example.com",
                InetAddress.getByName("10.1.1.2"),
                InetAddress.getByName("10.1.1.3"));

    NetworkAddressRules rules = NetworkAddressRules.builder(dns).allow("10.1.1.1").build();

    assertThat(rules.isHostAllowed(host), is(expectation));
  }

  @ParameterizedTest
  @CsvSource({"10.1.1.1,true", "10.1.1.2,true"})
  void isHostAllowedReturnsExpectedValueForIpv4AddressWithHostnameDenyRule(
      String host, boolean expectation) throws UnknownHostException {
    FakeDns dns =
        new FakeDns()
            .register("1.example.com", InetAddress.getByName("10.1.1.1"))
            .register("2.example.com", InetAddress.getByName("10.1.1.2"));
    NetworkAddressRules rules = NetworkAddressRules.builder(dns).deny("1.example.com").build();

    assertThat(rules.isHostAllowed(host), is(expectation));
  }

  @ParameterizedTest
  @CsvSource({"1.example.com,false", "2.example.com,true", "3.example.com,false"})
  void isHostAllowedReturnsExpectedValueForHostnameWithHostnameDenyRule(
      String host, boolean expectation) throws UnknownHostException {
    FakeDns dns =
        new FakeDns()
            .register("1.example.com", InetAddress.getByName("10.1.1.1"))
            .register("2.example.com", InetAddress.getByName("10.1.1.2"));

    NetworkAddressRules rules = NetworkAddressRules.builder(dns).deny("1.example.com").build();

    assertThat(rules.isHostAllowed(host), is(expectation));
  }

  @ParameterizedTest
  @CsvSource({"1.example.com,false", "2.example.com,true", "3.example.com,false"})
  void isHostAllowedReturnsExpectedValueForHostnameResolvingToMultipleAddressesWithHostnameDenyRule(
      String host, boolean expectation) throws UnknownHostException {
    FakeDns dns =
        new FakeDns()
            .register(
                "1.example.com",
                InetAddress.getByName("10.1.1.0"),
                InetAddress.getByName("10.1.1.1"))
            .register(
                "2.example.com",
                InetAddress.getByName("10.1.1.2"),
                InetAddress.getByName("10.1.1.3"));

    NetworkAddressRules rules = NetworkAddressRules.builder(dns).deny("1.example.com").build();

    assertThat(rules.isHostAllowed(host), is(expectation));
  }

  @ParameterizedTest
  @CsvSource({"10.1.1.1,false", "10.1.1.2,false", "3.example.com,false"})
  void isHostAllowedReturnsExpectedValueForIpv4AddressWithHostnameAllowRule(
      String host, boolean expectation) throws UnknownHostException {
    FakeDns dns =
        new FakeDns()
            .register("1.example.com", InetAddress.getByName("10.1.1.1"))
            .register("2.example.com", InetAddress.getByName("10.1.1.2"));
    NetworkAddressRules rules = NetworkAddressRules.builder(dns).allow("1.example.com").build();

    assertThat(rules.isHostAllowed(host), is(expectation));
  }

  @ParameterizedTest
  @CsvSource({"1.example.com,true", "2.example.com,false", "3.example.com,false"})
  void isHostAllowedReturnsExpectedValueForHostnameWithHostnameAllowRule(
      String host, boolean expectation) throws UnknownHostException {
    FakeDns dns =
        new FakeDns()
            .register("1.example.com", InetAddress.getByName("10.1.1.1"))
            .register("2.example.com", InetAddress.getByName("10.1.1.2"));

    NetworkAddressRules rules = NetworkAddressRules.builder(dns).allow("1.example.com").build();

    assertThat(rules.isHostAllowed(host), is(expectation));
  }

  @ParameterizedTest
  @CsvSource({"1.example.com,true", "2.example.com,false", "3.example.com,false"})
  void
      isHostAllowedReturnsExpectedValueForHostnameResolvingToMultipleAddressesWithHostnameAllowRule(
          String host, boolean expectation) throws UnknownHostException {
    FakeDns dns =
        new FakeDns()
            .register(
                "1.example.com",
                InetAddress.getByName("10.1.1.0"),
                InetAddress.getByName("10.1.1.1"))
            .register(
                "2.example.com",
                InetAddress.getByName("10.1.1.2"),
                InetAddress.getByName("10.1.1.3"));

    NetworkAddressRules rules = NetworkAddressRules.builder(dns).allow("1.example.com").build();

    assertThat(rules.isHostAllowed(host), is(expectation));
  }
}
