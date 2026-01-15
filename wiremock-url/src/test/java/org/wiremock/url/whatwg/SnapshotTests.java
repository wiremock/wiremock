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
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.writeResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.wiremock.url.AbsoluteUri;
import org.wiremock.url.AbsoluteUrl;
import org.wiremock.url.IllegalUri;
import org.wiremock.url.Origin;
import org.wiremock.url.Uri;

/**
 * Snapshot tests for URI/URL parsing behaviour.
 *
 * <h2>What is Snapshot Testing?</h2>
 *
 * Snapshot testing captures the actual behaviour of the URI parser and compares it against
 * previously recorded "snapshots" stored in JSON files. When implementation changes cause different
 * parsing behaviour, the tests fail but automatically update the snapshots to match the new
 * behaviour. This allows developers to review what changed and verify it's intentional.
 *
 * <h2>How It Works</h2>
 *
 * <ol>
 *   <li>Test cases are loaded from JSON snapshot files in {@code
 *       src/test/resources/org/wiremock/url/whatwg/}
 *   <li>Each test parses a URI and compares the result against expected values in the snapshot
 *   <li>When actual behaviour differs from the snapshot:
 *       <ul>
 *         <li>The test fails (as expected)
 *         <li>The actual behaviour is recorded in memory
 *         <li>After all tests complete, the {@link FailureTracker} writes updated snapshots to disk
 *       </ul>
 *   <li>Developers review the updated snapshots to verify the changes are correct
 *   <li>Re-running the tests with the updated snapshots will pass
 * </ol>
 *
 * <h2>Test Categories</h2>
 *
 * Tests are organised into four categories based on two dimensions:
 *
 * <ul>
 *   <li><strong>WHATWG validity</strong>: Whether the input is valid according to the WHATWG URL
 *       Standard
 *   <li><strong>WireMock validity</strong>: Whether WireMock successfully parses the input
 * </ul>
 *
 * This creates four snapshot files:
 *
 * <ul>
 *   <li>{@code whatwg_valid_wiremock_valid.json} - Valid by both standards (ideal cases)
 *   <li>{@code whatwg_valid_wiremock_invalid.json} - WHATWG considers valid but WireMock rejects
 *   <li>{@code whatwg_invalid_wiremock_valid.json} - WHATWG considers invalid but WireMock accepts
 *       (lenient parsing)
 *   <li>{@code whatwg_invalid_wiremock_invalid.json} - Invalid by both standards
 * </ul>
 *
 * <h2>What Gets Tested</h2>
 *
 * For successful parses ({@link #wiremock_valid}), the test verifies:
 *
 * <ul>
 *   <li>The parsed input URI and its normalised form
 *   <li>The parsed base URI (if provided) and its normalised form
 *   <li>The result of resolving the input against the base URI
 *   <li>The origin (for absolute URLs)
 *   <li>All URI components (scheme, authority, host, port, path, query, fragment, etc.)
 * </ul>
 *
 * For parsing failures ({@link #wiremock_invalid}), the test verifies:
 *
 * <ul>
 *   <li>The exception type thrown
 *   <li>The exception message
 *   <li>The cause exception type (if any)
 *   <li>The cause exception message (if any)
 * </ul>
 *
 * <h2>Developer Workflow</h2>
 *
 * <ol>
 *   <li>Make changes to the URI parsing implementation
 *   <li>Run {@code SnapshotTests}
 *   <li>If behaviour changed, tests will fail and snapshots will be updated
 *   <li>Review the diff in the JSON snapshot files (e.g., using git diff)
 *   <li>If changes are correct, commit the updated snapshots along with the code changes
 *   <li>If changes are incorrect, fix the implementation and repeat
 * </ol>
 *
 * @see WhatWGUrlTestManagement for test data loading and snapshot file management
 * @see FailureTracker for the JUnit extension that handles snapshot updates
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(FailureTracker.class)
public class SnapshotTests {

  final List<SimpleParseFailure> updated_whatwg_valid_wiremock_invalid = new ArrayList<>();
  final List<SimpleParseFailure> updated_whatwg_invalid_wiremock_invalid = new ArrayList<>();
  final List<SimpleParseSuccess> updated_whatwg_valid_wiremock_valid = new ArrayList<>();
  final List<SimpleParseSuccess> updated_whatwg_invalid_wiremock_valid = new ArrayList<>();

  volatile boolean failed = false;

  private static final List<? extends SimpleParseSuccess> wiremock_valid =
      WhatWGUrlTestManagement.wiremock_valid;

  @ParameterizedTest
  @FieldSource("wiremock_valid")
  void wiremock_valid(SimpleParseSuccess testCase) {

    var inputUriRef = parseReference(testCase, testCase.input());
    Uri inputNormalised = inputUriRef.normalise();

    var baseUri = parseUri(testCase, testCase.base());
    var baseNormalised = baseUri != null ? baseUri.normalise() : null;

    Uri resolved = baseUri == null ? inputNormalised : baseUri.resolve(inputUriRef);

    Origin origin = resolved instanceof AbsoluteUrl resolvedUrl ? resolvedUrl.getOrigin() : null;

    try {
      assertSoftly(
          s -> {
            s.assertThat(toExpectation(inputUriRef)).isEqualTo(testCase.inputExpected());
            s.assertThat(toExpectation(inputNormalised)).isEqualTo(testCase.inputNormalised());
            s.assertThat(toExpectation(baseUri)).isEqualTo(testCase.baseExpected());
            s.assertThat(toExpectation(baseNormalised)).isEqualTo(testCase.baseNormalised());
            s.assertThat(toExpectation(resolved)).isEqualTo(testCase.resolved());
            s.assertThat(toExpectation(origin)).isEqualTo(testCase.origin());
          });
      registerSuccess(testCase);
    } catch (AssertionError e) {
      registerUpdatedSuccess(testCase);
      throw e;
    }
  }

  private static final List<? extends SimpleParseFailure> wiremock_invalid =
      WhatWGUrlTestManagement.wiremock_invalid;

  @ParameterizedTest
  @FieldSource("wiremock_invalid")
  void wiremock_invalid(SimpleParseFailure testCase) {

    var throwable =
        assertThatExceptionOfType(IllegalUri.class)
            .isThrownBy(
                () -> {
                  Uri.parse(testCase.input());
                  registerUpdatedSuccess(testCase);
                })
            .actual();

    try {
      assertSoftly(
          s -> {
            s.assertThat(throwable.getClass().getSimpleName()).isEqualTo(testCase.exceptionType());
            s.assertThat(throwable.getMessage()).isEqualTo(testCase.exceptionMessage());
            s.assertThat(
                    throwable.getCause() == null
                        ? null
                        : throwable.getCause().getClass().getSimpleName())
                .isEqualTo(testCase.exceptionCauseType());
            s.assertThat(throwable.getCause() == null ? null : throwable.getCause().getMessage())
                .isEqualTo(testCase.exceptionCauseMessage());
          });
      registerFailure(testCase);
    } catch (AssertionError e) {
      registerUpdatedFailure(testCase, throwable);
      throw e;
    }
  }

  private Uri parseReference(SimpleParseSuccess testCase, String input) {
    try {
      return Uri.parse(input);
    } catch (Exception e) {
      registerUpdatedFailure(testCase, e);
      throw e;
    }
  }

  private @Nullable AbsoluteUri parseUri(SimpleParseSuccess testCase, @Nullable String input) {
    if (input == null) {
      return null;
    }
    try {
      return AbsoluteUri.parse(input);
    } catch (Exception e) {
      registerUpdatedFailure(testCase, e);
      throw e;
    }
  }

  private void registerUpdatedSuccess(WireMockSnapshotTestCase testCase) {
    String input = testCase.input();
    Uri inputUriRef = Uri.parse(input);
    final Uri inputNormalised = inputUriRef.normalise();

    final UriReferenceExpectation inputExpected = toExpectation(inputUriRef);
    final UriReferenceExpectation inputNormalisedExpected = toExpectation(inputNormalised);

    final String base = testCase.base();

    final AbsoluteUri baseUri;
    final AbsoluteUri baseUriNormalised;
    final Uri resolved;

    if (base == null || base.isEmpty() || base.equals("null")) {
      baseUri = null;
      baseUriNormalised = null;
      resolved = inputNormalised;
    } else {
      baseUri = AbsoluteUri.parse(base);
      baseUriNormalised = baseUri.normalise();
      resolved = baseUri.resolve(inputUriRef);
    }

    final UriReferenceExpectation baseExpected = toExpectation(baseUri);
    final UriReferenceExpectation baseNormalised = toExpectation(baseUriNormalised);
    final UriReferenceExpectation resolvedExpected = toExpectation(resolved);

    final UriReferenceExpectation origin;
    if (resolved instanceof AbsoluteUrl resolvedUrl) {
      origin = toExpectation(resolvedUrl.getOrigin());
    } else {
      origin = null;
    }

    SimpleParseSuccess updated =
        new SimpleParseSuccess(
            input,
            base,
            inputExpected,
            inputNormalisedExpected,
            baseExpected,
            baseNormalised,
            resolvedExpected,
            origin,
            testCase.source(),
            matchesWhatWg(testCase.source(), resolved));
    registerSuccess(updated);
  }

  private boolean matchesWhatWg(WhatWGUrlTestCase source, Uri resolved) {
    if (source instanceof SuccessWhatWGUrlTestCase successWhatWGUrlTestCase) {
      return successWhatWGUrlTestCase.href().equals(resolved.toString());
    } else {
      return false;
    }
  }

  private void registerSuccess(SimpleParseSuccess testCase) {
    if (testCase.source().failure()) {
      updated_whatwg_invalid_wiremock_valid.add(testCase);
    } else {
      updated_whatwg_valid_wiremock_valid.add(testCase);
    }
  }

  private void registerUpdatedFailure(WireMockSnapshotTestCase testCase, Throwable e) {
    var failure =
        new SimpleParseFailure(
            testCase.input(),
            testCase.base(),
            e.getClass().getSimpleName(),
            e.getMessage(),
            e.getCause() == null ? null : e.getCause().getClass().getSimpleName(),
            e.getCause() == null ? null : e.getCause().getMessage(),
            testCase.source());
    registerFailure(failure);
  }

  private void registerFailure(SimpleParseFailure failure) {
    if (failure.source().failure()) {
      updated_whatwg_invalid_wiremock_invalid.add(failure);
    } else {
      updated_whatwg_valid_wiremock_invalid.add(failure);
    }
  }

  public static @Nullable UriReferenceExpectation toExpectation(@Nullable Uri uri) {
    if (uri == null) {
      return null;
    }
    return new UriReferenceExpectation(
        uri.toString(),
        getFirstInterface(uri),
        uri.getScheme() == null ? null : uri.getScheme().toString(),
        uri.getAuthority() == null ? null : uri.getAuthority().toString(),
        uri.getUserInfo() == null ? null : uri.getUserInfo().toString(),
        uri.getUserInfo() == null ? null : uri.getUserInfo().getUsername().toString(),
        uri.getUserInfo() == null
            ? null
            : (uri.getUserInfo().getPassword() == null
                ? null
                : uri.getUserInfo().getPassword().toString()),
        uri.getHost() == null ? null : uri.getHost().toString(),
        uri.getPort() == null ? null : uri.getPort().toString(),
        uri.getPath().toString(),
        uri.getQuery() == null ? null : uri.getQuery().toString(),
        uri.getFragment() == null ? null : uri.getFragment().toString());
  }

  static String getFirstInterface(Uri uri) {
    //noinspection OptionalGetWithoutIsPresent
    return Arrays.stream(uri.getClass().getInterfaces()).findFirst().get().getSimpleName();
  }
}

/**
 * JUnit extension that tracks test failures and updates snapshot files.
 *
 * <p>This extension implements two JUnit interfaces:
 *
 * <ul>
 *   <li>{@link TestWatcher} - to detect when any test fails
 *   <li>{@link AfterAllCallback} - to write updated snapshots after all tests complete
 * </ul>
 *
 * <p>When any test in {@link SnapshotTests} fails, this extension:
 *
 * <ol>
 *   <li>Sets the {@code failed} flag to true
 *   <li>After all tests complete, writes the captured actual behaviour to snapshot JSON files
 *   <li>Sorts the test cases alphabetically by input for consistent file diffs
 * </ol>
 *
 * <p>The snapshot files are written to {@code src/test/resources/org/wiremock/url/whatwg/} with
 * names like {@code whatwg_valid_wiremock_valid.json}.
 */
