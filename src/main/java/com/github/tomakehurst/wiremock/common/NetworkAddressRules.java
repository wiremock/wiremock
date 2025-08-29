/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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
import static java.util.Collections.emptySet;

import java.util.HashSet;
import java.util.Set;

/** The interface Network address rules. */
public interface NetworkAddressRules {
  /** The constant ALLOW_ALL. */
  NetworkAddressRules ALLOW_ALL = new DefaultNetworkAddressRules(ALL_RANGES, emptySet());

  /**
   * Builder builder.
   *
   * @return the builder
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * Is allowed boolean.
   *
   * @param testValue the test value
   * @return the boolean
   */
  boolean isAllowed(String testValue);

  /**
   * Is allowed all boolean.
   *
   * @return the boolean
   */
  boolean isAllowedAll();

  /** The type Builder. */
  public static class Builder {
    private final Set<NetworkAddressRange> allowed = new HashSet<>();
    private final Set<NetworkAddressRange> denied = new HashSet<>();

    /**
     * Allow builder.
     *
     * @param expression the expression
     * @return the builder
     */
    public Builder allow(String expression) {
      allowed.add(NetworkAddressRange.of(expression));
      return this;
    }

    /**
     * Deny builder.
     *
     * @param expression the expression
     * @return the builder
     */
    public Builder deny(String expression) {
      denied.add(NetworkAddressRange.of(expression));
      return this;
    }

    /**
     * Build network address rules.
     *
     * @return the network address rules
     */
    public NetworkAddressRules build() {
      Set<NetworkAddressRange> allowedRanges = allowed;
      if (allowedRanges.isEmpty()) {
        allowedRanges = ALL_RANGES;
      }
      return new DefaultNetworkAddressRules(Set.copyOf(allowedRanges), Set.copyOf(denied));
    }
  }
}
