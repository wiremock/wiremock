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
package com.github.tomakehurst.wiremock.servlet;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AlternativeServletContainerTest {

    @Rule
    public WireMockRule wm = new WireMockRule(options().httpServerFactory(new AltHttpServerFactory()));
    WireMockTestClient client;

    @Before
    public void init() {
        client = new WireMockTestClient(wm.port());
        WireMock.configureFor(wm.port());
    }

    @Test
    public void supportsAlternativeHttpServerForBasicStub() {
        stubFor(get(urlEqualTo("/alt-server")).willReturn(aResponse().withStatus(204)));

        assertThat(client.get("/alt-server").statusCode(), is(204));
    }

    @Test
    public void supportsAlternativeHttpServerForFaultInjection() {
        stubFor(get(urlEqualTo("/alt-server")).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        WireMockResponse response = client.get("/alt-server");

        assertThat(response.statusCode(), is(418));
        assertThat(response.content(), is("No fault injector is configured!"));
    }
}
