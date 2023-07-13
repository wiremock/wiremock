/*
 * Copyright (C) 2015-2021 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.common.Gzip.gzip;
import static com.github.tomakehurst.wiremock.common.Gzip.unGzipToString;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hc.client5.http.entity.GzipCompressingEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.eclipse.jetty.util.Jetty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class GzipAcceptanceTest {

  @Nested
  class ChunkedTransferEncoding extends AcceptanceTestBase {

    @Test
    public void servesGzippedResponseForGet() {
      wireMockServer.stubFor(
          get(urlEqualTo("/gzip-response")).willReturn(aResponse().withBody("body text")));

      WireMockResponse response =
          testClient.get("/gzip-response", withHeader("Accept-Encoding", "gzip,deflate"));
      assertThat(response.firstHeader("Content-Encoding"), is("gzip"));
      assertThat(response.firstHeader("Transfer-Encoding"), is("chunked"));
      assertThat(response.headers().containsKey("Content-Length"), is(false));
      assertThat(response.headers().containsKey("Vary"), is(false));

      byte[] gzippedContent = response.binaryContent();

      String plainText = unGzipToString(gzippedContent);
      assertThat(plainText, is("body text"));
    }

    @Test
    public void servesGzippedResponseForPost() {
      wireMockServer.stubFor(post("/gzip-response").willReturn(ok("body text")));

      WireMockResponse response =
          testClient.post(
              "/gzip-response",
              new StringEntity(""),
              withHeader("Accept-Encoding", "gzip,deflate"));
      assertThat(response.firstHeader("Content-Encoding"), is("gzip"));

      byte[] gzippedContent = response.binaryContent();

      String plainText = unGzipToString(gzippedContent);
      assertThat(plainText, is("body text"));
    }

    @Test
    public void acceptsGzippedRequest() {
      wireMockServer.stubFor(
          any(urlEqualTo("/gzip-request"))
              .withRequestBody(equalTo("request body"))
              .willReturn(aResponse().withBody("response body")));

      HttpEntity compressedBody =
          new GzipCompressingEntity(new StringEntity("request body", ContentType.TEXT_PLAIN));
      WireMockResponse response = testClient.post("/gzip-request", compressedBody);

      assertThat(response.content(), is("response body"));
    }
  }

  @Nested
  class NoChunkedTransferEncoding {

    @RegisterExtension
    public WireMockExtension wm =
        WireMockExtension.newInstance()
            .options(
                wireMockConfig()
                    .dynamicPort()
                    .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.NEVER))
            .build();

    WireMockTestClient testClient;

    @BeforeEach
    public void init() {
      testClient = new WireMockTestClient(wm.getPort());
    }

    @Test
    public void returnsContentLengthHeaderWhenChunkedEncodingDisabled() {
      assumeTrue(isNotOldJettyVersion());

      String bodyText =
          RandomStringUtils.randomAlphabetic(
              257); // 256 bytes is the minimum size for gzip to be used
      wm.stubFor(get("/gzip-response").willReturn(ok(bodyText)));

      WireMockResponse response =
          testClient.get("/gzip-response", withHeader("Accept-Encoding", "gzip,deflate"));
      assertThat(response.firstHeader("Content-Encoding"), is("gzip"));
      assertThat(response.headers().containsKey("Transfer-Encoding"), is(false));
      assertThat(response.firstHeader("Content-Length"), is(String.valueOf(gzip(bodyText).length)));

      byte[] gzippedContent = response.binaryContent();

      String plainText = unGzipToString(gzippedContent);
      assertThat(plainText, is(bodyText));
    }

    private boolean isNotOldJettyVersion() {
      return !Jetty.VERSION.contains("9.2.");
    }
  }

  @Nested
  class GzipDisabled {

    @RegisterExtension
    public WireMockExtension wm =
        WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().gzipDisabled(true))
            .build();

    WireMockTestClient testClient;

    @BeforeEach
    public void init() {
      testClient = new WireMockTestClient(wm.getPort());
    }

    @Test
    public void doesNotGzipWhenDisabledInConfiguration() {
      String url = "/no-gzip-response";
      String bodyText = "body text";
      wm.stubFor(get(urlEqualTo(url)).willReturn(ok(bodyText)));

      WireMockResponse response =
          testClient.get(url, withHeader("Accept-Encoding", "gzip,deflate"));

      assertThat(response.statusCode(), is(200));
      assertThat(response.headers().containsKey("Content-Encoding"), is(false));

      String plainText = response.content();
      assertThat(plainText, is(bodyText));
    }
  }
}
