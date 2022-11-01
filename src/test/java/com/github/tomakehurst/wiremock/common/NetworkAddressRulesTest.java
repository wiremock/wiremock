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

import static com.github.tomakehurst.wiremock.common.NetworkAddressRange.ALL;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class NetworkAddressRulesTest {

  public static Collection<Object[]> data() {
    return asList(
        new Object[][] {
          // Just allows only allows specified
          {ranges("10.1.1.1"), ranges(), "10.1.1.0", false},
          {ranges("10.1.1.1"), ranges(), "10.1.1.1", true},
          {ranges("10.1.1.1"), ranges(), "10.1.1.2", false},

          // Just denies allows all but specified denied
          {ranges(), ranges("10.1.1.1"), "10.1.1.0", true},
          {ranges(), ranges("10.1.1.1"), "10.1.1.1", false},
          {ranges(), ranges("10.1.1.1"), "10.1.1.2", true},

          // Allows and non-overlapping denies only allows specified
          {ranges("10.1.1.1"), ranges("10.1.1.3"), "10.1.1.0", false},
          {ranges("10.1.1.1"), ranges("10.1.1.3"), "10.1.1.1", true},
          {ranges("10.1.1.1"), ranges("10.1.1.3"), "10.1.1.2", false},
          {ranges("10.1.1.1"), ranges("10.1.1.3"), "10.1.1.3", false},
          {ranges("10.1.1.1"), ranges("10.1.1.3"), "10.1.1.4", false},

          // Allows with nested denies only allows specified apart from denied
          {ranges("10.1.1.1-10.1.1.3"), ranges("10.1.1.2"), "10.1.1.0", false},
          {ranges("10.1.1.1-10.1.1.3"), ranges("10.1.1.2"), "10.1.1.1", true},
          {ranges("10.1.1.1-10.1.1.3"), ranges("10.1.1.2"), "10.1.1.2", false},
          {ranges("10.1.1.1-10.1.1.3"), ranges("10.1.1.2"), "10.1.1.3", true},
          {ranges("10.1.1.1-10.1.1.3"), ranges("10.1.1.2"), "10.1.1.4", false},

          // Denies with nested allows does not allow anything
          {ranges("10.1.1.2"), ranges("10.1.1.1-10.1.1.3"), "10.1.1.0", false},
          {ranges("10.1.1.2"), ranges("10.1.1.1-10.1.1.3"), "10.1.1.1", false},
          {ranges("10.1.1.2"), ranges("10.1.1.1-10.1.1.3"), "10.1.1.2", false},
          {ranges("10.1.1.2"), ranges("10.1.1.1-10.1.1.3"), "10.1.1.3", false},
          {ranges("10.1.1.2"), ranges("10.1.1.1-10.1.1.3"), "10.1.1.4", false},

          // Overlapping allows and denies only allows specified when not in denied
          {ranges("10.1.1.1-10.1.1.2"), ranges("10.1.1.2-10.1.1.3"), "10.1.1.0", false},
          {ranges("10.1.1.1-10.1.1.2"), ranges("10.1.1.2-10.1.1.3"), "10.1.1.1", true},
          {ranges("10.1.1.1-10.1.1.2"), ranges("10.1.1.2-10.1.1.3"), "10.1.1.2", false},
          {ranges("10.1.1.1-10.1.1.2"), ranges("10.1.1.2-10.1.1.3"), "10.1.1.3", false},
          {ranges("10.1.1.1-10.1.1.2"), ranges("10.1.1.2-10.1.1.3"), "10.1.1.4", false},

          // Mutually exclusive does not allow
          {ranges("10.1.1.1"), ranges("10.1.1.1"), "10.1.1.1", false},
        });
  }

  @MethodSource("data")
  @ParameterizedTest(name = "{index}: allowed={0}, denied={1}, input={2} expected={3}")
  void allowsAddressIncludedAndNotExcluded(
      Set<NetworkAddressRange> allowed,
      Set<NetworkAddressRange> denied,
      String input,
      boolean expected) {
    NetworkAddressRules rules = new NetworkAddressRules(allowed, denied);

    assertThat(rules.isAllowed(input), is(expected));
  }

  @Test
  void builderDefaultsToAll() {
    assertThat(NetworkAddressRules.builder().build(), is(NetworkAddressRules.ALLOW_ALL));
  }

  @Test
  void builderDefaultsAllowsToAll() {
    assertThat(
        NetworkAddressRules.builder().deny("10.1.1.1").build(),
        is(
            new NetworkAddressRules(
                ImmutableSet.of(ALL), ImmutableSet.of(NetworkAddressRange.of("10.1.1.1")))));
  }

  @Test
  void builderDefaultsDeniesToEmpty() {
    assertThat(
        NetworkAddressRules.builder().allow("10.1.1.1").build(),
        is(
            new NetworkAddressRules(
                ImmutableSet.of(NetworkAddressRange.of("10.1.1.1")), ImmutableSet.of())));
  }

  private static Set<NetworkAddressRange> ranges(String... networkAddressRanges) {
    return networkAddressRanges == null
        ? null
        : Arrays.stream(networkAddressRanges)
            .map(NetworkAddressRange::of)
            .collect(Collectors.toSet());
  }
}
