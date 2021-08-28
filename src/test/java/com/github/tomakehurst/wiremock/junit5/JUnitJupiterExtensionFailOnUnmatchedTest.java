package com.github.tomakehurst.wiremock.junit5;

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

public class JUnitJupiterExtensionFailOnUnmatchedTest {

    CloseableHttpClient client = HttpClientFactory.createClient();

    @Test
    void throws_a_verification_exception_when_an_unmatched_request_is_made_during_the_test() throws Exception {
        WireMockExtension extension = WireMockExtension.newInstance()
                .failOnUnmatchedRequests(true)
                .build();

        extension.beforeEach(null);

        extension.stubFor(get("/found").willReturn(ok()));

        try (CloseableHttpResponse response = client.execute(new HttpGet(extension.baseUrl() + "/not-found"))) {
            assertThat(response.getStatusLine().getStatusCode(), is(404));
        }

        assertThrows(VerificationException.class, () -> extension.afterEach(null));
    }

    @Test
    void does_not_throw_a_verification_exception_when_fail_on_unmatched_disabled() throws Exception {
        WireMockExtension extension = WireMockExtension.newInstance()
                .failOnUnmatchedRequests(false)
                .build();

        extension.beforeEach(null);

        extension.stubFor(get("/found").willReturn(ok()));

        try (CloseableHttpResponse response = client.execute(new HttpGet(extension.baseUrl() + "/not-found"))) {
            assertThat(response.getStatusLine().getStatusCode(), is(404));
        }

        assertDoesNotThrow(() -> extension.afterEach(null));
    }


}
