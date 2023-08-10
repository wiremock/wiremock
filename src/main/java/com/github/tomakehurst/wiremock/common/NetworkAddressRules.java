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

import static com.github.tomakehurst.wiremock.common.NetworkAddressRange.ALL;
import static java.util.Collections.emptySet;

import com.google.common.collect.ImmutableSet;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;

public class NetworkAddressRules {

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Dns dns) {
    return new Builder(dns);
  }

  private final Set<NetworkAddressRange> allowed;
  private final Set<NetworkAddressRange> denied;
  private final Dns dns;

  public static NetworkAddressRules ALLOW_ALL = new NetworkAddressRules(Set.of(ALL), emptySet());

  public NetworkAddressRules(Set<NetworkAddressRange> allowed, Set<NetworkAddressRange> denied) {
    this(allowed, denied, SystemDns.INSTANCE);
  }

  NetworkAddressRules(Set<NetworkAddressRange> allowed, Set<NetworkAddressRange> denied, Dns dns) {
    this.allowed = allowed;
    this.denied = denied;
    this.dns = dns;
  }

  public boolean isAllowed(String testValue) {
    return allowed.stream().anyMatch(rule -> rule.isIncluded(testValue))
        && denied.stream().noneMatch(rule -> rule.isIncluded(testValue));
  }

  public boolean isHostProhibited(String host) {
    try {
      final InetAddress[] resolvedAddresses = dns.getAllByName(host);
      return !Arrays.stream(resolvedAddresses)
          .allMatch(address -> isAllowed(address.getHostAddress()));
    } catch (UnknownHostException e) {
      return true;
    }
  }

  public static class Builder {
    private final ImmutableSet.Builder<NetworkAddressRange> allowed = ImmutableSet.builder();
    private final ImmutableSet.Builder<NetworkAddressRange> denied = ImmutableSet.builder();
    private final Dns dns;

    public Builder() {
      this(SystemDns.INSTANCE);
    }

    public Builder(Dns dns) {
      this.dns = dns;
    }

    public Builder allow(String expression) {
      allowed.add(NetworkAddressRange.of(expression, dns));
      return this;
    }

    public Builder deny(String expression) {
      denied.add(NetworkAddressRange.of(expression, dns));
      return this;
    }

    public NetworkAddressRules build() {
      Set<NetworkAddressRange> allowedRanges = allowed.build();
      if (allowedRanges.isEmpty()) {
        allowedRanges = Set.of(ALL);
      }
      return new NetworkAddressRules(allowedRanges, denied.build(), dns);
    }
  }
}
