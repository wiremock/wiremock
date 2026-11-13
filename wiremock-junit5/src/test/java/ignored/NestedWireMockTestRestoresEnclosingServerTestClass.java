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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;

@WireMockTest(httpPort = 8773)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class NestedWireMockTestRestoresEnclosingServerTestClass {

  @Nested
  @Order(1)
  @WireMockTest(httpPort = 8774)
  class RunsOn8774 {
    @Test
    void runs_on_the_nested_port(WireMockRuntimeInfo wmRuntimeInfo) {
      assertThat(wmRuntimeInfo.getHttpPort(), is(8774));
    }
  }

  @Nested
  @Order(2)
  @WireMockTest
  class RunsOnDynamicPort {
    @Test
    void runs_on_a_different_port(WireMockRuntimeInfo wmRuntimeInfo) {
      assertThat(wmRuntimeInfo.getHttpPort() == 8773, is(false));
    }
  }

  @Nested
  @Order(3)
  class RunsOnInheritedPort {
    @Test
    void runs_on_the_enclosing_port(WireMockRuntimeInfo wmRuntimeInfo) {
      assertThat(wmRuntimeInfo.getHttpPort(), is(8773));
    }
  }
}
