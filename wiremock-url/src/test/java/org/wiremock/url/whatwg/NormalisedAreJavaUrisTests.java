/*
 * Copyright (C) 2026 Thomas Akehurst
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
package org.wiremock.url.whatwg;

import java.net.URI;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.wiremock.url.AbsoluteUri;
import org.wiremock.url.AbsoluteUrl;
import org.wiremock.url.Authority;
import org.wiremock.url.Origin;
import org.wiremock.url.Uri;

public class NormalisedAreJavaUrisTests {

  private static final List<? extends SimpleParseSuccess> wiremock_valid =
      WhatWGUrlTestManagement.wiremock_valid;

  @ParameterizedTest
  @FieldSource("wiremock_valid")
  void normalised_are_all_valid_java_uris(SimpleParseSuccess testCase) {
    var inputUriRef = Uri.parse(testCase.input());
    Uri inputNormalised = inputUriRef.normalise();

    var baseUri = parseUri(testCase.base());
    var baseNormalised = baseUri != null ? baseUri.normalise() : null;

    Uri resolved = baseUri == null ? inputNormalised : baseUri.resolve(inputUriRef);

    Origin origin = resolved instanceof AbsoluteUrl resolvedUrl ? resolvedUrl.getOrigin() : null;

    Assertions.assertDoesNotThrow(
        () -> {
          if (hasJavaValidAuthority(inputNormalised)) {
            URI.create(inputNormalised.toString());
          }
          if (baseNormalised != null) {
            URI.create(baseNormalised.toString());
          }
          if (hasJavaValidAuthority(resolved)) {
            URI.create(resolved.toString());
          }
          if (origin != null && hasJavaValidAuthority(origin)) {
            URI.create(origin.toString());
          }
        });
  }

  private @Nullable AbsoluteUri parseUri(@Nullable String input) {
    if (input == null) {
      return null;
    }
    return AbsoluteUri.parse(input);
  }

  private static boolean hasJavaValidAuthority(Uri uri) {
    Authority authority = uri.getAuthority();
    return authority == null || !authority.toString().isEmpty();
  }
}
