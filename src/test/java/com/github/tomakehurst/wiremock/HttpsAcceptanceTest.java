package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import org.apache.http.HttpResponse;
import org.apache.http.MalformedChunkCodingException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HttpsAcceptanceTest {

    private static final int HTTP_PORT = 8080;
    private static final int HTTPS_PORT = 8443;

    private static WireMockServer wireMockServer;
    private static HttpClient httpClient;

    @BeforeClass
    public static void setupServer() {
        wireMockServer = new WireMockServer(HTTP_PORT, HTTPS_PORT);
        wireMockServer.setVerboseLogging(true);
        wireMockServer.start();
        WireMock.configure();

        httpClient = HttpClientFactory.createClient();
    }

    @AfterClass
    public static void serverShutdown() {
        wireMockServer.stop();
    }

    @Before
    public void init() throws InterruptedException {
        WireMock.reset();
    }

    @Test
    public void shouldReturnStubOnSpecifiedPort() throws Exception {
        stubFor(get(urlEqualTo("/https-test")).willReturn(aResponse().withStatus(200).withBody("HTTPS content")));

        assertThat(contentFor(url("/https-test")), is("HTTPS content"));
    }

    @Test
    public void emptyResponseFault() {
        stubFor(get(urlEqualTo("/empty/response")).willReturn(
                aResponse()
                        .withFault(Fault.EMPTY_RESPONSE)));


        getAndAssertUnderlyingExceptionInstanceClass(url("/empty/response"), NoHttpResponseException.class);
    }

    @Test
    public void malformedResponseChunkFault() {
        stubFor(get(urlEqualTo("/malformed/response")).willReturn(
                aResponse()
                        .withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

        getAndAssertUnderlyingExceptionInstanceClass(url("/malformed/response"), MalformedChunkCodingException.class);
    }

    @Test
    public void randomDataOnSocketFault() {
        stubFor(get(urlEqualTo("/random/data")).willReturn(
                aResponse()
                        .withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        getAndAssertUnderlyingExceptionInstanceClass(url("/random/data"), ProtocolException.class);
    }

    private String url(String path) {
        return String.format("https://localhost:%d%s", HTTPS_PORT, path);
    }

    private void getAndAssertUnderlyingExceptionInstanceClass(String url, Class<?> expectedClass) {
        boolean thrown = false;
        try {
            contentFor(url);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                assertThat(e.getCause(), instanceOf(expectedClass));
            } else {
                assertThat(e, instanceOf(expectedClass));
            }

            thrown = true;
        }

        assertTrue("No exception was thrown", thrown);
    }

    private static String contentFor(String url) throws Exception {
        HttpGet get = new HttpGet(url);
        HttpResponse response = httpClient.execute(get);
        String content = EntityUtils.toString(response.getEntity());
        return content;
    }
}
