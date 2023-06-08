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

import org.junit.jupiter.api.Test;

class NetworkAddressRangeTest {

  @Test
  void singleIpAddress() {
    NetworkAddressRange exclusion = NetworkAddressRange.of("10.1.2.3");

    assertThat(exclusion.isIncluded("10.1.2.3"), is(true));
    assertThat(exclusion.isIncluded("10.3.2.1"), is(false));
  }

  @Test
  void ipAddressRange() {
    NetworkAddressRange exclusion = NetworkAddressRange.of("10.1.1.1-10.1.2.2");

    assertThat(exclusion.isIncluded("10.1.1.1"), is(true));
    assertThat(exclusion.isIncluded("10.1.2.2"), is(true));
    assertThat(exclusion.isIncluded("10.1.1.254"), is(true));
    assertThat(exclusion.isIncluded("10.1.2.1"), is(true));

    assertThat(exclusion.isIncluded("10.3.2.1"), is(false));
    assertThat(exclusion.isIncluded("10.1.1.0"), is(false));
    assertThat(exclusion.isIncluded("10.1.2.3"), is(false));
  }

  @Test
  void exactDomainName() {
    NetworkAddressRange exclusion = NetworkAddressRange.of("my.stuff.wiremock.org");

    assertThat(exclusion.isIncluded("my.stuff.wiremock.org"), is(true));
    assertThat(exclusion.isIncluded("notmy.stuff.wiremock.org"), is(false));
  }

  @Test
  void domainNameWithWholeNameWildcard() {
    NetworkAddressRange exclusion = NetworkAddressRange.of("*.stuff.wiremock.org");

    assertThat(exclusion.isIncluded("my.stuff.wiremock.org"), is(true));
    assertThat(exclusion.isIncluded("alsomy.stuff.wiremock.org"), is(true));
    assertThat(exclusion.isIncluded("notmy.things.wiremock.org"), is(false));
  }

  @Test
  void domainNameWithPartialNameWildcard() {
    NetworkAddressRange exclusion = NetworkAddressRange.of("my.*uff.wiremock.org");

    assertThat(exclusion.isIncluded("my.stuff.wiremock.org"), is(true));
    assertThat(exclusion.isIncluded("my.fluff.wiremock.org"), is(true));
    assertThat(exclusion.isIncluded("notmy.stuff.wiremock.org"), is(false));
  }

  @Test
  void ipAddressResolvedFromDomainName() {
    NetworkAddressRange exclusion = NetworkAddressRange.of("127.0.0.1");
    assertThat(exclusion.isIncluded("localhost"), is(true));
  }

  @Test
  void ipRangeResolvedFromDomainName() {
    NetworkAddressRange exclusion = NetworkAddressRange.of("127.0.0.1-127.0.0.255");
    assertThat(exclusion.isIncluded("localhost"), is(true));
  }
}
