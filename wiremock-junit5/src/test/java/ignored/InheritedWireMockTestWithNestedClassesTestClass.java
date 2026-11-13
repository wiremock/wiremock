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

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class InheritedWireMockTestWithNestedClassesTestClass
    extends AbstractWireMockIntegrationTestClass {

  @Nested
  @Order(1)
  class FirstNested {
    @Test
    void runs_on_the_inherited_port(WireMockRuntimeInfo wmRuntimeInfo) {
      assertThat(wmRuntimeInfo.getHttpPort(), is(8776));
    }
  }

  @Nested
  @Order(2)
  class SecondNested {
    @Test
    void runs_on_the_inherited_port(WireMockRuntimeInfo wmRuntimeInfo) {
      assertThat(wmRuntimeInfo.getHttpPort(), is(8776));
    }
  }

  @Nested
  @Order(3)
  @WireMockTest(httpPort = 8776)
  class NestedRepeatingTheSamePort {
    @Test
    void runs_on_the_inherited_port(WireMockRuntimeInfo wmRuntimeInfo) {
      assertThat(wmRuntimeInfo.getHttpPort(), is(8776));
    }
  }
}
