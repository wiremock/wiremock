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

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.http.HttpClient4Factory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class JUnitJupiterExtensionFailOnUnmatchedTest {

    CloseableHttpClient client;
    ExtensionContext extensionContext;

    @BeforeEach
    void init() {
        client = HttpClient4Factory.createClient();

        extensionContext = Mockito.mock(ExtensionContext.class);
        when(extensionContext.getElement()).thenReturn(Optional.empty());
    }

    @Test
    void throws_a_verification_exception_when_an_unmatched_request_is_made_during_the_test() throws Exception {
        WireMockExtension extension = WireMockExtension.newInstance()
                .failOnUnmatchedRequests(true)
                .build();

        extension.beforeEach(extensionContext);

        extension.stubFor(get("/found").willReturn(ok()));

        try (CloseableHttpResponse response = client.execute(new HttpGet(extension.url("/not-found")))) {
            assertThat(response.getStatusLine().getStatusCode(), is(404));
        }

        assertThrows(VerificationException.class, () -> extension.afterEach(extensionContext));
    }

    @Test
    void does_not_throw_a_verification_exception_when_fail_on_unmatched_disabled() throws Exception {
        WireMockExtension extension = WireMockExtension.newInstance()
                .failOnUnmatchedRequests(false)
                .build();

        extension.beforeEach(extensionContext);

        extension.stubFor(get("/found").willReturn(ok()));

        try (CloseableHttpResponse response = client.execute(new HttpGet(extension.url("/not-found")))) {
            assertThat(response.getStatusLine().getStatusCode(), is(404));
        }

        assertDoesNotThrow(() -> extension.afterEach(extensionContext));
    }


}
