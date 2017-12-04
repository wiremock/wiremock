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

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.client.WireMockBuilder;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcher;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CustomMatchingAcceptanceTest {

    @SuppressWarnings("unchecked")
    @Rule
    public WireMockRule wmRule = new WireMockRule(options()
        .dynamicPort()
        .extensions(MyExtensionRequestMatcher.class),
        false);

    WireMockTestClient client;
    WireMock wm;

    @Before
    public void init() {
        client = new WireMockTestClient(wmRule.port());
        wm = WireMock.create().port(wmRule.port()).build();
    }

    @Test
    public void inlineRequestMatcherExtension() {
        wmRule.stubFor(requestMatching(new MyRequestMatcher()).willReturn(aResponse().withStatus(200)));
        assertThat(client.get("/correct").statusCode(), is(200));
        assertThat(client.get("/wrong").statusCode(), is(404));
    }

    @Test
    public void inlineRequestMatcher() {
        wmRule.stubFor(requestMatching(new RequestMatcher() {
            @Override
            public MatchResult match(Request request) {
                return MatchResult.of(request.getUrl().contains("correct"));
            }

            @Override
            public String getName() {
                return "inline";
            }
        }).willReturn(aResponse().withStatus(200)));

        assertThat(client.get("/correct").statusCode(), is(200));
        assertThat(client.get("/wrong").statusCode(), is(404));
    }

    @Test
    public void requestMatcherAsExtension() {
        wm.register(requestMatching("path-contains-param", Parameters.one("path", "findthis")).willReturn(aResponse().withStatus(200)));
        assertThat(client.get("/findthis/thing").statusCode(), is(200));
    }

    public static class MyRequestMatcher extends RequestMatcherExtension {

        @Override
        public MatchResult match(Request request, Parameters parameters) {
            return MatchResult.of(request.getUrl().contains("correct"));
        }
    }

    public static class MyExtensionRequestMatcher extends RequestMatcherExtension {

        @Override
        public MatchResult match(Request request, Parameters parameters) {
            String pathSegment = parameters.getString("path");
            return MatchResult.of(request.getUrl().contains(pathSegment));
        }

        @Override
        public String getName() {
            return "path-contains-param";
        }

    }
}
