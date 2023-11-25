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
package com.github.tomakehurst.wiremock.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.common.DefaultNetworkAddressRules;
import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Stream;
import org.apache.hc.client5.http.impl.InMemoryDnsResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class NetworkAddressRulesAdheringDnsResolverTest {

  InMemoryDnsResolver dns = new InMemoryDnsResolver();

  @ParameterizedTest
  @ValueSource(strings = {"10.1.1.2", "2.example.com"})
  void resolveReturnsWithUnmatchedIpv4DenyRule(String host) throws UnknownHostException {
    register("1.example.com", "10.1.1.1");
    register("2.example.com", "10.1.1.2");

    NetworkAddressRules rules = DefaultNetworkAddressRules.builder().deny("10.1.1.1").build();

    NetworkAddressRulesAdheringDnsResolver resolver =
        new NetworkAddressRulesAdheringDnsResolver(dns, rules);

    assertThat(resolver.resolve(host)).isEqualTo(dns.resolve(host));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "10.1.1.1",
        "1.example.com",
        "3.example.com",
      })
  void resolveThrowsExceptionWithMatchedIpv4DenyRule(String host) throws UnknownHostException {
    register("1.example.com", "10.1.1.1");
    register("2.example.com", "10.1.1.2");

    NetworkAddressRules rules = DefaultNetworkAddressRules.builder().deny("10.1.1.1").build();

    NetworkAddressRulesAdheringDnsResolver resolver =
        new NetworkAddressRulesAdheringDnsResolver(dns, rules);

    assertThatThrownBy(() -> resolver.resolve(host));
  }

  @ParameterizedTest
  @ValueSource(strings = {"10.1.1.2", "10.1.1.3", "2.example.com"})
  void resolveReturnsForHostnameResolvingToMultipleAddressesWithUnmatchedIpv4DenyRule(String host)
      throws UnknownHostException {
    register("1.example.com", "10.1.1.0", "10.1.1.1");
    register("2.example.com", "10.1.1.2", "10.1.1.3");

    NetworkAddressRules rules = DefaultNetworkAddressRules.builder().deny("10.1.1.1").build();

    NetworkAddressRulesAdheringDnsResolver resolver =
        new NetworkAddressRulesAdheringDnsResolver(dns, rules);

    assertThat(resolver.resolve(host)).isEqualTo(dns.resolve(host));
  }

  @ParameterizedTest
  @ValueSource(strings = {"10.1.1.1", "1.example.com"})
  void resolveThrowsExceptionForHostnameResolvingToMultipleAddressesWithMatchedIpv4DenyRule(
      String host) throws UnknownHostException {
    register("1.example.com", "10.1.1.0", "10.1.1.1");
    register("2.example.com", "10.1.1.2", "10.1.1.3");

    NetworkAddressRules rules = DefaultNetworkAddressRules.builder().deny("10.1.1.1").build();

    NetworkAddressRulesAdheringDnsResolver resolver =
        new NetworkAddressRulesAdheringDnsResolver(dns, rules);

    assertThatThrownBy(() -> resolver.resolve(host));
  }

  @ParameterizedTest
  @ValueSource(strings = {"10.1.1.1", "1.example.com"})
  void resolveReturnsWithUnmatchedIpv4AllowRule(String host) throws UnknownHostException {
    register("1.example.com", "10.1.1.1");
    register("2.example.com", "10.1.1.2");

    NetworkAddressRules rules = DefaultNetworkAddressRules.builder().allow("10.1.1.1").build();

    NetworkAddressRulesAdheringDnsResolver resolver =
        new NetworkAddressRulesAdheringDnsResolver(dns, rules);

    assertThat(resolver.resolve(host)).isEqualTo(dns.resolve(host));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "10.1.1.2",
        "2.example.com",
        "3.example.com",
      })
  void resolveThrowsExceptionWithMatchedIpv4AllowRule(String host) throws UnknownHostException {
    register("1.example.com", "10.1.1.1");
    register("2.example.com", "10.1.1.2");

    NetworkAddressRules rules = DefaultNetworkAddressRules.builder().allow("10.1.1.1").build();

    NetworkAddressRulesAdheringDnsResolver resolver =
        new NetworkAddressRulesAdheringDnsResolver(dns, rules);

    assertThatThrownBy(() -> resolver.resolve(host));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "10.1.1.1",
      })
  void resolveReturnsForHostnameResolvingToMultipleAddressesWithUnmatchedIpv4AllowRule(String host)
      throws UnknownHostException {
    register("1.example.com", "10.1.1.0", "10.1.1.1");
    register("2.example.com", "10.1.1.2", "10.1.1.3");

    NetworkAddressRules rules = DefaultNetworkAddressRules.builder().allow("10.1.1.1").build();

    NetworkAddressRulesAdheringDnsResolver resolver =
        new NetworkAddressRulesAdheringDnsResolver(dns, rules);

    assertThat(resolver.resolve(host)).isEqualTo(dns.resolve(host));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "10.1.1.0",
        "10.1.1.2",
        "10.1.1.3",
        "1.example.com",
        "2.example.com",
        "3.example.com",
      })
  void resolveThrowsExceptionForHostnameResolvingToMultipleAddressesWithMatchedIpv4AllowRule(
      String host) throws UnknownHostException {
    register("1.example.com", "10.1.1.0", "10.1.1.1");
    register("2.example.com", "10.1.1.2", "10.1.1.3");

    NetworkAddressRules rules = DefaultNetworkAddressRules.builder().allow("10.1.1.1").build();

    NetworkAddressRulesAdheringDnsResolver resolver =
        new NetworkAddressRulesAdheringDnsResolver(dns, rules);

    assertThatThrownBy(() -> resolver.resolve(host));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "10.1.1.1",
        "10.1.1.2",
        "2.example.com",
      })
  void resolveReturnsForIpv4AddressWithHostnameDenyRule(String host) throws UnknownHostException {
    register("1.example.com", "10.1.1.1");
    register("2.example.com", "10.1.1.2");

    NetworkAddressRules rules = DefaultNetworkAddressRules.builder().deny("1.example.com").build();

    NetworkAddressRulesAdheringDnsResolver resolver =
        new NetworkAddressRulesAdheringDnsResolver(dns, rules);

    assertThat(resolver.resolve(host)).isEqualTo(dns.resolve(host));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "1.example.com",
        "3.example.com",
      })
  void resolveThrowsExceptionForIpv4AddressWithHostnameDenyRule(String host)
      throws UnknownHostException {
    register("1.example.com", "10.1.1.1");
    register("2.example.com", "10.1.1.2");

    NetworkAddressRules rules = DefaultNetworkAddressRules.builder().deny("1.example.com").build();

    NetworkAddressRulesAdheringDnsResolver resolver =
        new NetworkAddressRulesAdheringDnsResolver(dns, rules);

    assertThatThrownBy(() -> resolver.resolve(host));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "10.1.1.1",
        "10.1.1.2",
        "1.example.com",
      })
  void resolveReturnsForIpv4AddressWithHostnameAllowRule(String host) throws UnknownHostException {
    register("1.example.com", "10.1.1.1");
    register("2.example.com", "10.1.1.2");

    NetworkAddressRules rules = DefaultNetworkAddressRules.builder().allow("1.example.com").build();

    NetworkAddressRulesAdheringDnsResolver resolver =
        new NetworkAddressRulesAdheringDnsResolver(dns, rules);

    assertThat(resolver.resolve(host)).isEqualTo(dns.resolve(host));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "2.example.com",
        "3.example.com",
      })
  void resolveThrowsExceptionForIpv4AddressWithHostnameAllowRule(String host)
      throws UnknownHostException {
    register("1.example.com", "10.1.1.1");
    register("2.example.com", "10.1.1.2");

    NetworkAddressRules rules = DefaultNetworkAddressRules.builder().allow("1.example.com").build();

    NetworkAddressRulesAdheringDnsResolver resolver =
        new NetworkAddressRulesAdheringDnsResolver(dns, rules);

    assertThatThrownBy(() -> resolver.resolve(host));
  }

  @Test
  void resolveIgnoresIpv6Addresses() throws UnknownHostException {
    register("1.example.com", "10.1.1.1", "2001:0db8:85a3:0000:0000:8a2e:0370:7334");

    NetworkAddressRules rules = DefaultNetworkAddressRules.builder().allow("10.1.1.1").build();

    NetworkAddressRulesAdheringDnsResolver resolver =
        new NetworkAddressRulesAdheringDnsResolver(dns, rules);

    assertThat(resolver.resolve("1.example.com")).isEqualTo(dns.resolve("10.1.1.1"));
  }

  private void register(String host, String... ipAddresses) throws UnknownHostException {
    dns.add(
        host,
        Stream.of(ipAddresses)
            .map(NetworkAddressRulesAdheringDnsResolverTest::toInetAddress)
            .toArray(InetAddress[]::new));
    for (String ipAddress : ipAddresses) {
      dns.add(ipAddress, InetAddress.getByName(ipAddress));
    }
  }

  private static InetAddress toInetAddress(String ipAddress) {
    try {
      return InetAddress.getByName(ipAddress);
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
}
