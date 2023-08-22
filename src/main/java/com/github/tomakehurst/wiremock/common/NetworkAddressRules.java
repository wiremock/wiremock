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
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.InetAddresses;
import java.util.Set;

public class NetworkAddressRules {

  public static Builder builder() {
    return new Builder();
  }

  private final Set<NetworkAddressRange> allowed;
  private final Set<NetworkAddressRange> allowedHostPatterns;
  private final Set<NetworkAddressRange> denied;
  private final Set<NetworkAddressRange> deniedHostPatterns;

  public static NetworkAddressRules ALLOW_ALL =
      new NetworkAddressRules(ImmutableSet.of(ALL), emptySet());

  public NetworkAddressRules(Set<NetworkAddressRange> allowed, Set<NetworkAddressRange> denied) {
    this.allowed =
        defaultIfEmpty(
            allowed.stream()
                .filter(
                    networkAddressRange ->
                        !(networkAddressRange instanceof NetworkAddressRange.DomainNameWildcard))
                .collect(toSet()),
            ImmutableSet.of(ALL));
    this.allowedHostPatterns =
        defaultIfEmpty(
            allowed.stream()
                .filter(
                    networkAddressRange ->
                        (networkAddressRange instanceof NetworkAddressRange.DomainNameWildcard))
                .collect(toSet()),
            ImmutableSet.of(ALL));
    this.denied =
        denied.stream()
            .filter(
                networkAddressRange ->
                    !(networkAddressRange instanceof NetworkAddressRange.DomainNameWildcard))
            .collect(toSet());
    this.deniedHostPatterns =
        denied.stream()
            .filter(
                networkAddressRange ->
                    (networkAddressRange instanceof NetworkAddressRange.DomainNameWildcard))
            .map(
                networkAddressRange -> (NetworkAddressRange.DomainNameWildcard) networkAddressRange)
            .collect(toSet());
  }

  private static <T> Set<T> defaultIfEmpty(Set<T> original, Set<T> ifEmpty) {
    if (original.isEmpty()) {
      return ifEmpty;
    } else {
      return original;
    }
  }

  public boolean isAllowed(String testValue) {
    if (InetAddresses.isInetAddress(testValue)) {
      return allowed.stream().anyMatch(rule -> rule.isIncluded(testValue))
          && denied.stream().noneMatch(rule -> rule.isIncluded(testValue));
    } else {
      return allowedHostPatterns.stream().anyMatch(rule -> rule.isIncluded(testValue))
          && deniedHostPatterns.stream().noneMatch(rule -> rule.isIncluded(testValue));
    }
  }

  public static class Builder {
    private final ImmutableSet.Builder<NetworkAddressRange> allowed = ImmutableSet.builder();
    private final ImmutableSet.Builder<NetworkAddressRange> denied = ImmutableSet.builder();

    public Builder allow(String expression) {
      allowed.add(NetworkAddressRange.of(expression));
      return this;
    }

    public Builder deny(String expression) {
      denied.add(NetworkAddressRange.of(expression));
      return this;
    }

    public NetworkAddressRules build() {
      Set<NetworkAddressRange> allowedRanges = allowed.build();
      if (allowedRanges.isEmpty()) {
        allowedRanges = ImmutableSet.of(ALL);
      }
      return new NetworkAddressRules(allowedRanges, denied.build());
    }
  }
}
