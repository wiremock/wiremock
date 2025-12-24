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

public class UriReferenceTests {

  @SuppressWarnings("unused")
  private static final List<? extends WhatWGUrlTestCase> wiremock_valid =
      WhatWGUrlTestManagement.wiremock_valid;

  @ParameterizedTest
  @FieldSource("wiremock_valid")
  void wiremock_valid(WhatWGUrlTestCase testCase) {
    System.out.println(testCase);
    var input = testCase.input();
    var uriReference = UriReference.parse(input);
    assertThat(uriReference.toString()).isEqualTo(input);

    UriReference normalised = uriReference.normalise();
    assertThat(normalised.normalise()).isSameAs(normalised);

    var normalSerialised = normalised.toString();
    if (!(uriReference instanceof PathAndQuery && normalSerialised.startsWith("//"))) {
      UriReference reconstituted = UriReference.parse(normalSerialised);
      assertThat(reconstituted).isEqualTo(normalised);
    }

    if (testCase instanceof SuccessWhatWGUrlTestCase successTestCase) {
      UriReference resolved;
      if (successTestCase.base() != null) {
        resolved = resolve(uriReference, successTestCase.base());
      } else {
        resolved = normalised;
      }

      if (resolved != null) {

        assertThat(Optional.ofNullable(resolved.getScheme()).map(scheme -> scheme + ":").orElse(""))
            .isEqualTo(successTestCase.protocol());

        Optional<Authority> authority = Optional.ofNullable(resolved.getAuthority());
        Optional<UserInfo> userInfo = authority.flatMap(a -> Optional.ofNullable(a.getUserInfo()));
        Optional<Username> username = userInfo.map(UserInfo::getUsername);
        Optional<Password> password = userInfo.flatMap(a -> Optional.ofNullable(a.getPassword()));

        assertThat(username.map(Object::toString).orElse("")).isEqualTo(successTestCase.username());
        assertThat(password.map(Object::toString).orElse("")).isEqualTo(successTestCase.password());

        assertThat(Optional.ofNullable(resolved.getPort()).map(Object::toString).orElse(""))
            .isEqualTo(successTestCase.port());

        if (!successTestCase.pathname().isEmpty()
            && !successTestCase.pathname().matches(".*/[a-zA-Z]:(/.*|$)") // windows style paths
            && !successTestCase
                .pathname()
                .matches(
                    ".*%[a-fA-F0-9]?(?:[^a-fA-F0-9].*|$)") // % not as part of a percent encoding
            && !(uriReference.getAuthority() != null
                && uriReference.getAuthority().toString().isEmpty())
            && !resolved.getPath().toString().contains("\\")
            && !uriReference.getPath().toString().contains("\t")) {
          assertThat(resolved.getPath().toString()).isEqualTo(successTestCase.pathname());
        }

        if (!successTestCase.search().contains("{")
            && !successTestCase
                .search()
                .matches(
                    ".*%[a-fA-F0-9]?(?:[^a-fA-F0-9].*|$)") // % not as part of a percent encoding
        ) {
          assertThat(
                  Optional.ofNullable(resolved.getQuery())
                      .map(o -> o.isEmpty() ? "" : "?" + o)
                      .orElse(""))
              .isEqualTo(successTestCase.search());
        }
        if (!input.endsWith(" ")
            && !successTestCase.hash().contains("{")
            && !successTestCase.hash().contains("#")) {
          assertThat(
                  Optional.ofNullable(resolved.getFragment())
                      .map(f -> f.isEmpty() ? "" : "#" + f)
                      .orElse(""))
              .isEqualTo(successTestCase.hash());
        }

        if (Optional.ofNullable(resolved.getHost())
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
            && !resolved.getPath().toString().contains("\\")
            && !successTestCase.pathname().contains("|")
            && !uriReference.getPath().toString().contains("\t")
            && !successTestCase.search().contains("{")
            && !successTestCase
                .search()
                .matches(
                    ".*%[a-fA-F0-9]?(?:[^a-fA-F0-9].*|$)") // % not as part of a percent encoding
            && !successTestCase.hash().contains("{")
            && !successTestCase.hash().contains("#")) {
          assertThat(resolved.toString()).isEqualTo(successTestCase.href());
          assertThat(authority.map(Authority::getHostAndPort).map(Object::toString).orElse(""))
              .isEqualTo(successTestCase.host());
        }
      }
    }
  }

  private static @Nullable Uri resolve(UriReference urlReference, String baseString) {
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
    assertThatExceptionOfType(IllegalUriReference.class)
        .isThrownBy(() -> UriReference.parse(testCase.input()));
  }

  // convenience way to test specific cases
  @Test
  void debug() {
    wiremock_valid(
        new SuccessWhatWGUrlTestCase(
            /* input */ "test",
            /* base */ "file:///tmp/mock/path",
            /* href */ "file:///tmp/mock/test",
            /* origin */ null,
            /* protocol */ "file:",
            /* username */ "",
            /* password */ "",
            /* host */ "",
            /* hostname */ "",
            /* port */ "",
            /* pathname */ "/tmp/mock/test",
            /* search */ "",
            /* searchParams */ null,
            /* hash */ ""));
  }

  @Test
  void relativeRefParserThrowsCorrectExceptionType() {
    // When parsing a URL (not a relative reference), RelativeRef.parse should throw
    // IllegalRelativeRef, not IllegalOrigin
    assertThatExceptionOfType(IllegalRelativeRef.class)
        .isThrownBy(() -> RelativeRef.parse("http://example.com/"));
  }
}
