/*
 * Copyright (C) 2011 Thomas Akehurst
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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@WireMockTest(httpPort = 8765)
class JunitJupiterExtensionDeclarativeWithConfiguredNestedTest {
    @Test
    void runs_on_the_supplied_port(WireMockRuntimeInfo wmRuntimeInfo) {
        assertThat(wmRuntimeInfo.getHttpPort(), is(8765));
    }

    @Nested
    @WireMockTest(httpPort = 8766)
    class RunsOn8766 {
        @Test
        void runs_on_the_supplied_port(WireMockRuntimeInfo wmRuntimeInfo) {
            assertThat(wmRuntimeInfo.getHttpPort(), is(8766));
        }
    }

    @Nested
    @WireMockTest(httpPort = 8767)
    class RunsOn8767 {
        @Test
        void runs_on_the_supplied_port(WireMockRuntimeInfo wmRuntimeInfo) {
            assertThat(wmRuntimeInfo.getHttpPort(), is(8767));
        }
    }

    @Nested
    class RunsOnInheritedPort {
        @Test
        void runs_on_the_supplied_port(WireMockRuntimeInfo wmRuntimeInfo) {
            assertThat(wmRuntimeInfo.getHttpPort(), is(8765));
        }
    }
}
