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
package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.standalone.CommandLineOptions;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WireMockConfigurationTest {


    @Test
    public void testInitWithProxyAllAndNoPreservingHostHeaderUsesProxyUrlBasedHost() {
        CommandLineOptions options = new CommandLineOptions("--proxy-all", "http://localhost:8080");
        WireMockConfiguration.init(options);

        assertThat(WireMockConfiguration.getInstance().preserveHostHeader(), is(false));
        assertThat(WireMockConfiguration.getInstance().proxyUrl(), is("http://localhost:8080"));
        assertThat(WireMockConfiguration.getInstance().proxyUrlBasedHostHeader(), is("localhost"));
    }

    @Test
    public void testInitWithProxyAllAndPreservingHostHeaderDoesNotHoldTheProxyUrlBasedHost() {
        CommandLineOptions options = new CommandLineOptions("--proxy-all", "http://localhost:8080", "--preserve-host-header");
        WireMockConfiguration.init(options);

        assertThat(WireMockConfiguration.getInstance().preserveHostHeader(), is(true));
        assertThat(WireMockConfiguration.getInstance().proxyUrl(), is("http://localhost:8080"));
        assertThat(WireMockConfiguration.getInstance().proxyUrlBasedHostHeader(), is(nullValue()));
    }
}
