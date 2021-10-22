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

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.testsupport.TestNotifier;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import ignored.ManyUnmatchedRequestsTest;
import ignored.SingleUnmatchedRequestTest;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.verification.diff.JUnitStyleDiffRenderer.junitStyleDiffMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class NearMissesRuleAcceptanceTest {

    @Nested
    class NearMissesRuleTest {

        private TestNotifier testNotifier = new TestNotifier();

        @RegisterExtension
        public WireMockExtension wm = WireMockExtension.newInstance().options(options()
                .dynamicPort()
                .notifier(testNotifier)
                .withRootDirectory("src/main/resources/empty"))
        .build();

        WireMockTestClient client;

        @BeforeEach
        public void init() {
            client = new WireMockTestClient(wm.getRuntimeInfo().getHttpPort());
            testNotifier.reset();
        }

        @Test
        public void logsUnmatchedRequestsAtErrorWithNearMisses() throws Exception {
            wm.stubFor(get(urlEqualTo("/near-miss")).willReturn(aResponse().withStatus(200)));
            wm.stubFor(get(urlEqualTo("/miss")).willReturn(aResponse().withStatus(200)));

            client.post("/a-near-mis", new StringEntity(""));

            assertThat(testNotifier.getErrorMessages(), hasItem(allOf(
                containsString("Request was not matched"),
                containsString("/a-near-mis"),
                containsString("/near-miss"),
                containsString("HTTP method does not match"),
                containsString("URL does not match")
                )
            ));
        }

        @Test
        public void throwsVerificationExceptionIfSomeRequestsWentUnmatched() {
            String message = runTestAndGetMessage(ManyUnmatchedRequestsTest.class);

            assertThat(message, containsString("2 requests were unmatched by any stub mapping"));
            assertThat(message,
                containsString(junitStyleDiffMessage(
                    "GET\n/hit\n",
                    "GET\n/near-misssss\n"
                ))
            );
            assertThat(message,
                containsString(junitStyleDiffMessage(
                    "GET\n/hit\n",
                    "GET\n/a-near-mis\n"
                ))
            );
        }

        @Test
        public void throwsVerificationExceptionIfASingleRequestWentUnmatched() {
            String message = runTestAndGetMessage(SingleUnmatchedRequestTest.class);
            assertThat(message, containsString("A request was unmatched by any stub mapping. Closest stub mapping was:"));
            assertThat(message,
                containsString(junitStyleDiffMessage(
                    "GET\n/hit\n",
                    "GET\n/near-misssss\n"
                ))
            );
        }

        @Test
        public void shouldFindNearMatch() {
            Throwable exception = assertThrows(VerificationException.class, () -> {

                client.get("/123");

                wm.verify(getRequestedFor(urlPathEqualTo("/")));
            });
            assertTrue(exception.getMessage().contains("No requests exactly matched. Most similar request was:"));
        }

        private String runTestAndGetMessage(Class<?> testClass) {
            final AtomicReference<String> message = new AtomicReference<>("");

            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(selectClass(testClass))
                    .build();
            Launcher launcher = LauncherFactory.create();
            launcher.registerTestExecutionListeners(new TestExecutionListener() {
                @Override
                public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
                    testExecutionResult.getThrowable()
                            .map(Throwable::getMessage)
                            .ifPresent(message::set);
                }
            });
            launcher.execute(request);

            return message.get();
        }
    }

    @Nested
    class CustomMatcherWithNearMissesTest {

        @RegisterExtension
        public WireMockExtension wmeWithCustomMatcher = WireMockExtension.newInstance().options(options()
                .dynamicPort()
                .withRootDirectory("src/main/resources/empty")
                .extensions(new RequestMatcherExtension() {
                    @Override
                    public MatchResult match(Request request, Parameters parameters) {
                        return MatchResult.partialMatch(0.5);
                    }

                    @Override
                    public String getName() {
                        return "always-match";
                    }
                })).build();

        WireMockTestClient client;

        @BeforeEach
        public void init() {
            client = new WireMockTestClient(wmeWithCustomMatcher.getRuntimeInfo().getHttpPort());
        }

        @Test
        public void successfullyCalculatesNearMissesWhenACustomMatcherIsRegistered() {
            wmeWithCustomMatcher.stubFor(requestMatching("always-match").willReturn(aResponse()));

            client.get("/");

            assertThat(wmeWithCustomMatcher.findNearMissesForAllUnmatchedRequests().size(), is(1));
        }

    }
}
