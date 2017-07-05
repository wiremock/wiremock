/*
 * Copyright (C) 2017 Thomas Akehurst
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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.After;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertThat;

public class SnapshotAcceptanceTest extends AcceptanceTestBase {
    private WireMockServer proxyingService;
    private WireMockTestClient proxyingTestClient;

    public void init() {
        proxyingService = new WireMockServer(wireMockConfig()
            .dynamicPort()
            .withRootDirectory("src/test/resources/empty"));
        proxyingService.start();
        proxyingService.stubFor(proxyAllTo("http://localhost:" + wireMockServer.port()));

        proxyingTestClient = new WireMockTestClient(proxyingService.port());
        wireMockServer.stubFor(any(anyUrl()).willReturn(ok()));
    }

    @After
    public void proxyServerShutdown() {
        proxyingService.stop();
    }

    private static final String DEFAULT_SNAPSHOT_RESPONSE =
            "{                                                           \n" +
            "    \"mappings\": [                                         \n" +
            "        {                                                   \n" +
            "            \"request\" : {                                 \n" +
            "                \"url\" : \"/foo/bar/baz\",                 \n" +
            "                \"method\" : \"GET\"                        \n" +
            "            },                                              \n" +
            "            \"response\" : {                                \n" +
            "                \"status\" : 200                            \n" +
            "            }                                               \n" +
            "        },                                                  \n" +
            "        {                                                   \n" +
            "            \"request\" : {                                 \n" +
            "                \"url\" : \"/foo/bar\",                     \n" +
            "                \"method\" : \"GET\"                        \n" +
            "            },                                              \n" +
            "            \"response\" : {                                \n" +
            "                \"status\" : 200                            \n" +
            "            }                                               \n" +
            "        }                                                   \n" +
            "    ]                                                       \n" +
            "}                                                            ";

    @Test
    public void returnsRequestsWithDefaultOptions() throws Exception {
        proxyingTestClient.get("/foo/bar", withHeader("A", "B"));
        proxyingTestClient.get("/foo/bar/baz", withHeader("A", "B"));

        assertThat(
            proxyingTestClient.snapshot("{ \"persist\": false }"),
            equalToJson(DEFAULT_SNAPSHOT_RESPONSE, JSONCompareMode.STRICT_ORDER)
        );
    }

    private static final String FILTER_BY_REQUEST_PATTERN_SNAPSHOT_REQUEST =
            "{                                                 \n" +
            "    \"outputFormat\": \"full\",                   \n" +
            "    \"persist\": \"false\",                       \n" +
            "    \"filters\": {                                \n" +
            "        \"urlPattern\": \"/foo.*\",               \n" +
            "        \"headers\": {                            \n" +
            "            \"A\": { \"equalTo\": \"B\" }         \n" +
            "        }                                         \n" +
            "    }                                             \n" +
            "}                                                   ";

    private static final String FILTER_BY_REQUEST_PATTERN_SNAPSHOT_RESPONSE =
            "{                                                           \n" +
            "    \"mappings\": [                                         \n" +
            "        {                                                   \n" +
            "            \"request\" : {                                 \n" +
            "                \"url\" : \"/foo/bar/baz\",                 \n" +
            "                \"method\" : \"GET\"                        \n" +
            "            },                                              \n" +
            "            \"response\" : {                                \n" +
            "                \"status\" : 200                            \n" +
            "            }                                               \n" +
            "        },                                                  \n" +
            "        {                                                   \n" +
            "            \"request\" : {                                 \n" +
            "                \"url\" : \"/foo/bar\",                     \n" +
            "                \"method\" : \"GET\"                        \n" +
            "            },                                              \n" +
            "            \"response\" : {                                \n" +
            "                \"status\" : 200                            \n" +
            "            }                                               \n" +
            "        }                                                   \n" +
            "    ]                                                       \n" +
            "}                                                             ";

    @Test
    public void returnsFilteredRequestsWithJustRequestPatternsAndFullOutputFormat() throws Exception {
        // Matches both
        proxyingTestClient.get("/foo/bar", withHeader("A", "B"));
        // Fails header match
        proxyingTestClient.get("/foo");
        // Fails URL match
        proxyingTestClient.get("/bar", withHeader("A", "B"));
        // Fails header match
        proxyingTestClient.get("/foo/", withHeader("A", "C"));
        // Matches both
        proxyingTestClient.get("/foo/bar/baz", withHeader("A", "B"));

        assertThat(
            proxyingTestClient.snapshot(FILTER_BY_REQUEST_PATTERN_SNAPSHOT_REQUEST),
            equalToJson(FILTER_BY_REQUEST_PATTERN_SNAPSHOT_RESPONSE, JSONCompareMode.STRICT_ORDER)
        );
    }

    private static final String FILTER_BY_REQUEST_PATTERN_AND_IDS_SNAPSHOT_REQUEST_TEMPLATE =
            "{                                                     \n" +
            "    \"outputFormat\": \"full\",                       \n" +
            "    \"persist\": \"false\",                           \n" +
            "    \"filters\": {                                    \n" +
            "        \"ids\": [ %s, %s ],                          \n" +
            "        \"urlPattern\": \"/foo.*\"                    \n" +
            "    }                                                 \n" +
            "}                                                       ";

    private static final String FILTER_BY_REQUEST_PATTERN_AND_IDS_SNAPSHOT_RESPONSE =
            "{                                                       \n" +
            "    \"mappings\": [                                     \n" +
            "        {                                               \n" +
            "            \"request\" : {                             \n" +
            "                \"url\" : \"/foo/bar\",                 \n" +
            "                \"method\" : \"GET\"                    \n" +
            "            },                                          \n" +
            "            \"response\" : {                            \n" +
            "                \"status\" : 200                        \n" +
            "            }                                           \n" +
            "        }                                               \n" +
            "    ]                                                   \n" +
            "}                                                         ";

    @Test
    public void returnsFilteredRequestsWithRequestPatternAndIdsWithFullOutputFormat() {
        // Matches both
        proxyingTestClient.get("/foo/bar");
        // Fails URL match
        proxyingTestClient.get("/bar");
        // Fails ID match
        proxyingTestClient.get("/foo");

        String requestsJson = proxyingTestClient.get("/__admin/requests").content();
        JsonNode requestsNode = Json.node(requestsJson).path("requests");

        String request = String.format(FILTER_BY_REQUEST_PATTERN_AND_IDS_SNAPSHOT_REQUEST_TEMPLATE,
            requestsNode.get(2).get("id"),
            requestsNode.get(1).get("id")
        );

        assertThat(
            proxyingTestClient.snapshot(request),
            equalToJson(FILTER_BY_REQUEST_PATTERN_AND_IDS_SNAPSHOT_RESPONSE, JSONCompareMode.STRICT_ORDER)
        );
    }

    private static final String CAPTURE_HEADERS_SNAPSHOT_REQUEST =
            "{                                  \n" +
            "    \"outputFormat\": \"full\",    \n" +
            "    \"persist\": \"false\",        \n" +
            "    \"captureHeaders\": {          \n" +
            "        \"Accept\": {              \n" +
            "            \"anything\": true     \n" +
            "        },                         \n" +
            "        \"X-NoMatch\": {           \n" +
            "            \"equalTo\": \"!\"     \n" +
            "        }                          \n" +
            "    }                              \n" +
            "}                                    ";

    private static final String CAPTURE_HEADERS_SNAPSHOT_RESPONSE =
            "{                                                           \n" +
            "    \"mappings\": [                                         \n" +
            "        {                                                   \n" +
            "            \"request\" : {                                 \n" +
            "                \"url\" : \"/foo/bar\",                     \n" +
            "                \"method\" : \"PUT\",                       \n" +
            "                \"headers\": {                              \n" +
            "                    \"Accept\": {                           \n" +
            "                        \"equalTo\": \"B\"                  \n" +
            "                    }                                       \n" +
            "                }                                           \n" +
            "            },                                              \n" +
            "            \"response\" : {                                \n" +
            "                \"status\" : 200                            \n" +
            "            }                                               \n" +
            "        }                                                   \n" +
            "    ]                                                       \n" +
            "}                                                             ";

    @Test
    public void returnsStubMappingWithCapturedHeaders() {
        proxyingTestClient.put("/foo/bar",
            withHeader("A", "B"),
            withHeader("Accept", "B"),
            withHeader("X-NoMatch", "should be ignored")
        );

        String actual = proxyingTestClient.snapshot(CAPTURE_HEADERS_SNAPSHOT_REQUEST);
        assertThat(actual, equalToJson(CAPTURE_HEADERS_SNAPSHOT_RESPONSE, JSONCompareMode.STRICT_ORDER));
        assertFalse(actual.contains("X-NoMatch"));
    }

    private static final String REPEATS_AS_SCENARIOS_SNAPSHOT_REQUEST =
            "{                                                 \n" +
            "    \"outputFormat\": \"full\",                   \n" +
            "    \"persist\": \"false\",                       \n" +
            "    \"repeatsAsScenarios\": \"true\"              \n" +
            "}                                                   ";

    private static final String REPEATS_AS_SCENARIOS_SNAPSHOT_RESPONSE =
            "{                                                               \n" +
            "    \"mappings\": [                                             \n" +
            "        {                                                       \n" +
            "            \"scenarioName\" : \"scenario-bar-baz\",            \n" +
            "            \"requiredScenarioState\" : \"Started\",            \n" +
            "            \"request\" : {                                     \n" +
            "                \"url\" : \"/bar/baz\",                         \n" +
            "                \"method\" : \"GET\"                            \n" +
            "            }                                                   \n" +
            "        },                                                      \n" +
            "        {                                                       \n" +
            "            \"request\" : {                                     \n" +
            "                \"url\" : \"/foo\",                             \n" +
            "                \"method\" : \"GET\"                            \n" +
            "            }                                                   \n" +
            "        },                                                      \n" +
            "        {                                                       \n" +
            "            \"scenarioName\" : \"scenario-bar-baz\",            \n" +
            "            \"requiredScenarioState\" : \"Started\",            \n" +
            "            \"newScenarioState\" : \"scenario-bar-baz-2\",      \n" +
            "            \"request\" : {                                     \n" +
            "                \"url\" : \"/bar/baz\",                         \n" +
            "                \"method\" : \"GET\"                            \n" +
            "            }                                                   \n" +
            "        }                                                       \n" +
            "    ]                                                           \n" +
            "}                                                                 ";

    @Test
    public void returnsStubMappingsWithScenariosForRepeatedRequests() {
        proxyingTestClient.get("/bar/baz");
        proxyingTestClient.get("/foo");
        proxyingTestClient.get("/bar/baz");

        assertThat(
            proxyingTestClient.snapshot(REPEATS_AS_SCENARIOS_SNAPSHOT_REQUEST),
            equalToJson(REPEATS_AS_SCENARIOS_SNAPSHOT_RESPONSE, JSONCompareMode.STRICT_ORDER)
        );
    }
}
