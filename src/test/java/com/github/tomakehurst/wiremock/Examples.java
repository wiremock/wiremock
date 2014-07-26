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
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Ignore("Run when validating documentation")
public class Examples extends AcceptanceTestBase {

    @Test
    public void exactUrlOnly() {
        stubFor(get(urlEqualTo("/some/thing"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Hello world!")));

        assertThat(testClient.get("/some/thing").statusCode(), is(200));
        assertThat(testClient.get("/some/thing/else").statusCode(), is(404));
    }

    @Test
    public void urlRegexMatch() {
        stubFor(put(urlMatching("/thing/matching/[0-9]+"))
                .willReturn(aResponse().withStatus(200)));
    }

    @Test
    public void headerMatching() {
        stubFor(post(urlEqualTo("/with/headers"))
                .withHeader("Content-Type", equalTo("text/xml"))
                .withHeader("Accept", matching("text/.*"))
                .withHeader("etag", notMatching("abcd.*"))
                .withHeader("etag", containing("2134"))
                .willReturn(aResponse().withStatus(200)));
    }

    @Test
    public void bodyMatching() {
        stubFor(post(urlEqualTo("/with/body"))
                .withRequestBody(matching("<status>OK</status>"))
                .withRequestBody(notMatching("<status>ERROR</status>"))
                .willReturn(aResponse().withStatus(200)));
    }

    @Test
    public void priorities() {

        //Catch-all case
        stubFor(get(urlMatching("/api/.*")).atPriority(5)
                .willReturn(aResponse().withStatus(401)));

        //Specific case
        stubFor(get(urlEqualTo("/api/specific-resource")).atPriority(1) //1 is highest
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Resource state")));
    }

    @Test
    public void responseHeaders() {
        stubFor(get(urlEqualTo("/whatever"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Etag", "b13894794wb")));
    }

    @Test
    public void bodyFile() {
        stubFor(get(urlEqualTo("/body-file"))
                .willReturn(aResponse()
                        .withBodyFile("path/to/myfile.xml")));
    }

    @Test
    public void binaryBody() {
        stubFor(get(urlEqualTo("/binary-body"))
                .willReturn(aResponse()
                        .withBody(new byte[] { 1, 2, 3, 4 })));
    }

    @Test(expected=VerificationException.class)
    public void verifyAtLeastOnce() {
        verify(postRequestedFor(urlEqualTo("/verify/this"))
                .withHeader("Content-Type", equalTo("text/xml")));

        verify(3, postRequestedFor(urlEqualTo("/3/of/these")));
    }

    @Test(expected=VerificationException.class)
    public void verifyWithoutHeader() {
        verify(putRequestedFor(urlEqualTo("/without/header")).withoutHeader("Content-Type"));
    }

    @Test
    public void findingRequests() {
        List<LoggedRequest> requests = findAll(putRequestedFor(urlMatching("/api/.*")));
    }

    @Test
    public void proxying() {
        stubFor(get(urlMatching("/other/service/.*"))
                .willReturn(aResponse().proxiedFrom("http://otherhost.com/approot")));
    }

    @Test
    public void proxyIntercept() {
        // Low priority catch-all proxies to otherhost.com by default
        stubFor(get(urlMatching(".*")).atPriority(10)
                .willReturn(aResponse().proxiedFrom("http://otherhost.com")));


        // High priority stub will send a Service Unavailable response
        // if the specified URL is requested
        stubFor(get(urlEqualTo("/api/override/123")).atPriority(1)
                .willReturn(aResponse().withStatus(503)));
    }

    @Test
    public void toDoListScenario() {
        stubFor(get(urlEqualTo("/todo/items")).inScenario("To do list")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withBody("<items>" +
                                "   <item>Buy milk</item>" +
                                "</items>")));

        stubFor(post(urlEqualTo("/todo/items")).inScenario("To do list")
                .whenScenarioStateIs(STARTED)
                .withRequestBody(containing("Cancel newspaper subscription"))
                .willReturn(aResponse().withStatus(201))
                .willSetStateTo("Cancel newspaper item added"));

        stubFor(get(urlEqualTo("/todo/items")).inScenario("To do list")
                .whenScenarioStateIs("Cancel newspaper item added")
                .willReturn(aResponse()
                        .withBody("<items>" +
                                "   <item>Buy milk</item>" +
                                "   <item>Cancel newspaper subscription</item>" +
                                "</items>")));

        WireMockResponse response = testClient.get("/todo/items");
        assertThat(response.content(), containsString("Buy milk"));
        assertThat(response.content(), not(containsString("Cancel newspaper subscription")));

        response = testClient.postWithBody("/todo/items", "Cancel newspaper subscription", "text/plain", "UTF-8");
        assertThat(response.statusCode(), is(201));

        response = testClient.get("/todo/items");
        assertThat(response.content(), containsString("Buy milk"));
        assertThat(response.content(), containsString("Cancel newspaper subscription"));
    }

    @Test
    public void delay() {
        stubFor(get(urlEqualTo("/delayed")).willReturn(
                aResponse()
                        .withStatus(200)
                        .withFixedDelay(2000)));
    }

    @Test
    public void fault() {
        stubFor(get(urlEqualTo("/fault"))
                .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));
    }

    @Test
    public void xpath() {
        stubFor(put(urlEqualTo("/xpath"))
            .withRequestBody(matchingXPath("/todo-list[count(todo-item) = 3]"))
            .willReturn(aResponse().withStatus(200)));
    }

}
