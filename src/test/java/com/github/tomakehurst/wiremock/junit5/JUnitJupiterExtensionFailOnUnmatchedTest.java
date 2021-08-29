package com.github.tomakehurst.wiremock.junit5;

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JUnitJupiterExtensionFailOnUnmatchedTest {

    Mockery context;
    CloseableHttpClient client;
    ExtensionContext extensionContext;

    @BeforeEach
    void init() {
        client = HttpClientFactory.createClient();

        context = new Mockery();
        extensionContext = context.mock(ExtensionContext.class);
        context.checking(new Expectations() {{
            oneOf(extensionContext).getElement(); will(returnValue(Optional.empty()));
        }});
    }

    @Test
    void throws_a_verification_exception_when_an_unmatched_request_is_made_during_the_test() throws Exception {
        WireMockExtension extension = WireMockExtension.newInstance()
                .failOnUnmatchedRequests(true)
                .build();

        extension.beforeEach(extensionContext);

        extension.stubFor(get("/found").willReturn(ok()));

        try (CloseableHttpResponse response = client.execute(new HttpGet(extension.baseUrl() + "/not-found"))) {
            assertThat(response.getStatusLine().getStatusCode(), is(404));
        }

        assertThrows(VerificationException.class, () -> extension.afterEach(extensionContext));
    }

    @Test
    void does_not_throw_a_verification_exception_when_fail_on_unmatched_disabled() throws Exception {
        WireMockExtension extension = WireMockExtension.newInstance()
                .failOnUnmatchedRequests(false)
                .build();

        extension.beforeEach(extensionContext);

        extension.stubFor(get("/found").willReturn(ok()));

        try (CloseableHttpResponse response = client.execute(new HttpGet(extension.baseUrl() + "/not-found"))) {
            assertThat(response.getStatusLine().getStatusCode(), is(404));
        }

        assertDoesNotThrow(() -> extension.afterEach(extensionContext));
    }


}
