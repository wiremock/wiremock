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

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public class ResponseTemplatingAcceptanceTest {

    public static class Local {

        WireMockTestClient client;

        @Rule
        public WireMockRule wm = new WireMockRule(options()
            .dynamicPort()
            .extensions(new ResponseTemplateTransformer(false))
        );

        @Before
        public void init() {
            client = new WireMockTestClient(wm.port());
        }

        @Test
        public void appliesResponseTemplateWhenAddedToStubMapping() {
            wm.stubFor(get(urlPathEqualTo("/templated"))
                .willReturn(aResponse()
                    .withBody("{{request.path.[0]}}")
                    .withTransformers("response-template")));

            assertThat(client.get("/templated").content(), is("templated"));
        }

        @Test
        public void doesNotApplyResponseTemplateWhenNotAddedToStubMapping() {
            wm.stubFor(get(urlPathEqualTo("/not-templated"))
                .willReturn(aResponse()
                    .withBody("{{request.path.[0]}}")));

            assertThat(client.get("/not-templated").content(), is("{{request.path.[0]}}"));
        }
    }

    public static class Global {

        WireMockTestClient client;

        @Rule
        public WireMockRule wm = new WireMockRule(options()
            .dynamicPort()
            .extensions(new ResponseTemplateTransformer(true))
        );

        @Before
        public void init() {
            client = new WireMockTestClient(wm.port());
        }

        @Test
        public void appliesResponseTemplate() {
            wm.stubFor(get(urlPathEqualTo("/templated"))
                .willReturn(aResponse()
                    .withBody("{{request.path.[0]}}")));

            assertThat(client.get("/templated").content(), is("templated"));
        }

        @Test
        public void appliesToResponseBodyFromFile() {
            wm.stubFor(get(urlPathEqualTo("/templated"))
                .willReturn(aResponse()
                    .withBodyFile("templated-example.txt")));

            assertThat(client.get("/templated").content(), is("templated"));
        }

        @Test
        public void copesWithBase64BodiesWithoutTemplateElements() {
            wm.stubFor(get(urlMatching("/documents/document/.+"))
                .willReturn(ok()
                    .withBase64Body("JVBERi0xLjEKJcKlwrHDqwoKMSAwIG9iagogIDw8IC9UeXBlIC9DYXRhbG9nCiAgICAgL1BhZ2VzIDIgMCBSCiAgPj4KZW5kb2JqCgoyIDAgb2JqCiAgPDwgL1R5cGUgL1BhZ2VzCiAgICAgL0tpZHMgWzMgMCBSXQogICAgIC9Db3VudCAxCiAgICAgL01lZGlhQm94IFswIDAgMzAwIDE0NF0KICA+PgplbmRvYmoKCjMgMCBvYmoKICA8PCAgL1R5cGUgL1BhZ2UKICAgICAgL1BhcmVudCAyIDAgUgogICAgICAvUmVzb3VyY2VzCiAgICAgICA8PCAvRm9udAogICAgICAgICAgIDw8IC9GMQogICAgICAgICAgICAgICA8PCAvVHlwZSAvRm9udAogICAgICAgICAgICAgICAgICAvU3VidHlwZSAvVHlwZTEKICAgICAgICAgICAgICAgICAgL0Jhc2VGb250IC9UaW1lcy1Sb21hbgogICAgICAgICAgICAgICA+PgogICAgICAgICAgID4+CiAgICAgICA+PgogICAgICAvQ29udGVudHMgNCAwIFIKICA+PgplbmRvYmoKCjQgMCBvYmoKICA8PCAvTGVuZ3RoIDU1ID4+CnN0cmVhbQogIEJUCiAgICAvRjEgMTggVGYKICAgIDAgMCBUZAogICAgKEhlbGxvIFdvcmxkKSBUagogIEVUCmVuZHN0cmVhbQplbmRvYmoKCnhyZWYKMCA1CjAwMDAwMDAwMDAgNjU1MzUgZiAKMDAwMDAwMDAxOCAwMDAwMCBuIAowMDAwMDAwMDc3IDAwMDAwIG4gCjAwMDAwMDAxNzggMDAwMDAgbiAKMDAwMDAwMDQ1NyAwMDAwMCBuIAp0cmFpbGVyCiAgPDwgIC9Sb290IDEgMCBSCiAgICAgIC9TaXplIDUKICA+PgpzdGFydHhyZWYKNTY1CiUlRU9GCg=="))
            );

            WireMockResponse response = client.get("/documents/document/123");

            assertThat(response.statusCode(), is(200));
        }
    }
}
