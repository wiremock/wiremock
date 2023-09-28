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
import static com.github.tomakehurst.wiremock.common.NetworkAddressRules.NetworkAddressRulesResult.*;
import static com.github.tomakehurst.wiremock.common.NetworkAddressUtils.isValidInet4Address;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Set;

public class NetworkAddressRules {

  public static Builder builder() {
    return new Builder();
  }

  private final Set<NetworkAddressRange> allowedIps;
  private final Set<NetworkAddressRange> allowedHostPatterns;
  private final Set<NetworkAddressRange> deniedIps;
  private final Set<NetworkAddressRange> deniedHostPatterns;

  private final boolean allowedHostRulesExist;
  private final boolean hostRulesExist;

  private final boolean allowedIpRulesExist;
  private final boolean deniedIpRulesExist;

  private final boolean allowedRulesExist;

  public enum NetworkAddressRulesResult {
    ALLOW,
    NEUTRAL,
    DENY
  }

  private static final Set<NetworkAddressRange> ALL_SET = Set.of(ALL);

  public static final NetworkAddressRules ALLOW_ALL = new NetworkAddressRules(ALL_SET, emptySet());

  public NetworkAddressRules(Set<NetworkAddressRange> allowed, Set<NetworkAddressRange> denied) {
    this.allowedIps =
        allowed.stream()
            .filter(
                networkAddressRange ->
                    !(networkAddressRange instanceof NetworkAddressRange.DomainNameWildcard))
            .collect(toSet());
    this.allowedHostPatterns =
        allowed.stream()
            .filter(
                networkAddressRange ->
                    (networkAddressRange instanceof NetworkAddressRange.DomainNameWildcard))
            .collect(toSet());
    this.deniedIps =
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
            .collect(toSet());

    allowedHostRulesExist = !allowedHostPatterns.isEmpty();
    boolean deniedHostRulesExist = !deniedHostPatterns.isEmpty();
    hostRulesExist = allowedHostRulesExist || deniedHostRulesExist;

    allowedIpRulesExist = !allowedIps.isEmpty();
    deniedIpRulesExist = !deniedIps.isEmpty();

    allowedRulesExist = allowedHostRulesExist || allowedIpRulesExist;
  }

  private static Set<NetworkAddressRange> allIfEmpty(Set<NetworkAddressRange> original) {
    if (original.isEmpty()) {
      return ALL_SET;
    } else {
      return original;
    }
  }

  public boolean isAllowed(String testValue) {
    if (isValidInet4Address(testValue)) {
      return allIfEmpty(allowedIps).stream().anyMatch(rule -> rule.isIncluded(testValue))
          && deniedIps.stream().noneMatch(rule -> rule.isIncluded(testValue));
    } else {
      return hostPatternAllowed(testValue)
          && deniedHostPatterns.stream().noneMatch(rule -> rule.isIncluded(testValue));
    }
  }

  public NetworkAddressRulesResult isAllowedHostName(String testValue) {
    if (!hostRulesExist) {
      return NEUTRAL;
    } else if (isValidInet4Address(testValue)) {
      return NEUTRAL;
    } else if (hostPatternAllowed(testValue)
        && deniedHostPatterns.stream().noneMatch(rule -> rule.isIncluded(testValue))) {
      return ALLOW;
    } else {
      return DENY;
    }
  }

  private boolean hostPatternAllowed(String testValue) {
    return allIfEmpty(allowedHostPatterns).stream().anyMatch(rule -> rule.isIncluded(testValue));
  }

  public boolean isAllowedIpAddress(String testValue) {
    return ipAllowed(testValue) && deniedIps.stream().noneMatch(rule -> rule.isIncluded(testValue));
  }

  private boolean ipAllowed(String testValue) {
    if (!allowedRulesExist) {
      return true;
    } else if (allowedHostRulesExist && deniedIpRulesExist && !allowedIpRulesExist) {
      return true;
    } else {
      return allowedIps.stream().anyMatch(rule -> rule.isIncluded(testValue));
    }
  }

  public static class Builder {
    private final Set<NetworkAddressRange> allowed = new HashSet<>();
    private final Set<NetworkAddressRange> denied = new HashSet<>();

    public Builder allow(String expression) {
      allowed.add(NetworkAddressRange.of(expression));
      return this;
    }

    public Builder deny(String expression) {
      denied.add(NetworkAddressRange.of(expression));
      return this;
    }

    public NetworkAddressRules build() {
      Set<NetworkAddressRange> allowedRanges = allowed;
      if (allowedRanges.isEmpty()) {
        allowedRanges = ALL_SET;
      }
      return new NetworkAddressRules(Set.copyOf(allowedRanges), Set.copyOf(denied));
    }
  }
}
