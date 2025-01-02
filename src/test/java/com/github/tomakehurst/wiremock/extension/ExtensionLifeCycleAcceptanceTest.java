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
package com.github.tomakehurst.wiremock.extension;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.apache.hc.core5.http.ContentType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.github.tomakehurst.wiremock.AcceptanceTestBase;
import com.github.tomakehurst.wiremock.common.Notifier;
import java.util.ArrayList;
import java.util.List;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;

public class ExtensionLifeCycleAcceptanceTest extends AcceptanceTestBase {

  @Test
  public void extensionStartIsCalledWhenTheServerIsInitialised() {
    TestNotifier notifier = new TestNotifier();
    setupServer(
        wireMockConfig()
            .dynamicPort()
            .notifier(notifier)
            .extensions(new StartStopLoggingExtension(notifier)));
    assertThat(notifier.infoMessages.size(), greaterThanOrEqualTo(1));
    assertThat(notifier.infoMessages, hasItem(containsString("Extension started")));
  }

  @Test
  public void extensionStopIsCalledWhenTheServerShutsDown() {
    TestNotifier notifier = new TestNotifier();
    setupServer(
        wireMockConfig()
            .dynamicPort()
            .notifier(notifier)
            .extensions(new StartStopLoggingExtension(notifier)));
    notifier.reset();
    testClient.post("/__admin/shutdown", new StringEntity("", TEXT_PLAIN));
    // should contain the admin request log message and the stop message from our extension
    assertThat(notifier.infoMessages.size(), is(2));
    assertThat(notifier.infoMessages.get(1), containsString("Extension stopped"));
  }

  public static class TestNotifier implements Notifier {

    final List<String> infoMessages = new ArrayList<>();

    public void reset() {
      infoMessages.clear();
    }

    @Override
    public void info(String message) {
      infoMessages.add(message);
    }

    @Override
    public void error(String message) {}

    @Override
    public void error(String message, Throwable t) {}
  }

  public static class StartStopLoggingExtension implements Extension {
    private final Notifier notifier;

    public StartStopLoggingExtension(Notifier notifier) {
      this.notifier = notifier;
    }

    @Override
    public String getName() {
      return "start-stop-logging";
    }

    @Override
    public void start() {
      notifier.info("Extension started");
    }

    @Override
    public void stop() {
      notifier.info("Extension stopped");
    }
  }
}
