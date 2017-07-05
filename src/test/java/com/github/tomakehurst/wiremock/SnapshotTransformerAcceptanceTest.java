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

import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.testsupport.GlobalStubMappingTransformer;
import com.github.tomakehurst.wiremock.testsupport.NonGlobalStubMappingTransformer;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.After;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static org.junit.Assert.assertThat;

public class SnapshotTransformerAcceptanceTest extends AcceptanceTestBase {
    private WireMockServer proxyingService;
    private WireMockTestClient proxyingTestClient;

    public void proxyServerStart(Class<? extends StubMappingTransformer> ... extensions) {
        proxyingService = new WireMockServer(wireMockConfig()
            .dynamicPort()
            .withRootDirectory("src/test/resources/empty")
            .extensions(extensions)
        );
        proxyingService.start();
        proxyingService.stubFor(proxyAllTo("http://localhost:" + wireMockServer.port()));

        proxyingTestClient = new WireMockTestClient(proxyingService.port());
        wireMockServer.stubFor(any(anyUrl()).willReturn(ok()));
    }

    @After
    public void proxyServerShutdown() {
        proxyingService.stop();
    }

    private static final String GLOBAL_TRANSFORMED_STUB_MAPPING_REQUEST =
            "{                                  \n" +
            "    \"outputFormat\": \"full\",    \n" +
            "    \"persist\": \"false\"         \n" +
            "}                                    ";

    private static final String GLOBAL_TRANSFORMED_STUB_MAPPING_RESPONSE =
            "{                                                           \n" +
            "    \"mappings\": [                                         \n" +
            "        {                                                   \n" +
            "            \"request\" : {                                 \n" +
            "                \"url\" : \"/foo?transformed=global\",      \n" +
            "                \"method\" : \"GET\"                        \n" +
            "            },                                              \n" +
            "            \"response\" : {                                \n" +
            "                \"status\" : 200                            \n" +
            "            }                                               \n" +
            "        },                                                  \n" +
            "        {                                                   \n" +
            "            \"request\" : {                                 \n" +
            "                \"url\" : \"/?transformed=global\",         \n" +
            "                \"method\" : \"GET\"                        \n" +
            "            },                                              \n" +
            "            \"response\" : {                                \n" +
            "                \"status\" : 200                            \n" +
            "            }                                               \n" +
            "        }                                                   \n" +
            "    ]                                                       \n" +
            "}                                                             ";

    @Test
    public void returnsTransformedStubMappingWithGlobalTransformer() {
        proxyServerStart(
            GlobalStubMappingTransformer.class,
            NonGlobalStubMappingTransformer.class // should ignore this one
        );

        proxyingTestClient.get("/");
        proxyingTestClient.get("/foo");

        assertThat(
            proxyingTestClient.snapshot(GLOBAL_TRANSFORMED_STUB_MAPPING_REQUEST),
            equalToJson(GLOBAL_TRANSFORMED_STUB_MAPPING_RESPONSE, JSONCompareMode.STRICT_ORDER)
        );
    }

    private static final String NONGLOBAL_TRANSFORMED_STUB_MAPPING_REQUEST =
            "{                                    \n" +
            "    \"outputFormat\": \"full\",      \n" +
            "    \"persist\": \"false\",          \n" +
            "    \"transformers\": [              \n" +
            "       \"nonglobal-transformer\"     \n" +
            "    ]                                \n" +
            "}                                      ";

    private static final String NONGLOBAL_TRANSFORMED_STUB_MAPPING_RESPONSE =
            "{                                                           \n" +
            "    \"mappings\": [                                         \n" +
            "        {                                                   \n" +
            "            \"request\" : {                                 \n" +
            "                \"url\" : \"/foo?transformed=nonglobal\",   \n" +
            "                \"method\" : \"GET\",                       \n" +
            "                \"headers\": {                              \n" +
            "                    \"Accept\": {                           \n" +
            "                        \"equalTo\": \"B\"                  \n" +
            "                    }                                       \n" +
            "                }                                           \n" +
            "            },                                              \n" +
            "            \"response\" : {                                \n" +
            "                \"status\" : 200                            \n" +
            "            }                                               \n" +
            "        },                                                  \n" +
            "        {                                                   \n" +
            "            \"request\" : {                                 \n" +
            "                \"url\" : \"/?transformed=nonglobal\",      \n" +
            "                \"method\" : \"GET\",                       \n" +
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
    public void returnsTransformedStubMappingWithNonGlobalTransformer() {
        proxyServerStart(NonGlobalStubMappingTransformer.class);

        proxyingTestClient.get("/");
        proxyingTestClient.get("/foo");

        assertThat(
            proxyingTestClient.snapshot(NONGLOBAL_TRANSFORMED_STUB_MAPPING_REQUEST),
            equalToJson(NONGLOBAL_TRANSFORMED_STUB_MAPPING_RESPONSE, JSONCompareMode.STRICT_ORDER)
        );
    }
}
