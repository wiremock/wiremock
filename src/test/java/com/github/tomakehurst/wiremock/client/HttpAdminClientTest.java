package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.*;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

public class HttpAdminClientTest {

    final static int httpPort = 18080;
    final static int httpsPort = 18443;

    final static String relativePath = "/some/thing";
    final static String url = "https://localhost:" + httpsPort + "/some/thing";


    HttpAdminClient httpAdminClient =
            new HttpAdminClient.HttpAdminClientBuilder("localhost", httpsPort).setScheme("https")
                    .build();

    WireMockServer wireMockServer = new WireMockServer(httpPort, httpsPort);


    @Before
    public void init() {
        wireMockServer.start();
    }

    @After
    public void stopServer() {
        wireMockServer.stop();
    }

    @Test
    public void shouldSetAndGetBuilderProperties() {

        HttpAdminClient.HttpAdminClientBuilder builder = new HttpAdminClient.HttpAdminClientBuilder("temp", 0);

        assertThat(builder.getHost(), is("temp"));
        assertThat(builder.getPort(), is(0));

        final String host = "host";
        final int port = 8080;

        builder.setHost(host)
                .setHttpClient(null)
                .setPort(port)
                .setScheme("scheme")
                .setUrlPathPrefix("prefix");
        assertThat(builder.getHost(), is(host));
        assertThat(builder.getPort(), is(port));
        assertThat(builder.getScheme(), is("scheme"));
        assertThat(builder.getUrlPathPrefix(), is("prefix"));
        assertThat(builder.getHttpClient(), nullValue());

        HttpAdminClient client = builder.build();
        assertThat(client.getHost(), is(host));
        assertThat(client.getPort(), is(port));
        assertThat(client.getScheme(), is("scheme"));
        assertThat(client.getUrlPathPrefix(), is("prefix"));
        assertThat(client.getHttpClient(), notNullValue());
    }

    @Test
    public void shouldAddStub() {
        httpAdminClient.resetMappings();
        httpAdminClient.addStubMapping(get(urlEqualTo(relativePath))
                .willReturn(aResponse()
                        .withStatus(300)
                        .withBody("test body")).build());
        assertThat(httpAdminClient.getJsonAssertOkAndReturnBody(url, 300), is("test body"));
        assertThat(httpAdminClient.listAllStubMappings()
                .getMappings()
                .size(), is(1));
    }

    @Test
    public void shouldCountCalls() {
        httpAdminClient.resetRequests();
        assertThat(httpAdminClient.getHttpClient(), notNullValue());

        httpAdminClient.addStubMapping(get(urlEqualTo(relativePath))
                .willReturn(aResponse()
                        .withStatus(300)).build());
        httpAdminClient.getJsonAssertOkAndReturnBody(url, 300);

        assertThat(httpAdminClient.countRequestsMatching(getRequestedFor(urlEqualTo(relativePath)).build())
                .getCount(), is(1));

        httpAdminClient.resetRequests();
        assertThat(httpAdminClient.countRequestsMatching(getRequestedFor(urlEqualTo(relativePath)).build())
                .getCount(), is(0));
    }

}