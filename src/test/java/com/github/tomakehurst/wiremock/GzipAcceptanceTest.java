package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.stubbing.StubMappingJsonRecorder;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.common.Gzip.unGzipToString;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GzipAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void servesGzippedResponseWhenRequested() throws Exception {
        wireMockServer.stubFor(get(urlEqualTo("/gzip-response")).willReturn(aResponse().withBody("body text")));

        WireMockResponse response = testClient.get("/gzip-response", withHeader("Accept-Encoding", "gzip,deflate"));
        assertThat(response.firstHeader("Content-Encoding"), is("gzip"));

        byte[] gzippedContent = response.binaryContent();

        String plainText = unGzipToString(gzippedContent);
        assertThat(plainText, is("body text"));
    }

}
