/* Copyright (C) 2013 Roger Abelenda
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

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.hasExactly;
import static org.junit.Assert.assertThat;

public class JournalCapacityTest {

    @Test
    public void shouldGetLastRequestsWhenRequestQueryWithLimitedJournal() {
        WireMockServer server = new WireMockServer(new WireMockConfiguration().journalCapacity(3));
        server.start();
        WireMockTestClient testClient = new WireMockTestClient();

        testClient.get("/use/1");
        testClient.get("/use/2");
        testClient.get("/use/3");
        testClient.get("/use/4");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));

        assertThat(requests, hasExactly(withUrl("/use/2"), withUrl("/use/3"), withUrl("/use/4")));

        server.stop();
    }

    private LoggedRequestWithUrlMatcher withUrl(String url) {
          return new LoggedRequestWithUrlMatcher(url);
    }

}
