/*
 * Copyright (C) 2022-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.NetworkAddressRange.ALL_RANGES;
import static com.github.tomakehurst.wiremock.common.NetworkAddressUtils.isValidInet4Address;
import static java.util.stream.Collectors.toSet;

import java.util.Set;

public class DefaultNetworkAddressRules implements NetworkAddressRules {

  private final Set<NetworkAddressRange> allowed;
  private final Set<NetworkAddressRange> allowedHostPatterns;
  private final Set<NetworkAddressRange> denied;
  private final Set<NetworkAddressRange> deniedHostPatterns;

  public DefaultNetworkAddressRules(
      Set<NetworkAddressRange> allowed, Set<NetworkAddressRange> denied) {
    this.allowed =
        defaultIfEmpty(
            allowed.stream()
                .filter(
                    networkAddressRange ->
                        !(networkAddressRange instanceof NetworkAddressRange.DomainNameWildcard))
                .collect(toSet()),
            ALL_RANGES);
    this.allowedHostPatterns =
        defaultIfEmpty(
            allowed.stream()
                .filter(
                    networkAddressRange ->
                        (networkAddressRange instanceof NetworkAddressRange.DomainNameWildcard))
                .collect(toSet()),
            ALL_RANGES);
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

  @Override
  public boolean isAllowed(String testValue) {
    if (isValidInet4Address(testValue)) {
      return allowed.stream().anyMatch(rule -> rule.isIncluded(testValue))
          && denied.stream().noneMatch(rule -> rule.isIncluded(testValue));
    } else {
      return allowedHostPatterns.stream().anyMatch(rule -> rule.isIncluded(testValue))
          && deniedHostPatterns.stream().noneMatch(rule -> rule.isIncluded(testValue));
    }
  }

  @Override
  public boolean isAllowedAll() {
    return allowed.equals(ALL_RANGES)
        && allowedHostPatterns.equals(ALL_RANGES)
        && denied.isEmpty()
        && deniedHostPatterns.isEmpty();
  }
}
