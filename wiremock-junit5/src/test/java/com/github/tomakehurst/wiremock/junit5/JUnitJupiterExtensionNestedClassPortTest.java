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
package com.github.tomakehurst.wiremock.junit5;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import ignored.InheritedWireMockTestWithNestedClassesTestClass;
import ignored.NestedWireMockTestRestoresEnclosingServerTestClass;
import ignored.NestedWireMockTestWithOwnPortTestClass;
import ignored.OnlyNestedClassAnnotatedTestClass;
import java.net.ServerSocket;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

public class JUnitJupiterExtensionNestedClassPortTest {

  @Test
  void nested_class_with_its_own_port_runs_on_that_port() {
    execute(NestedWireMockTestWithOwnPortTestClass.class)
        .assertStatistics(stats -> stats.succeeded(2).failed(0));
  }

  @Test
  void enclosing_server_is_restored_after_a_nested_class_with_its_own_server() {
    execute(NestedWireMockTestRestoresEnclosingServerTestClass.class)
        .assertStatistics(stats -> stats.succeeded(3).failed(0));
  }

  @Test
  void nested_classes_share_a_server_configured_on_an_abstract_superclass() {
    execute(InheritedWireMockTestWithNestedClassesTestClass.class)
        .assertStatistics(stats -> stats.succeeded(3).failed(0));
  }

  @Test
  void all_servers_are_stopped_after_the_test_class_completes() {
    execute(NestedWireMockTestWithOwnPortTestClass.class);
    execute(NestedWireMockTestRestoresEnclosingServerTestClass.class);
    execute(InheritedWireMockTestWithNestedClassesTestClass.class);
    execute(OnlyNestedClassAnnotatedTestClass.class)
        .assertStatistics(stats -> stats.succeeded(1).failed(0));

    for (int port : new int[] {8771, 8772, 8773, 8774, 8775, 8776}) {
      assertDoesNotThrow(() -> new ServerSocket(port).close(), "port " + port + " still in use");
    }
  }

  private static Events execute(Class<?> testClass) {
    return EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(testClass))
        .execute()
        .testEvents();
  }
}
