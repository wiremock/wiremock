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
package ignored;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.http.client.apache5.ApacheHttpClientFactory;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.standalone.CommandLineOptions;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

@WireMockTest(httpsEnabled = true)
public class DefaultHttpsKeystoreTestFixture {

  public static void verifyHttpsDefaults() {
    Launcher launcher = LauncherFactory.create();
    SummaryGeneratingListener listener = new SummaryGeneratingListener();
    launcher.registerTestExecutionListeners(listener);
    launcher.execute(
        LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClass(DefaultHttpsKeystoreTestFixture.class))
            .build());

    TestExecutionSummary summary = listener.getSummary();
    if (!summary.getFailures().isEmpty()) {
      throw new AssertionError("HTTPS fixture failed", summary.getFailures().get(0).getException());
    }
    assertEquals(3, summary.getTestsSucceededCount());
  }

  @Test
  void declarativeExtensionServesHttps(WireMockRuntimeInfo runtimeInfo) throws Exception {
    assertTrue(runtimeInfo.isHttpsEnabled());
    stubFor(get("/keystore-test").willReturn(ok()));
    assertHttpsResponse(runtimeInfo.getHttpsBaseUrl());
  }

  @Test
  void httpsSettingsBuilderUsesValidDefaultKeystore() throws Exception {
    assertTrue(new HttpsSettings.Builder().build().keyStore().loadStore().size() > 0);
  }

  @Test
  void standaloneOptionsServeHttpsWithDefaultKeystore() throws Exception {
    WireMockServer server =
        new WireMockServer(new CommandLineOptions("--port", "0", "--https-port", "0"));
    try {
      server.start();
      server.stubFor(get("/keystore-test").willReturn(ok()));
      assertHttpsResponse("https://localhost:" + server.httpsPort());
    } finally {
      server.stop();
    }
  }

  private static void assertHttpsResponse(String baseUrl) throws Exception {
    try (CloseableHttpClient client = ApacheHttpClientFactory.createClient()) {
      int status =
          client.execute(new HttpGet(baseUrl + "/keystore-test"), response -> response.getCode());
      assertEquals(200, status);
    }
  }
}
