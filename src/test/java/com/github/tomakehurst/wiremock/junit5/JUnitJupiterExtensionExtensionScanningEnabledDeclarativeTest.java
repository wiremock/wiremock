/*
 * Copyright (C) 2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.junit5;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import java.io.IOException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class JUnitJupiterExtensionExtensionScanningEnabledDeclarativeTest {

  private static int responseCode(String url) {
    try (CloseableHttpClient client = HttpClientFactory.createClient();
        CloseableHttpResponse response = client.execute(new HttpGet(url))) {
      return response.getCode();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nested
  @WireMockTest
  class Default {

    @Test
    void extension_scanning_defaults_to_false(WireMockRuntimeInfo wmRuntimeInfo) {
      stubFor(requestMatching("mock").willReturn(ok()));
      assertNotEquals(responseCode(wmRuntimeInfo.getHttpBaseUrl()), 200);
    }
  }

  @Nested
  @WireMockTest(extensionScanningEnabled = false)
  class Disabled {

    @Test
    void extension_scanning_disabled(WireMockRuntimeInfo wmRuntimeInfo) {
      stubFor(requestMatching("mock").willReturn(ok()));
      assertNotEquals(responseCode(wmRuntimeInfo.getHttpBaseUrl()), 200);
    }
  }

  @Nested
  @WireMockTest(extensionScanningEnabled = true)
  class Enabled {

    @Test
    void extension_scanning_enabled(WireMockRuntimeInfo wmRuntimeInfo) {
      stubFor(requestMatching("mock").willReturn(ok()));
      assertEquals(responseCode(wmRuntimeInfo.getHttpBaseUrl()), 200);
    }
  }
}
