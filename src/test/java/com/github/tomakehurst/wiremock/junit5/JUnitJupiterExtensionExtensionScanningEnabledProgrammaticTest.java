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
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import java.io.IOException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class JUnitJupiterExtensionExtensionScanningEnabledProgrammaticTest {

  private static int responseCode(String url) {
    try (CloseableHttpClient client = HttpClientFactory.createClient();
        CloseableHttpResponse response = client.execute(new HttpGet(url))) {
      return response.getCode();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nested
  class Default {
    @RegisterExtension
    WireMockExtension wm =
        WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    @Test
    void extension_scanning_defaults_to_false() {
      wm.stubFor(requestMatching("mock").willReturn(ok()));
      assertNotEquals(responseCode(wm.getRuntimeInfo().getHttpBaseUrl()), 200);
    }
  }

  @Nested
  class Disabled {
    @RegisterExtension
    WireMockExtension disabled =
        WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().extensionScanningEnabled(false))
            .build();

    @Test
    void extension_scanning_disabled() {
      disabled.stubFor(requestMatching("mock").willReturn(ok()));
      assertNotEquals(responseCode(disabled.getRuntimeInfo().getHttpBaseUrl()), 200);
    }
  }

  @Nested
  class Enabled {
    @RegisterExtension
    WireMockExtension enabled =
        WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().extensionScanningEnabled(true))
            .build();

    @Test
    void extension_scanning_enabled() {
      enabled.stubFor(requestMatching("mock").willReturn(ok()));
      assertEquals(responseCode(enabled.getRuntimeInfo().getHttpBaseUrl()), 200);
    }
  }
}
