package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.apache.hc.core5.http.MalformedChunkCodingException;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FaultsAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void connectionResetByPeerFault() {
        stubFor(
                get(urlEqualTo("/connection/reset"))
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        RuntimeException runtimeException =
                assertThrows(RuntimeException.class, () -> testClient.get("/connection/reset"));
        assertThat(runtimeException.getMessage(), is("java.net.SocketException: Connection reset"));
    }

    @Test
    public void emptyResponseFault() {
        stubFor(
                get(urlEqualTo("/empty/response")).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        getAndAssertUnderlyingExceptionInstanceClass("/empty/response", NoHttpResponseException.class);
    }

    @Test
    public void malformedResponseChunkFault() {
        stubFor(
                get(urlEqualTo("/malformed/response"))
                        .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

        getAndAssertUnderlyingExceptionInstanceClass(
                "/malformed/response", MalformedChunkCodingException.class);
    }

    @Test
    public void randomDataOnSocketFault() {
        stubFor(
                get(urlEqualTo("/random/data"))
                        .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        getAndAssertUnderlyingExceptionInstanceClass("/random/data", NoHttpResponseException.class);
    }

    private void getAndAssertUnderlyingExceptionInstanceClass(String url, Class<?> expectedClass) {
        boolean thrown = false;
        try {
            WireMockResponse response = testClient.get(url);
            response.content();
        } catch (Exception e) {
            assertThat(e.getCause(), instanceOf(expectedClass));
            thrown = true;
        }

        assertTrue(thrown, "No exception was thrown");
    }
}
