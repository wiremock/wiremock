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

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.common.Gzip.unGzipToString;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GzipAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void servesGzippedResponseForGet() throws Exception {
        wireMockServer.stubFor(get(urlEqualTo("/gzip-response")).willReturn(aResponse().withBody("body text")));

        WireMockResponse response = testClient.get("/gzip-response", withHeader("Accept-Encoding", "gzip,deflate"));
        assertThat(response.firstHeader("Content-Encoding"), is("gzip"));

        byte[] gzippedContent = response.binaryContent();

        String plainText = unGzipToString(gzippedContent);
        assertThat(plainText, is("body text"));
    }

    @Test
    public void servesGzippedResponseForPost() throws Exception {
        wireMockServer.stubFor(post("/gzip-response").willReturn(ok("body text")));

        WireMockResponse response = testClient.post("/gzip-response",
            new StringEntity(""),
            withHeader("Accept-Encoding", "gzip,deflate")
        );
        assertThat(response.firstHeader("Content-Encoding"), is("gzip"));

        byte[] gzippedContent = response.binaryContent();

        String plainText = unGzipToString(gzippedContent);
        assertThat(plainText, is("body text"));
    }

    @Test
    public void acceptsGzippedRequest() {
        wireMockServer.stubFor(any(urlEqualTo("/gzip-request"))
            .withRequestBody(equalTo("request body"))
            .willReturn(aResponse().withBody("response body")));

        HttpEntity compressedBody = new GzipCompressingEntity(new StringEntity("request body", ContentType.TEXT_PLAIN));
        WireMockResponse response = testClient.post("/gzip-request", compressedBody);

        assertThat(response.content(), is("response body"));
    }
}
