/*
 * Copyright (C) 2023-2024 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.ProhibitedNetworkAddressException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;

public class NetworkAddressRulesAdheringDnsResolver implements DnsResolver {

  private final DnsResolver delegate;
  private final NetworkAddressRules networkAddressRules;

  public NetworkAddressRulesAdheringDnsResolver(NetworkAddressRules networkAddressRules) {
    this(SystemDefaultDnsResolver.INSTANCE, networkAddressRules);
  }

  public NetworkAddressRulesAdheringDnsResolver(
      DnsResolver delegate, NetworkAddressRules networkAddressRules) {
    this.delegate = delegate;
    this.networkAddressRules = networkAddressRules;
  }

  @Override
  public InetAddress[] resolve(String host) throws UnknownHostException {
    if (!networkAddressRules.isAllowed(host)) {
      throw new ProhibitedNetworkAddressException();
    }

    final InetAddress[] resolved = delegate.resolve(host);

    if (networkAddressRules.isAllowedAll()) {
      return resolved;
    }

    final InetAddress[] resolvedIpv4 =
        Arrays.stream(resolved)
            .filter(inetAddress -> inetAddress instanceof Inet4Address)
            .toArray(InetAddress[]::new);

    if (Stream.of(resolvedIpv4)
        .anyMatch(address -> !networkAddressRules.isAllowed(address.getHostAddress()))) {
      throw new ProhibitedNetworkAddressException();
    }

    return resolvedIpv4;
  }

  @Override
  public String resolveCanonicalHostname(String host) throws UnknownHostException {
    return delegate.resolveCanonicalHostname(host);
  }
}
