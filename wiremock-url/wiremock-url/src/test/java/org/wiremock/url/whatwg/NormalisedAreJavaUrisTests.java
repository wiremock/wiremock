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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.net.URI;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.wiremock.url.AbsoluteUri;
import org.wiremock.url.AbsoluteUrl;
import org.wiremock.url.Authority;
import org.wiremock.url.Origin;
import org.wiremock.url.Uri;

public class NormalisedAreJavaUrisTests {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "sc:foo",
        "file:///",
        "file://localhost",
        "file://localhost/",
        "sc:?",
        "sc:foo?",
        "file://?",
        "file://#frag",
        "file:///?",
        "file://localhost?",
        "file://localhost/?",
      })
  void java_valid_uris(String validUri) {
    assertDoesNotThrow(() -> URI.create(validUri));
  }

  @ParameterizedTest
  @ValueSource(strings = {"sc:", "file://", "sc:#frag"})
  void java_invalid_uris(String invalidUri) {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> URI.create(invalidUri));
  }

  private static final List<? extends SimpleParseSuccess> wiremock_valid =
      WhatWGUrlTestManagement.wiremock_valid;

  @ParameterizedTest
  @FieldSource("wiremock_valid")
  void normalised_are_all_valid_java_uris(SimpleParseSuccess testCase) {
    var inputUriRef = Uri.parse(testCase.input());
    Uri inputNormalised =
        inputUriRef instanceof AbsoluteUri absoluteUri ? absoluteUri.normalise() : inputUriRef;

    var baseUri = parseUri(testCase.base());
    var baseNormalised = baseUri != null ? baseUri.normalise() : null;

    Uri resolved = baseUri == null ? inputNormalised : baseUri.resolve(inputUriRef);

    Origin origin = resolved instanceof AbsoluteUrl resolvedUrl ? resolvedUrl.getOrigin() : null;

    assertDoesNotThrow(
        () -> {
          if (inputNormalised instanceof AbsoluteUri && notJavaEdgeCase(inputNormalised)) {
            URI.create(inputNormalised.toString());
          }
          if (baseNormalised != null && notJavaEdgeCase(baseNormalised)) {
            URI.create(baseNormalised.toString());
          }
          if (notJavaEdgeCase(resolved)) {
            URI.create(resolved.toString());
          }
          if (origin != null && notJavaEdgeCase(origin)) {
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

  private static boolean notJavaEdgeCase(Uri uri) {
    return !isJavaEdgeCase(uri);
  }

  private static boolean isJavaEdgeCase(Uri uri) {
    Authority authority = uri.getAuthority();
    boolean edgeCase1 = authority == null && uri.getPath().isEmpty() && uri.getQuery() == null;
    boolean edgeCase2 =
        authority != null
            && authority.toString().isEmpty()
            && uri.getPath().isEmpty()
            && uri.getQuery() == null
            && uri.getFragment() == null;
    return edgeCase1 || edgeCase2;
  }
}
