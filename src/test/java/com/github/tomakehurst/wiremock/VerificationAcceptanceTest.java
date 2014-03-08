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
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.RequestJournalDisabledException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;

@RunWith(Enclosed.class)
public class VerificationAcceptanceTest {

    public static class JournalEnabled extends AcceptanceTestBase {

        @Test
        public void verifiesRequestBasedOnUrlOnly() {
            testClient.get("/this/got/requested");
            verify(getRequestedFor(urlEqualTo("/this/got/requested")));
        }

        @Test(expected=VerificationException.class)
        public void throwsVerificationExceptionWhenNoMatch() {
            testClient.get("/this/got/requested");
            verify(getRequestedFor(urlEqualTo("/this/did/not")));
        }

        @Test
        public void verifiesWithHeaders() {
            testClient.put("/update/this", withHeader("Content-Type", "application/json"), withHeader("Encoding", "UTF-8"));
            verify(putRequestedFor(urlMatching("/[a-z]+/this"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Encoding", notMatching("LATIN-1")));
        }

        @Test
        public void verifiesWithMultiValueHeaders() {
            testClient.get("/multi/value/header",
                    withHeader("X-Thing", "One"),
                    withHeader("X-Thing", "Two"),
                    withHeader("X-Thing", "Three"));

            verify(getRequestedFor(urlEqualTo("/multi/value/header"))
                .withHeader("X-Thing", equalTo("Two"))
                .withHeader("X-Thing", matching("Thr.*")));

            verify(getRequestedFor(urlEqualTo("/multi/value/header"))
                    .withHeader("X-Thing", equalTo("Three")));
        }

        @Test(expected=VerificationException.class)
        public void throwsVerificationExceptionWhenHeadersDoNotMatch() {
            testClient.put("/to/modify", withHeader("Content-Type", "application/json"), withHeader("Encoding", "LATIN-1"));
            verify(putRequestedFor(urlEqualTo("/to/modify"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Encoding", notMatching("LATIN-1")));
        }

        private static final String SAMPLE_JSON =
            "{ 													\n" +
            "	\"thing\": {									\n" +
            "		\"importantKey\": \"Important value\",		\n" +
            "	}												\n" +
            "}													";


        @Test
        public void verifiesWithBody() {
            testClient.postWithBody("/add/this", SAMPLE_JSON, "application/json", "utf-8");
            verify(postRequestedFor(urlEqualTo("/add/this"))
                    .withRequestBody(matching(".*\"importantKey\": \"Important value\".*")));
        }

        @Test
        public void verifiesWithBodyContainingJson() {
            testClient.postWithBody("/body/contains", SAMPLE_JSON, "application/json", "utf-8");
            verify(postRequestedFor(urlEqualTo("/body/contains"))
                    .withRequestBody(matchingJsonPath("$.thing"))
                    .withRequestBody(matchingJsonPath("$..*[?(@.importantKey == 'Important value')]")));
        }

        @Test
        public void verifiesWithBodyEquallingJson() {
            testClient.postWithBody("/body/json", SAMPLE_JSON, "application/json", "utf-8");
            verify(postRequestedFor(urlEqualTo("/body/json"))
                    .withRequestBody(equalToJson(SAMPLE_JSON)));
        }

        @Test
        public void verifiesWithBodyEquallingJsonWithCompareMode() {
            testClient.postWithBody("/body/json/lenient", "{ \"message\": \"Hello\", \"key\": \"welcome.message\" }", "application/json", "utf-8");
            verify(postRequestedFor(urlEqualTo("/body/json/lenient"))
                    .withRequestBody(equalToJson("{ \"message\": \"Hello\" }", LENIENT)));
        }

        @Test
        public void verifiesWithBodyEquallingXml() {
            testClient.postWithBody("/body/xml", "<thing><subThing>The stuff</subThing></thing>", "application/xml", "utf-8");
            verify(postRequestedFor(urlEqualTo("/body/xml"))
                    .withRequestBody(equalToXml("<thing>     <subThing>The stuff\n</subThing>\n\n    </thing>")));
        }

        @Test
        public void verifiesWithBodyContainingString() {
            testClient.postWithBody("/body/json", SAMPLE_JSON, "application/json", "utf-8");
            verify(postRequestedFor(urlEqualTo("/body/json"))
                    .withRequestBody(containing("Important value")));
        }

        @Test(expected=VerificationException.class)
        public void resetErasesCounters() {
            testClient.get("/count/this");
            testClient.get("/count/this");
            testClient.get("/count/this");

            WireMock.reset();

            verify(getRequestedFor(urlEqualTo("/count/this")));
        }

        @Test
        public void verifiesArbitraryRequestCount() {
            testClient.get("/add/to/count");
            testClient.get("/add/to/count");
            testClient.get("/add/to/count");
            testClient.get("/add/to/count");

            verify(4, getRequestedFor(urlEqualTo("/add/to/count")));
        }

        @Test
        public void verifiesHeaderAbsent() {
            testClient.get("/without/header", withHeader("Content-Type", "application/json"));
            verify(getRequestedFor(urlEqualTo("/without/header"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withoutHeader("Accept"));
        }

        @Test(expected=VerificationException.class)
        public void failsVerificationWhenAbsentHeaderPresent() {
            testClient.get("/without/another/header", withHeader("Content-Type", "application/json"));
            verify(getRequestedFor(urlEqualTo("/without/another/header"))
                    .withoutHeader("Content-Type"));
        }

        @Test
        @SuppressWarnings("unchecked")
        public void showsExpectedAndReceivedRequestsOnVerificationException() {
            testClient.put("/some/request", withHeader("X-My-Stuff", "things"));

            try {
                verify(getRequestedFor(urlEqualTo("/specific/thing")));
                fail();
            } catch (VerificationException e) {
                assertThat(e.getMessage(), allOf(
                        containsString("Expected at least one request matching: {"),
                        containsString("/specific/thing"),
                        containsString("Requests received: "),
                        containsString("/some/request")));
            }
        }

        @Test
        @SuppressWarnings("unchecked")
        public void showsReceivedRequestsOnVerificationException() {
            testClient.put("/some/request", withHeader("X-My-Stuff", "things"));

            try {
                verify(14, getRequestedFor(urlEqualTo("/specific/thing")));
                fail();
            } catch (VerificationException e) {
                assertThat(e.getMessage(), allOf(
                        containsString("Expected exactly 14 requests matching: {"),
                        containsString("/specific/thing"),
                        containsString("Requests received: "),
                        containsString("/some/request")));
            }
        }

        @Test
        public void verifiesPatchRequests() {
            testClient.patchWithBody("/patch/this", SAMPLE_JSON, "application/json");
            verify(patchRequestedFor(urlEqualTo("/patch/this"))
                    .withRequestBody(matching(".*\"importantKey\": \"Important value\".*")));
        }
    }

    public static class JournalDisabled {

        @Rule
        public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().disableRequestJournal());

        @Test(expected=RequestJournalDisabledException.class)
        public void verifyThrowsExceptionWhenVerificationAttemptedAndRequestJournalDisabled() {
            verify(getRequestedFor(urlEqualTo("/whatever")));
        }

        @Test(expected=RequestJournalDisabledException.class)
        public void findAllThrowsExceptionWhenVerificationAttemptedAndRequestJournalDisabled() {
            findAll(getRequestedFor(urlEqualTo("/whatever")));
        }
    }
}
