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
package com.github.tomakehurst.wiremock.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class WireMockDefaultInstanceMemoryLeakTest {

  @Test
  void stoppedServerIsNotRetainedByDefaultInstanceOrInheritedChildThreads() throws Exception {
    CountDownLatch childStarted = new CountDownLatch(1);
    CountDownLatch releaseChild = new CountDownLatch(1);

    WeakReference<WireMockServer> serverRef =
        startConfigureSpawnChildAndStop(childStarted, releaseChild);

    try {
      awaitGc(serverRef);
      assertThat(serverRef.get())
          .as("Stopped WireMockServer should be garbage-collected even while a child thread lives")
          .isNull();
    } finally {
      releaseChild.countDown();
    }
  }

  private WeakReference<WireMockServer> startConfigureSpawnChildAndStop(
      CountDownLatch childStarted, CountDownLatch releaseChild) throws Exception {
    WireMockServer server = new WireMockServer(wireMockConfig().dynamicPort());
    server.start();
    WireMock.configureFor(new WireMock(server));

    Thread longLivedChild =
        new Thread(
            () -> {
              childStarted.countDown();
              try {
                releaseChild.await(30, TimeUnit.SECONDS);
              } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
              }
            },
            "wiremock-leak-test-child");
    longLivedChild.setDaemon(true);
    longLivedChild.start();
    assertThat(childStarted.await(5, TimeUnit.SECONDS)).isTrue();

    WeakReference<WireMockServer> ref = new WeakReference<>(server);
    server.stop();
    return ref;
  }

  @Test
  void reconfiguringOnSameThreadReleasesPreviousClientFromInheritedChildThreads() throws Exception {
    CountDownLatch childStarted = new CountDownLatch(1);
    CountDownLatch releaseChild = new CountDownLatch(1);

    WeakReference<WireMock> firstClientRef =
        configureSpawnChildAndReconfigure(childStarted, releaseChild);

    try {
      awaitGc(firstClientRef);
      assertThat(firstClientRef.get())
          .as("Previous WireMock defaultInstance should be GC'd after reconfiguring on same thread")
          .isNull();
    } finally {
      releaseChild.countDown();
      WireMock.configure();
    }
  }

  private WeakReference<WireMock> configureSpawnChildAndReconfigure(
      CountDownLatch childStarted, CountDownLatch releaseChild) throws Exception {
    WireMock firstClient = WireMock.create().port(18081).build();
    WireMock.configureFor(firstClient);

    Thread longLivedChild =
        new Thread(
            () -> {
              childStarted.countDown();
              try {
                releaseChild.await(30, TimeUnit.SECONDS);
              } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
              }
            },
            "wiremock-reconfigure-leak-test-child");
    longLivedChild.setDaemon(true);
    longLivedChild.start();
    assertThat(childStarted.await(5, TimeUnit.SECONDS)).isTrue();

    WeakReference<WireMock> ref = new WeakReference<>(firstClient);
    WireMock.configureFor(18082);
    return ref;
  }

  @Test
  void childThreadReconfiguringDefaultInstanceDoesNotClearParentThreadInstance() throws Exception {
    WireMockServer parentServer = new WireMockServer(wireMockConfig().dynamicPort());
    WireMockServer childServer = new WireMockServer(wireMockConfig().dynamicPort());
    parentServer.start();
    childServer.start();
    try {
      WireMock.configureFor(new WireMock(parentServer));

      AtomicReference<Throwable> childError = new AtomicReference<>();
      Thread child =
          new Thread(
              () -> {
                try {
                  WireMock.configureFor(new WireMock(childServer));
                  stubFor(get("/child").willReturn(ok("from-child")));
                } catch (Throwable t) {
                  childError.set(t);
                }
              });
      child.start();
      child.join(5000);
      assertThat(childError.get()).isNull();

      stubFor(get("/parent").willReturn(ok("from-parent")));

      WireMockTestClient parentClient = new WireMockTestClient(parentServer.port());
      WireMockTestClient childClient = new WireMockTestClient(childServer.port());
      assertThat(parentClient.get("/parent").content()).isEqualTo("from-parent");
      assertThat(childClient.get("/child").content()).isEqualTo("from-child");
    } finally {
      childServer.stop();
      parentServer.stop();
    }
  }

  private static void awaitGc(WeakReference<?> ref) throws InterruptedException {
    for (int i = 0; i < 30 && ref.get() != null; i++) {
      System.gc();
      Thread.sleep(50);
    }
  }
}
