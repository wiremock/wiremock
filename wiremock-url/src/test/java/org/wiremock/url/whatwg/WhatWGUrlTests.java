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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.remoteUrl;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.testData;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.updateTestData;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.wiremock.url.IllegalUrlReference;
import org.wiremock.url.Url;
import org.wiremock.url.UrlReference;

class WhatWGUrlTests {

  @SuppressWarnings("unused")
  private static final List<WhatWGUrlTestCase> whatwg_valid =
      testData.stream().filter(test -> !test.failure()).toList();

  @ParameterizedTest
  @FieldSource("whatwg_valid")
  void we_accept_all_whatwg_valid_urls(WhatWGUrlTestCase testCase) {
    var url = UrlReference.parse(testCase.input());
  }

  @SuppressWarnings("unused")
  private static final List<WhatWGUrlTestCase> whatwg_invalid =
      testData.stream().filter(WhatWGUrlTestCase::failure).toList();

  @ParameterizedTest
  @FieldSource("whatwg_valid")
  void we_reject_all_whatwg_invalid_urls(WhatWGUrlTestCase testCase) {
    assertThatExceptionOfType(IllegalUrlReference.class)
        .isThrownBy(() -> UrlReference.parse(testCase.input()));
  }

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
