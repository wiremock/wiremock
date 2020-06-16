package com.github.tomakehurst.wiremock;

import com.google.common.collect.Lists;
import org.apache.hc.core5.http.URIScheme;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testcontainers.containers.GenericContainer;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.apache.hc.core5.http.URIScheme.HTTP;
import static org.apache.hc.core5.http.URIScheme.HTTPS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class HttpClientProxyTest {

    private static final List<HttpProxiedClientBuilder> clients = asList(
            ApacheHC4ClientBuilder.INSTANCE,
            ApacheHC5ClientBuilder.INSTANCE,
            ApacheHC5AsyncClientBuilder.INSTANCE,
            OkHttp4ClientBuilder.INSTANCE,
            JettyClientBuilder.INSTANCE
    );

    private static final List<List<Object>> parameters = Lists.cartesianProduct(
            clients,
            /* proxy */ asList(HTTP, HTTPS),
            /* target */ asList(HTTP, HTTPS)
    );

    private final HttpProxiedClientBuilder clientBuilder;
    private final URIScheme proxyScheme;
    private final URIScheme targetScheme;

    @Test(timeout = 5000)
    public void proxiesSuccessfully() throws Exception {

        HttpClient httpClient = clientBuilder.buildClient(proxyScheme.name(), portFor(proxyScheme));

        Response response = httpClient.get(targetScheme+"://www.example.com");

        assertEquals(SC_OK, response.status);
        assertThat(response.body, containsString("<h1>Example Domain</h1>"));
    }

    private int portFor(URIScheme proxyScheme) {
        return proxyScheme == HTTPS ? httpsProxy.getFirstMappedPort() : httpProxy.getFirstMappedPort();
    }

    @Parameterized.Parameters(name = "{index}: client={0}, proxyScheme={1}, targetScheme={2}")
    public static Collection<Object[]> data() {
        return parameters.stream()
                .map(List::toArray)
                .collect(Collectors.toList());
    }

    public HttpClientProxyTest(HttpProxiedClientBuilder clientBuilder, URIScheme proxyScheme, URIScheme targetScheme) {
        this.clientBuilder = clientBuilder;
        this.proxyScheme = proxyScheme;
        this.targetScheme = targetScheme;
    }


    @ClassRule
    public static GenericContainer httpProxy = new GenericContainer<>("avogt/forwardproxy")
            .withExposedPorts(3128);

    @ClassRule
    public static GenericContainer httpsProxy = new GenericContainer<>("wernight/spdyproxy")
            .withExposedPorts(44300)
            ;
}
