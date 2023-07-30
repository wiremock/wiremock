/*
 * Copyright (C) 2023 Thomas Akehurst
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
package ignored;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.junit5.WireMockExtension.extensionOptions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit5.JUnitJupiterExtensionSubclassingTest;
import com.github.tomakehurst.wiremock.testsupport.Network;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class JupiterExtensionTestClass {

  CloseableHttpClient client = HttpClientFactory.createClient();

  @RegisterExtension
  static JUnitJupiterExtensionSubclassingTest.MyWireMockExtension wm =
      new JUnitJupiterExtensionSubclassingTest.MyWireMockExtension(
          extensionOptions()
              .options(
                  wireMockConfig().port(Network.findFreePort()).httpsPort(Network.findFreePort()))
              .configureStaticDsl(true));

  @Test
  void respects_config_passed_via_builder() throws Exception {
    assertThat(JUnitJupiterExtensionSubclassingTest.MyWireMockExtension.beforeAllCalled, is(true));
    assertThat(JUnitJupiterExtensionSubclassingTest.MyWireMockExtension.beforeEachCalled, is(true));
    assertThat(JUnitJupiterExtensionSubclassingTest.MyWireMockExtension.afterEachCalled, is(false));
    assertThat(JUnitJupiterExtensionSubclassingTest.MyWireMockExtension.afterAllCalled, is(false));

    stubFor(get("/ping").willReturn(ok()));

    try (CloseableHttpResponse response =
        client.execute(new HttpGet("https://localhost:" + wm.getHttpsPort() + "/ping"))) {
      assertThat(response.getCode(), is(200));
    }
  }
}
