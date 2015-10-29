package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import org.junit.*;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class HttpAdminClientTest extends HttpAdminClient {
    private WireMockServer wireMockServer;

    final static int httpPort = 8080;
    final static int httpsPort = 8443;

    public HttpAdminClientTest() {
        super(new HttpAdminClientBuilder("localhost", httpsPort).setScheme("https"));
    }


    @Before
    public void init() {
        wireMockServer = new WireMockServer(httpPort, httpsPort);
        wireMockServer.start();
    }

    @After
    public void stopServer() {
        wireMockServer.stop();
    }


    @Test
    public void httpAdminClientBuilderTest() {

        HttpAdminClient.HttpAdminClientBuilder builder;
        {//validate constructor
            final String host1 = "host1";
            final int port1 = 123;
            builder = new HttpAdminClient.HttpAdminClientBuilder(host1, port1);


            Assert.assertEquals(builder.getHost(), host1);
            Assert.assertEquals(builder.getPort(), port1);
        }

        final String host2 = "host2";
        final int port2 = 234;

        {//validate setters and getters

            builder.setHost(host2)
                    .setHttpClient(null)
                    .setPort(port2)
                    .setScheme("scheme")
                    .setUrlPathPrefix("prefix");
            Assert.assertEquals(builder.getHost(), host2);
            Assert.assertEquals(builder.getPort(), port2);
            Assert.assertEquals(builder.getScheme(), "scheme");
            Assert.assertEquals(builder.getUrlPathPrefix(), "prefix");
            Assert.assertNull(builder.getHttpClient());
        }

        {//validate build
            HttpAdminClient client = builder.build();

            Assert.assertEquals(client.getHost(), host2);
            Assert.assertEquals(client.getPort(), port2);
            Assert.assertEquals(client.getScheme(), "scheme");
            Assert.assertEquals(client.getUrlPathPrefix(), "prefix");
            Assert.assertNotNull(client.getHttpClient());
        }

    }

    @Test
    public void stubTest() {
        Assert.assertNotNull(this.getHttpClient());
        final String relativePath = "/some/thing";
        final String url = "https://localhost:8443/some/thing";
        this.resetMappings();

        {//no mappings at start
            ListStubMappingsResult result = this.listAllStubMappings();
            Assert.assertEquals(0, result.getMappings()
                    .size());
        }

        {//add one mapping
            this.addStubMapping(get(urlEqualTo(relativePath))
                    .willReturn(aResponse()
                            .withStatus(300)));
            getJsonAssertOkAndReturnBody(url, 300);

            ListStubMappingsResult result = this.listAllStubMappings();
            Assert.assertEquals(1, result.getMappings()
                    .size());
        }

        { //clear mappings
            this.resetMappings();
            ListStubMappingsResult result = this.listAllStubMappings();
            Assert.assertEquals(0, result.getMappings()
                    .size());
        }
    }


    @Test
    public void countTest() {
        this.resetRequests();

        Assert.assertNotNull(this.getHttpClient());
        final String relativePath = "/some/thing";
        final String url = "https://localhost:8443/some/thing";

        this.addStubMapping(get(urlEqualTo(relativePath))
                .willReturn(aResponse()
                        .withStatus(300)));
        getJsonAssertOkAndReturnBody(url, 300);


        {
            VerificationResult result = this.countRequestsMatching(getRequestedFor(urlEqualTo(relativePath)));
            Assert.assertEquals(result.getCount(), 1);

        }

        this.resetRequests();
        {
            VerificationResult result = this.countRequestsMatching(getRequestedFor(urlEqualTo(relativePath)));
            Assert.assertEquals(result.getCount(), 0);
        }
    }

}
