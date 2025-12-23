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
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.wiremock.url.whatwg.SuccessWhatWGUrlTestCase;
import org.wiremock.url.whatwg.WhatWGUrlTestCase;
import org.wiremock.url.whatwg.WhatWGUrlTestManagement;

public class UrlReferenceTests {

  @SuppressWarnings("unused")
  private static final List<? extends WhatWGUrlTestCase> wiremock_valid =
      WhatWGUrlTestManagement.wiremock_valid;

  @ParameterizedTest
  @FieldSource("wiremock_valid")
  void wiremock_valid(WhatWGUrlTestCase testCase) {
    System.out.println(testCase);
    var input = testCase.input();
    var urlReference = UrlReference.parse(input);
    assertThat(urlReference.toString()).isEqualTo(input);

    UrlReference normalised = urlReference.normalise();
    assertThat(normalised.normalise()).isSameAs(normalised);

    var normalSerialised = normalised.toString();
    if (!(urlReference instanceof PathAndQuery && normalSerialised.startsWith("//"))) {
      UrlReference reconstituted = UrlReference.parse(normalSerialised);
      assertThat(reconstituted).isEqualTo(normalised);
    }

    if (testCase instanceof SuccessWhatWGUrlTestCase successTestCase) {
      UrlReference resolved;
      if (successTestCase.base() != null) {
        resolved = resolve(urlReference, successTestCase.base());
      } else {
        resolved = normalised;
      }

      if (resolved != null) {

        assertThat(Optional.ofNullable(resolved.scheme()).map(scheme -> scheme + ":").orElse(""))
            .isEqualTo(successTestCase.protocol());

        Optional<Authority> authority = Optional.ofNullable(resolved.authority());
        Optional<UserInfo> userInfo = authority.flatMap(a -> Optional.ofNullable(a.userInfo()));
        Optional<Username> username = userInfo.map(UserInfo::username);
        Optional<Password> password = userInfo.flatMap(a -> Optional.ofNullable(a.password()));

        assertThat(username.map(Object::toString).orElse("")).isEqualTo(successTestCase.username());
        assertThat(password.map(Object::toString).orElse("")).isEqualTo(successTestCase.password());

        assertThat(Optional.ofNullable(resolved.port()).map(Object::toString).orElse(""))
            .isEqualTo(successTestCase.port());

        if (!successTestCase.pathname().isEmpty()
            && !successTestCase.pathname().matches(".*/[a-zA-Z]:(/.*|$)") // windows style paths
            && !successTestCase
                .pathname()
                .matches(
                    ".*%[a-fA-F0-9]?(?:[^a-fA-F0-9].*|$)") // % not as part of a percent encoding
            && !(urlReference.authority() != null && urlReference.authority().toString().isEmpty())
            && !resolved.path().toString().contains("\\")
            && !urlReference.path().toString().contains("\t")) {
          assertThat(resolved.path().toString()).isEqualTo(successTestCase.pathname());
        }

        if (!successTestCase.search().contains("{")
            && !successTestCase
                .search()
                .matches(
                    ".*%[a-fA-F0-9]?(?:[^a-fA-F0-9].*|$)") // % not as part of a percent encoding
        ) {
          assertThat(
                  Optional.ofNullable(resolved.query())
                      .map(o -> o.isEmpty() ? "" : "?" + o)
                      .orElse(""))
              .isEqualTo(successTestCase.search());
        }
        if (!input.endsWith(" ")
            && !successTestCase.hash().contains("{")
            && !successTestCase.hash().contains("#")) {
          assertThat(
                  Optional.ofNullable(resolved.fragment())
                      .map(f -> f.isEmpty() ? "" : "#" + f)
                      .orElse(""))
              .isEqualTo(successTestCase.hash());
        }

        if (Optional.ofNullable(resolved.host())
                .map(Object::toString)
                .orElse("")
                .equals(successTestCase.hostname())
            && !successTestCase.pathname().isEmpty()
            && !successTestCase.pathname().matches(".*/[a-zA-Z]:(/.*|$)")
            && !successTestCase
                .pathname()
                .matches(
                    ".*%[a-fA-F0-9]?(?:[^a-fA-F0-9].*|$)") // % not as part of a percent encoding
            && !input.endsWith(" ")
            && !resolved.path().toString().contains("\\")
            && !successTestCase.pathname().contains("|")
            && !urlReference.path().toString().contains("\t")
            && !successTestCase.search().contains("{")
            && !successTestCase
                .search()
                .matches(
                    ".*%[a-fA-F0-9]?(?:[^a-fA-F0-9].*|$)") // % not as part of a percent encoding
            && !successTestCase.hash().contains("{")
            && !successTestCase.hash().contains("#")) {
          assertThat(resolved.toString()).isEqualTo(successTestCase.href());
          assertThat(authority.map(Authority::hostAndPort).map(Object::toString).orElse(""))
              .isEqualTo(successTestCase.host());
        }
      }
    }
  }

  private static @Nullable Url resolve(UrlReference urlReference, String baseString) {
    try {
      return Url.parse(baseString).resolve(urlReference);
    } catch (IllegalUrl ignored) {
      // probably a URN we do not yet handle
      return null;
    }
  }

  @SuppressWarnings("unused")
  private static final List<? extends WhatWGUrlTestCase> wiremock_invalid =
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
    wiremock_valid(
        new SuccessWhatWGUrlTestCase(
            /* input */ "foo://1234567890abcdefghijlmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ-._~!$&'()*+,;=:@host/",
            /* base */ null,
            /* href */ "foo://1234567890abcdefghijlmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ-._~!$&'()*+,;=@host/",
            /* origin */ "foo://host",
            /* protocol */ "foo:",
            /* username */ "1234567890abcdefghijlmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ-._~!$&'()*+,;=",
            /* password */ "",
            /* host */ "host",
            /* hostname */ "host",
            /* port */ "",
            /* pathname */ "/",
            /* search */ "",
            /* searchParams */ null,
            /* hash */ ""));
  }
}
