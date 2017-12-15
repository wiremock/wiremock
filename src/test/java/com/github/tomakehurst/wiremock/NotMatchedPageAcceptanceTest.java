
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

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.github.tomakehurst.wiremock.verification.notmatched.NotMatchedRenderer;
import org.junit.After;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.file;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalsMultiLine;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NotMatchedPageAcceptanceTest {

    WireMockServer wm;
    WireMockTestClient testClient;

    @After
    public void stop() {
        wm.stop();
    }

    @Test
    public void rendersAPlainTextDiffWhenStubNotMatchedAndANearMissIsAvailable() {
        configure();

        stubFor(post("/thing")
            .withName("The post stub with a really long name that ought to wrap and let us see exactly how that looks when it is done")
            .withHeader("X-My-Header", containing("correct value"))
            .withHeader("Accept", matching("text/plain.*"))
            .withRequestBody(equalToJson(
                "{                              \n" +
                "    \"thing\": {               \n" +
                "        \"stuff\": [1, 2, 3]   \n" +
                "    }                          \n" +
                "}"))
            .willReturn(ok()));

        WireMockResponse response = testClient.postJson(
        "/thin",
        "{                        \n" +
            "    \"thing\": {           \n" +
            "        \"nothing\": {}    \n" +
            "    }                      \n" +
            "}",
            withHeader("X-My-Header", "wrong value"),
            withHeader("Accept", "text/plain")
        );

        assertThat(response.content(), equalsMultiLine(file("not-found-diff-sample_ascii.txt")));
    }

    @Test
    public void rendersAPlainTextDiffWhenRequestIsOnlyUrlAndMethod() {
        configure();

        stubFor(get("/another-url")
            .withRequestBody(absent())
            .willReturn(ok()));

        WireMockResponse response = testClient.get("/gettable");

        assertThat(response.statusCode(), is(404));
    }

    @Test
    public void showsADefaultMessageWhenNoStubsWerePresent() {
        configure();

        WireMockResponse response = testClient.get("/no-stubs-to-match");

        assertThat(response.statusCode(), is(404));
        assertThat(response.firstHeader(CONTENT_TYPE), is("text/plain"));
        assertThat(response.content(), is("No response could be served as there are no stub mappings in this WireMock instance."));
    }

    @Test
    public void supportsCustomNoMatchRenderer() {
        configure(wireMockConfig().notMatchedRenderer(new NotMatchedRenderer() {
            @Override
            protected ResponseDefinition render(Admin admin, Request request) {
                return ResponseDefinitionBuilder.responseDefinition()
                    .withStatus(403)
                    .withBody("No you don't!")
                    .build();
            }
        }));

        WireMockResponse response = testClient.get("/should-not-match");

        assertThat(response.statusCode(), is(403));
        assertThat(response.content(), is("No you don't!"));
    }

    private void configure() {
        configure(WireMockConfiguration.wireMockConfig());
    }

    private void configure(WireMockConfiguration options) {
        options
            .dynamicPort()
            .withRootDirectory("src/test/resources/empty");
        wm = new WireMockServer(options);
        wm.start();
        testClient = new WireMockTestClient(wm.port());
        WireMock.configureFor(wm.port());
    }

}
