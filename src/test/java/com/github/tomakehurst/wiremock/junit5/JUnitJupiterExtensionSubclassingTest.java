/*
 * Copyright (C) 2021 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.junit5.WireMockExtension.extensionOptions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class JUnitJupiterExtensionSubclassingTest {

  @BeforeEach
  public void beforeEach() {
    MyWireMockExtension.reset();
  }

  @Test
  void executes_all_lifecycle_callbacks() {
    Events testEvents =
        EngineTestKit.engine("junit-jupiter")
            .selectors(selectClass(TestClass.class))
            .execute()
            .testEvents();

    testEvents.assertStatistics(stats -> stats.succeeded(1));

    assertThat(MyWireMockExtension.beforeAllCalled, is(true));
    assertThat(MyWireMockExtension.beforeEachCalled, is(true));
    assertThat(MyWireMockExtension.afterEachCalled, is(true));
    assertThat(MyWireMockExtension.afterAllCalled, is(true));
  }

  public static class TestClass {

    CloseableHttpClient client = HttpClientFactory.createClient();

    @RegisterExtension
    static MyWireMockExtension wm =
        new MyWireMockExtension(
            extensionOptions()
                .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
                .configureStaticDsl(true));

    @Test
    void respects_config_passed_via_builder() throws Exception {
      assertThat(MyWireMockExtension.beforeAllCalled, is(true));
      assertThat(MyWireMockExtension.beforeEachCalled, is(true));
      assertThat(MyWireMockExtension.afterEachCalled, is(false));
      assertThat(MyWireMockExtension.afterAllCalled, is(false));

      stubFor(get("/ping").willReturn(ok()));

      try (CloseableHttpResponse response =
          client.execute(new HttpGet("https://localhost:" + wm.getHttpsPort() + "/ping"))) {
        assertThat(response.getCode(), is(200));
      }
    }
  }

  public static class MyWireMockExtension extends WireMockExtension {

    public static boolean beforeAllCalled = false;
    public static boolean beforeEachCalled = false;
    public static boolean afterEachCalled = false;
    public static boolean afterAllCalled = false;

    public MyWireMockExtension(WireMockExtension.Builder builder) {
      super(builder);
    }

    @Override
    protected void onBeforeAll(WireMockRuntimeInfo wireMockRuntimeInfo) {
      beforeAllCalled = true;
    }

    @Override
    protected void onBeforeEach(WireMockRuntimeInfo wireMockRuntimeInfo) {
      beforeEachCalled = true;
    }

    @Override
    protected void onAfterEach(WireMockRuntimeInfo wireMockRuntimeInfo) {
      afterEachCalled = true;
    }

    @Override
    protected void onAfterAll(WireMockRuntimeInfo wireMockRuntimeInfo) {
      afterAllCalled = true;
    }

    public static void reset() {
      beforeAllCalled = false;
      beforeEachCalled = false;
      afterEachCalled = false;
      afterAllCalled = false;
    }
  }
}
