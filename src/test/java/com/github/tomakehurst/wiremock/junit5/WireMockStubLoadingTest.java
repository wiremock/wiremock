/*
 * Copyright (C) 2024 Thomas Akehurst
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.Test;

@WireMockTest
class WireMockStubLoadingTest {

    @Test
    @WireMockStub("/wiremock/stubs/simple-get.json")
    void shouldReturnExpectedResponseWhenStubMatches(WireMockRuntimeInfo runtimeInfo) {
        // Given
        WireMockTestClient client = new WireMockTestClient(runtimeInfo.getHttpPort());

        // When
        WireMockResponse response = client.get("/test");

        // Then
        assertThat(response.statusCode(), is(200));
        assertThat(response.content(), is("Hello World"));
    }

    @Test
    @WireMockStub("/wiremock/stubs/multiple-stubs.json")
    void shouldHandleMultipleEndpoints(WireMockRuntimeInfo runtimeInfo) {
        // Given
        WireMockTestClient client = new WireMockTestClient(runtimeInfo.getHttpPort());

        // When/Then
        WireMockResponse response1 = client.get("/test1");
        assertThat(response1.statusCode(), is(200));
        assertThat(response1.content(), is("Test 1"));

        WireMockResponse response2 = client.get("/test2");
        assertThat(response2.statusCode(), is(200));
        assertThat(response2.content(), is("Test 2"));
    }

    @Test
    @WireMockStub("/wiremock/stubs/simple-get.json")
    void shouldReturn404WhenUrlDoesNotMatch(WireMockRuntimeInfo runtimeInfo) {
        // Given
        WireMockTestClient client = new WireMockTestClient(runtimeInfo.getHttpPort());

        // When
        WireMockResponse response = client.get("/non-existent");

        // Then
        assertThat(response.statusCode(), is(404));
    }

    @Test
    @WireMockStub("/wiremock/stubs/simple-get.json")
    void shouldAllowVerificationOfRequests(WireMockRuntimeInfo runtimeInfo) {
        // Given
        WireMockTestClient client = new WireMockTestClient(runtimeInfo.getHttpPort());

        // When
        client.get("/test");

        // Then
        verify(getRequestedFor(urlEqualTo("/test")));
    }
}