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
package com.github.tomakehurst.wiremock.pool;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.Before;
import org.junit.Test;

import java.net.ServerSocket;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.opts;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WireMockServerPoolTest {

    @Before
    public void init() {
        WireMockServerPool.reset();
    }

    @Test
    public void returnsANewCorrectlyConfiguredServerInitially() throws Exception {
        int port = randomPort();

        WireMockServer server = WireMockServerPool.checkOut(wireMockConfig().port(port));
        server.start();

        assertThat(server.port(), is(port));
    }

    @Test
    public void returnsAnExistingInstanceWhenOptionsAreTheSame() throws Exception {
        int port = randomPort();

        WireMockServer server1 = WireMockServerPool.checkOut(opts().port(port));
        WireMockServerPool.checkIn(server1);

        WireMockServer server2 = WireMockServerPool.checkOut(opts().port(port));
        assertThat(server2, sameInstance(server1));
    }

    @Test
    public void returnsANewInstanceWhenOptionsDiffer() throws Exception {
        int port1 = randomPort();
        int port2 = randomPort();

        WireMockServer server1 = WireMockServerPool.checkOut(opts().port(port1));
        WireMockServerPool.checkIn(server1);

        WireMockServer server2 = WireMockServerPool.checkOut(opts().port(port2));
        assertThat(server2.port(), is(port2));
        assertThat(server2, not(sameInstance(server1)));
    }

    int randomPort() throws Exception {
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        return port;
    }

}
