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
package org.wiremock.url.whatwg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.concat;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.remoteUrl;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.rfc3986_invalid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.rfc3986_valid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.sortTestData;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.testData;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.updateTestData;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_invalid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_invalid_rfc3986_invalid_wiremock_invalid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_invalid_rfc3986_invalid_wiremock_valid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_invalid_rfc3986_valid_wiremock_invalid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_invalid_rfc3986_valid_wiremock_valid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_valid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_valid_rfc3986_invalid_wiremock_invalid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_valid_rfc3986_invalid_wiremock_valid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_valid_rfc3986_valid_wiremock_invalid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_valid_rfc3986_valid_wiremock_valid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.wiremock_invalid;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.wiremock_valid;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.wiremock.url.Url;

class WhatWGUrlInvariantTests {

  @Test
  @EnabledIf("remoteDataReachable")
  void test_data_is_up_to_date() throws IOException, URISyntaxException {
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
  void whatwg_valid_rfc3986_valid_wiremock_valid_contains_no_duplicates() {
    assertThat(whatwg_valid_rfc3986_valid_wiremock_valid).doesNotHaveDuplicates();
  }

  @Test
  void whatwg_valid_rfc3986_valid_wiremock_invalid_contains_no_duplicates() {
    assertThat(whatwg_valid_rfc3986_valid_wiremock_invalid).doesNotHaveDuplicates();
  }

  @Test
  void whatwg_valid_rfc3986_invalid_wiremock_valid_contains_no_duplicates() {
    assertThat(whatwg_valid_rfc3986_invalid_wiremock_valid).doesNotHaveDuplicates();
  }

  @Test
  void whatwg_valid_rfc3986_invalid_wiremock_invalid_contains_no_duplicates() {
    assertThat(whatwg_valid_rfc3986_invalid_wiremock_invalid).doesNotHaveDuplicates();
  }

  @Test
  void whatwg_invalid_rfc3986_valid_wiremock_valid_contains_no_duplicates() {
    assertThat(whatwg_invalid_rfc3986_valid_wiremock_valid).doesNotHaveDuplicates();
  }

  @Test
  void whatwg_invalid_rfc3986_valid_wiremock_invalid_contains_no_duplicates() {
    assertThat(whatwg_invalid_rfc3986_valid_wiremock_invalid).doesNotHaveDuplicates();
  }

  @Test
  void whatwg_invalid_rfc3986_invalid_wiremock_valid_contains_no_duplicates() {
    assertThat(whatwg_invalid_rfc3986_invalid_wiremock_valid).doesNotHaveDuplicates();
  }

  @Test
  void whatwg_invalid_rfc3986_invalid_wiremock_invalid_contains_no_duplicates() {
    assertThat(whatwg_invalid_rfc3986_invalid_wiremock_invalid).doesNotHaveDuplicates();
  }

  @Test
  void whatwg_covers_all_cases() throws IOException, URISyntaxException {
    coversAllCases(whatwg_valid, whatwg_invalid);
  }

  @Test
  void rfc3986_covers_all_cases() throws IOException, URISyntaxException {
    coversAllCases(rfc3986_valid, rfc3986_invalid);
  }

  @Test
  void wiremock_covers_all_cases() throws IOException, URISyntaxException {
    coversAllCases(wiremock_valid, wiremock_invalid);
  }

  private static void coversAllCases(List<WhatWGUrlTestCase> valid, List<WhatWGUrlTestCase> invalid)
      throws IOException, URISyntaxException {
    try {
      assertThat(concat(valid, invalid)).containsExactlyInAnyOrderElementsOf(testData);
    } catch (AssertionError e) {
      sortTestData();
      throw e;
    }
  }

  private static boolean remoteDataReachable() {
    return hostReachable(remoteUrl);
  }

  @SuppressWarnings("SameParameterValue")
  private static boolean hostReachable(Url url) {
    try (Socket socket = new Socket()) {
      //noinspection DataFlowIssue
      socket.connect(new InetSocketAddress(url.host().toString(), url.resolvedPort().port()), 500);
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
