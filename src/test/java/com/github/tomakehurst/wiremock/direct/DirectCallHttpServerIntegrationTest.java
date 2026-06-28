/*
 * Copyright (C) 2021-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.direct;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import com.google.common.base.Stopwatch;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DirectCallHttpServerIntegrationTest {

  @Test
  void exampleUsage() {
    DirectCallHttpServerFactory factory = new DirectCallHttpServerFactory();
    WireMockServer wm = new WireMockServer(wireMockConfig().httpServerFactory(factory));
    wm.start(); // no-op

    DirectCallHttpServer server = factory.getHttpServer();

    Response response = server.stubRequest(mockRequest());
    assertEquals(404, response.getStatus());
  }

  @Test
  void withDelay() {
    DirectCallHttpServerFactory factory = new DirectCallHttpServerFactory();
    WireMockServer wm =
        new WireMockServer(
            wireMockConfig()
                .usingFilesUnderClasspath("classpath-filesource")
                .httpServerFactory(factory));
    wm.start(); // no-op

    DirectCallHttpServer server = factory.getHttpServer();

    MockRequest mockRequest = mockRequest().url("/slow-response").method(GET);

    Stopwatch stopwatch = Stopwatch.createStarted();
    Response response = server.stubRequest(mockRequest);
    stopwatch.stop();

    assertEquals(200, response.getStatus());
    assertThat(stopwatch.elapsed(MILLISECONDS), greaterThanOrEqualTo(499L));
  }

  @Test
  void withFileBody() {
    DirectCallHttpServerFactory factory = new DirectCallHttpServerFactory();
    WireMockServer wm =
        new WireMockServer(
            wireMockConfig()
                .usingFilesUnderClasspath("classpath-filesource")
                .httpServerFactory(factory));
    wm.start(); // no-op

    DirectCallHttpServer server = factory.getHttpServer();

    MockRequest mockRequest = mockRequest().url("/test").method(GET);
    Response response = server.stubRequest(mockRequest);
    assertEquals("THINGS!", response.getBodyAsString());
  }

  @Test
  void usesConfiguredNotifierForStubRequestLogging() {
    TestNotifier notifier = new TestNotifier();
    DirectCallHttpServerFactory factory = new DirectCallHttpServerFactory();
    WireMockServer wm =
        new WireMockServer(
            wireMockConfig()
                .httpServerFactory(factory)
                .stubRequestLoggingDisabled(false)
                .notifier(notifier));
    wm.start(); // no-op

    DirectCallHttpServer server = factory.getHttpServer();
    server.stubRequest(mockRequest().url("/not-matched").method(GET));

    assertThat(notifier.infoMessages, hasItem(containsString("Request received:")));
  }

  @Test
  void usesConfiguredNotifierForAdminRequestLogging() {
    TestNotifier notifier = new TestNotifier();
    DirectCallHttpServerFactory factory = new DirectCallHttpServerFactory();
    WireMockServer wm =
        new WireMockServer(wireMockConfig().httpServerFactory(factory).notifier(notifier));
    wm.start(); // no-op

    DirectCallHttpServer server = factory.getHttpServer();
    Response response = server.adminRequest(mockRequest().url("/__admin/mappings").method(GET));

    assertEquals(200, response.getStatus());
    assertThat(notifier.infoMessages, hasItem(containsString("Admin request received:")));
    assertThat(notifier.infoMessages, hasItem(containsString("GET /__admin/mappings")));
  }

  private static class TestNotifier implements Notifier {
    private final List<String> infoMessages = new ArrayList<>();

    @Override
    public void info(String message) {
      infoMessages.add(message);
    }

    @Override
    public void error(String message) {}

    @Override
    public void error(String message, Throwable t) {}
  }
}
