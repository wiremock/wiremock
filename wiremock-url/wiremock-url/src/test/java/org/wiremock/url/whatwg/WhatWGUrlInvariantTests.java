/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.wiremock.url.Lists.concat;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.remoteUrl;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.sortTestData;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.testData;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.updateTestData;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_invalid_wiremock_invalid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_invalid_wiremock_valid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_valid_wiremock_invalid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_valid_wiremock_valid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.wiremock_invalid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.wiremock_valid;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.wiremock.url.AbsoluteUrl;
import org.wiremock.url.Rfc3986Validator;

class WhatWGUrlInvariantTests {

  @Test
  @EnabledIf("remoteDataReachable")
  void test_data_is_up_to_date() throws IOException {
    String expected = WhatWGUrlTestManagement.readRemote();
    try {
      assertThat(WhatWGUrlTestManagement.readLocal()).isEqualTo(expected);
    } catch (AssertionError e) {
      updateTestData(expected);
      throw e;
    }
  }

  @Test
  void test_data_contains_no_duplicates() {
    assertThat(testData).doesNotHaveDuplicates();
  }

  @Test
  void whatwg_valid_wiremock_valid_contains_no_duplicates() {
    assertThat(whatwg_valid_wiremock_valid).doesNotHaveDuplicates();
  }

  @Test
  void whatwg_valid_wiremock_invalid_contains_no_duplicates() {
    assertThat(whatwg_valid_wiremock_invalid).doesNotHaveDuplicates();
  }

  @Test
  void whatwg_invalid_wiremock_valid_contains_no_duplicates() {
    assertThat(whatwg_invalid_wiremock_valid).doesNotHaveDuplicates();
  }

  @Test
  void whatwg_invalid_wiremock_invalid_contains_no_duplicates() {
    assertThat(whatwg_invalid_wiremock_invalid).doesNotHaveDuplicates();
  }

  @Test
  void whatwg_covers_all_cases() throws IOException {
    coversAllWireMockCases(whatwg_valid, whatwg_invalid);
  }

  @Test
  void wiremock_covers_all_cases() throws IOException {
    coversAllWireMockCases(wiremock_valid, wiremock_invalid);
  }

  @Test
  void rfc3986_covers_all_cases() throws IOException {
    coversAllWhatWgCases(rfc3986_valid, rfc3986_invalid);
  }

  @Test
  void java_covers_all_cases() throws IOException {
    coversAllWhatWgCases(java_valid, java_invalid);
  }

  private static final List<? extends WhatWGUrlTestCase> rfc3986_valid =
      WhatWGUrlTestManagement.rfc3986_valid;

  @ParameterizedTest
  @FieldSource("rfc3986_valid")
  void rfc3986_valid_is_correct(WhatWGUrlTestCase testCase) {
    assertThat(Rfc3986Validator.isValidUriReference(testCase.input())).isTrue();
  }

  private static final List<? extends WhatWGUrlTestCase> rfc3986_invalid =
      WhatWGUrlTestManagement.rfc3986_invalid;

  @ParameterizedTest
  @FieldSource("rfc3986_invalid")
  void rfc3986_invalid_is_correct(WhatWGUrlTestCase testCase) {
    assertThat(Rfc3986Validator.isValidUriReference(testCase.input())).isFalse();
  }

  private static final List<? extends WireMockSnapshotTestCase> whatwg_valid =
      WhatWGUrlTestManagement.whatwg_valid;

  @ParameterizedTest
  @FieldSource("whatwg_valid")
  void whatwg_valid_is_correct(WireMockSnapshotTestCase testCase) {
    assertThat(testCase.source().success()).isTrue();
  }

  private static final List<? extends WireMockSnapshotTestCase> whatwg_invalid =
      WhatWGUrlTestManagement.whatwg_invalid;

  @ParameterizedTest
  @FieldSource("whatwg_invalid")
  void whatwg_invalid_is_correct(WireMockSnapshotTestCase testCase) {
    assertThat(testCase.source().success()).isFalse();
  }

  private static final List<? extends WhatWGUrlTestCase> java_valid =
      WhatWGUrlTestManagement.java_valid;

  @ParameterizedTest
  @FieldSource("java_valid")
  void java_valid_is_correct(WhatWGUrlTestCase testCase) throws URISyntaxException {
    new URI(testCase.input());
  }

  private static final List<? extends WhatWGUrlTestCase> java_invalid =
      WhatWGUrlTestManagement.java_invalid;

  @ParameterizedTest
  @FieldSource("java_invalid")
  void java_invalid_is_correct(WhatWGUrlTestCase testCase) {
    assertThatThrownBy(() -> new URI(testCase.input())).isInstanceOf(URISyntaxException.class);
  }

  private static void coversAllWhatWgCases(
      List<? extends WhatWGUrlTestCase> valid, List<? extends WhatWGUrlTestCase> invalid)
      throws IOException {
    try {
      @SuppressWarnings("unchecked")
      var concat = (List<WhatWGUrlTestCase>) concat(valid, invalid);
      assertThat(concat).containsAll(testData);
    } catch (AssertionError e) {
      sortTestData();
      throw e;
    }
  }

  private static void coversAllWireMockCases(
      List<? extends WireMockSnapshotTestCase> valid,
      List<? extends WireMockSnapshotTestCase> invalid)
      throws IOException {
    try {
      var whatWgTestCases = concat(valid, invalid).stream().map(WireMockSnapshotTestCase::source);

      assertThat(whatWgTestCases).containsAll(testData);
    } catch (AssertionError e) {
      sortTestData();
      throw e;
    }
  }

  private static boolean remoteDataReachable() {
    return hostReachable(remoteUrl);
  }

  @SuppressWarnings("SameParameterValue")
  private static boolean hostReachable(AbsoluteUrl url) {
    try (Socket socket = new Socket()) {
      //noinspection DataFlowIssue
      socket.connect(
          new InetSocketAddress(url.getHost().toString(), url.getResolvedPort().getIntValue()),
          500);
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
