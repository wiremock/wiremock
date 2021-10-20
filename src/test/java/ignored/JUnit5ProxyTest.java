package ignored;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;

public class JUnit5ProxyTest {

    @Rule
    public WireMockRule wm = new WireMockRule(options().dynamicPort().enableBrowserProxying(true));

    HttpClient httpClient = HttpClientBuilder.create()
            .useSystemProperties() // This must be enabled for auto-configuration of proxy settings to work
            .build();

    @Before
    public void init() {
        JvmProxyConfigurer.configureFor(wm);
    }

    @After
    public void cleanup() {
        JvmProxyConfigurer.restorePrevious();
    }

    @Test
    public void testViaProxyUsingRule() throws Exception {
        wm.stubFor(get("/things")
                .withHost(equalTo("my.first.domain"))
                .willReturn(ok("Domain 1")));

        wm.stubFor(get("/things")
                .withHost(equalTo("my.second.domain"))
                .willReturn(ok("Domain 2")));

        HttpResponse response = httpClient.execute(new HttpGet("http://my.first.domain/things"));
        String responseBody = EntityUtils.toString(response.getEntity());
        assertEquals("Domain 1", responseBody);

        response = httpClient.execute(new HttpGet("http://my.second.domain/things"));
        responseBody = EntityUtils.toString(response.getEntity());
        assertEquals("Domain 2", responseBody);
    }

    @Test
    public void testViaProxyUsingServer() throws Exception {
        WireMockServer wireMockServer = new WireMockServer(options().dynamicPort().enableBrowserProxying(true));
        wireMockServer.start();
        JvmProxyConfigurer.configureFor(wireMockServer);

        wireMockServer.stubFor(get("/things")
                .withHost(equalTo("my.first.domain"))
                .willReturn(ok("Domain 1")));

        wireMockServer.stubFor(get("/things")
                .withHost(equalTo("my.second.domain"))
                .willReturn(ok("Domain 2")));

        HttpResponse response = httpClient.execute(new HttpGet("http://my.first.domain/things"));
        String responseBody = EntityUtils.toString(response.getEntity());
        assertEquals("Domain 1", responseBody);

        response = httpClient.execute(new HttpGet("http://my.second.domain/things"));
        responseBody = EntityUtils.toString(response.getEntity());
        assertEquals("Domain 2", responseBody);

        wireMockServer.stop();
        JvmProxyConfigurer.restorePrevious();
    }
}