class FailureTracker implements TestWatcher, AfterAllCallback {

  @Override
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public void testFailed(ExtensionContext context, @Nullable Throwable cause) {
    ((SnapshotTests) context.getTestInstance().get()).failed = true;
  }

  @Override
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public void afterAll(ExtensionContext context) throws Exception {
    var snapshotTests = (SnapshotTests) context.getTestInstance().get();
    if (snapshotTests.failed) {
      writeResource(
          "whatwg_valid_wiremock_valid",
          sort(snapshotTests.updated_whatwg_valid_wiremock_valid),
          new CustomDepthPrettyPrinter());
      writeResource(
          "whatwg_valid_wiremock_invalid",
          sort(snapshotTests.updated_whatwg_valid_wiremock_invalid));
      writeResource(
          "whatwg_invalid_wiremock_valid",
          sort(snapshotTests.updated_whatwg_invalid_wiremock_valid),
          new CustomDepthPrettyPrinter());
      writeResource(
          "whatwg_invalid_wiremock_invalid",
          sort(snapshotTests.updated_whatwg_invalid_wiremock_invalid));
    }
  }

  private List<?> sort(List<? extends WireMockSnapshotTestCase> testCases) {
    return testCases.stream()
        .sorted(Comparator.comparing(WireMockSnapshotTestCase::input))
        .toList();
  }
}
