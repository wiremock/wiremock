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
package ignored;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class ManyUnmatchedRequestsTest {

    @RegisterExtension
    public WireMockExtension wm = WireMockExtension.newInstance()
            .options(options().dynamicPort().withRootDirectory("src/main/resources/empty"))
            .failOnUnmatchedRequests(true)
            .build();

    WireMockTestClient client;

    @BeforeEach
    public void init() {
        client = new WireMockTestClient(wm.getPort());
    }

    @Test
    public void unmatched() {
        wm.stubFor(get(urlEqualTo("/hit")).willReturn(aResponse().withStatus(200)));

        client.get("/a-near-mis");
        client.get("/near-misssss");
        client.get("/hit");
    }
}
