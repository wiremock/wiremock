/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.common.JettySettings;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.jetty12.Jetty12HttpServer;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QueuedThreadPoolAcceptanceTest extends AcceptanceTestBase {

  @BeforeAll
  public static void setupServer() {
    setupServer(
        new WireMockConfiguration()
            .httpServerFactory(
                (options, adminRequestHandler, stubRequestHandler) ->
                    new Jetty12HttpServer(
                        options,
                        adminRequestHandler,
                        stubRequestHandler,
                        JettySettings.Builder.aJettySettings().build(),
                        new InstrumentedQueuedThreadPool(options.containerThreads()))));
  }

  @Test
  public void serverUseCustomInstrumentedQueuedThreadPool() {
    assertThat(InstrumentedQueuedThreadPool.flag, is(true));
  }

  public static class InstrumentedQueuedThreadPool extends QueuedThreadPool {
    public static boolean flag = false;

    public InstrumentedQueuedThreadPool(int maxThreads) {
      this(maxThreads, 8);
    }

    public InstrumentedQueuedThreadPool(int maxThreads, int minThreads) {
      this(maxThreads, minThreads, 60000);
    }

    public InstrumentedQueuedThreadPool(int maxThreads, int minThreads, int idleTimeout) {
      super(maxThreads, minThreads, idleTimeout, null);
    }

    @Override
    protected void doStart() throws Exception {
      super.doStart();
      flag = true;
    }
  }
}
