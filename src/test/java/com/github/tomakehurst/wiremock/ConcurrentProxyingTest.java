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
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.defaultTestFilesRoot;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConcurrentProxyingTest {

    @RegisterExtension
    public WireMockExtension wm = WireMockExtension.newInstance().options(options().dynamicPort()).failOnUnmatchedRequests(false).build();

    @RegisterExtension
    public WireMockExtension target = WireMockExtension.newInstance().options(options().dynamicPort().usingFilesUnderDirectory(defaultTestFilesRoot())).failOnUnmatchedRequests(false).build();

    private WireMockTestClient client;

    @Test
    public void concurrent() throws Exception {
        client = new WireMockTestClient(wm.getRuntimeInfo().getHttpPort());

        wm.stubFor(any(anyUrl())
            .atPriority(10)
            .willReturn(aResponse()
                .proxiedFrom(target.baseUrl())));

        ExecutorService executor = Executors.newFixedThreadPool(20);

        List<Future<?>> results = newArrayList();
        for (int i = 0; i < 100; i++) {
            results.add(executor.submit(new Runnable() {
                @Override
                public void run() {
                    assertThat(client.get("/plain-example1.txt").content(), is("Example 1"));
                    assertThat(client.get("/plain-example2.txt").content(), is("Example 2"));
                    assertThat(client.get("/plain-example3.txt").content(), is("Example 3"));
                    assertThat(client.get("/plain-example4.txt").content(), is("Example 4"));
                    assertThat(client.get("/plain-example5.txt").content(), is("Example 5"));
                }
            }));
        }

        for (Future<?> result: results) {
            result.get();
        }
    }
}
