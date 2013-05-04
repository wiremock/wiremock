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

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.hasExactly;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.withUrl;
import static org.junit.Assert.assertThat;

public class JournalCapacityTest {

    private WireMockServer server;
    private WireMockTestClient testClient;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldGetLastRequestsWhenRequestQueryWithBoundedJournal() {
        setupJournalWithCapacity(3);
        try {
            generateRequests();
            assertThat(getAllRequests(), hasExactly(withUrl("/use/2"), withUrl("/use/3"), withUrl("/use/4")));
        } finally {
            server.stop();
        }
    }

    private void generateRequests() {
        testClient.get("/use/1");
        testClient.get("/use/2");
        testClient.get("/use/3");
        testClient.get("/use/4");
    }

    private List<LoggedRequest> getAllRequests() {
        return findAll(getRequestedFor(urlMatching("/.*")));
    }

    private void setupJournalWithCapacity(Integer capacity) {
        server = new WireMockServer(new WireMockConfiguration().journalCapacity(capacity));
        server.start();
        testClient = new WireMockTestClient();
    }

    @Test(expected = VerificationException.class)
    public void shouldFailWhenRequestQueryWithDisabledJournal() {
        setupJournalWithCapacity(0);
        try {
            generateRequests();
            getAllRequests();
        } finally {
            server.stop();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldGetAllRequestsWhenRequestQueryWithUnboundedJournal() {
        setupJournalWithCapacity(null);
        try {
            generateRequests();
            assertThat(getAllRequests(), hasExactly(withUrl("/use/1"), withUrl("/use/2"), withUrl("/use/3"), withUrl("/use/4")));
        } finally {
            server.stop();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenRequestQueryWithNegativeCapacityJournal() {
        setupJournalWithCapacity(-1);
    }

}
