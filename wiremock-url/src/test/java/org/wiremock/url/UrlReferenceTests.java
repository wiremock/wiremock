/*
 * Copyright (C) 2025 Thomas Akehurst
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
package org.wiremock.url;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.wiremock.url.whatwg.SuccessWhatWGUrlTestCase;
import org.wiremock.url.whatwg.WhatWGUrlTestCase;
import org.wiremock.url.whatwg.WhatWGUrlTestManagement;

public class UrlReferenceTests {

  @SuppressWarnings("unused")
  private static final List<WhatWGUrlTestCase> wiremock_valid =
      WhatWGUrlTestManagement.wiremock_valid;

  @ParameterizedTest
  @FieldSource("wiremock_valid")
  void wiremock_valid(WhatWGUrlTestCase testCase) {
    testValid(testCase);
  }

  @SuppressWarnings("unused")
  private static final List<WhatWGUrlTestCase> wiremock_invalid =
      WhatWGUrlTestManagement.wiremock_invalid;

  @ParameterizedTest
  @FieldSource("wiremock_invalid")
  void wiremock_invalid(WhatWGUrlTestCase testCase) {
    System.out.println(testCase);
    assertThatExceptionOfType(IllegalUrlReference.class)
        .isThrownBy(() -> UrlReference.parse(testCase.input()));
  }

  // convenience way to test specific cases
  @Test
  void debug() {
    testValid(
        new SuccessWhatWGUrlTestCase(
            /* input */ "notspecial://host/?'",
            /* base */ null,
            /* href */ "notspecial://host/?'",
            /* origin */ "null",
            /* protocol */ "notspecial:",
            /* username */ "",
            /* password */ "",
            /* host */ "host",
            /* hostname */ "host",
            /* port */ "",
            /* pathname */ "/",
            /* search */ "?'",
            /* searchParams */ null,
            /* hash */ ""));
  }

  private static void testValid(WhatWGUrlTestCase testCase) {
    System.out.println(testCase);
    var input = testCase.input();
    var urlReference = UrlReference.parse(input);
    assertThat(urlReference.toString()).isEqualTo(input);

    UrlReference normalised = urlReference.normalise();
    UrlReference reconstituted = UrlReference.parse(normalised.toString());
    assertThat(reconstituted).isEqualTo(normalised);

    if (testCase instanceof SuccessWhatWGUrlTestCase successTestCase) {
      if (successTestCase.base() == null) {
        //        assertThat(normalised.toString()).isEqualTo(successTestCase.href());

        assertThat(Optional.ofNullable(normalised.scheme()).map(scheme -> scheme + ":").orElse(""))
            .isEqualTo(successTestCase.protocol());

        Optional<Authority> authority = Optional.ofNullable(normalised.authority());
        Optional<UserInfo> userInfo = authority.flatMap(a -> Optional.ofNullable(a.userInfo()));

        assertThat(userInfo.map(UserInfo::username).orElse(""))
            .isEqualTo(successTestCase.username());
        assertThat(userInfo.map(UserInfo::password).orElse(""))
            .isEqualTo(successTestCase.password());

        //
        // assertThat(Optional.ofNullable(normalised.host()).map(Object::toString).orElse(""))
        //            .isEqualTo(successTestCase.hostname());
        assertThat(Optional.ofNullable(normalised.port()).map(Object::toString).orElse(""))
            .isEqualTo(successTestCase.port());
        //        assertThat(authority.map(Authority::hostAndPort).map(Object::toString).orElse(""))
        //            .isEqualTo(successTestCase.host());
        //        assertThat(normalised.path().toString()).isEqualTo(successTestCase.pathname());
        //        final String expected;
        //        if (successTestCase.href().contains("?") && successTestCase.search().isEmpty()) {
        //          expected = "?";
        //        } else {
        //          expected = successTestCase.search();
        //        }
        assertThat(
                Optional.ofNullable(normalised.query())
                    .map(o -> o.isEmpty() ? "" : "?" + o)
                    .orElse(""))
            .isEqualTo(successTestCase.search());
        assertThat(
                Optional.ofNullable(normalised.fragment())
                    .map(f -> f.isEmpty() ? "" : "#" + f)
                    .orElse(""))
            .isEqualTo(successTestCase.hash());
      }
    }
  }
}
