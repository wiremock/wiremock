package ignored;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JUnit5ProxyTest {

    @RegisterExtension
    public WireMockExtension wm = WireMockExtension.newInstance().options(options().dynamicPort().enableBrowserProxying(true)).build();

    CloseableHttpClient httpClient = HttpClientBuilder.create()
            .useSystemProperties() // This must be enabled for auto-configuration of proxy settings to work
            .build();

    @BeforeEach
    public void init() {
        JvmProxyConfigurer.configureFor(wm.getPort());
    }

    @AfterEach
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

        ClassicHttpResponse response = httpClient.execute(new HttpGet("http://my.first.domain/things"));
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

        ClassicHttpResponse response = httpClient.execute(new HttpGet("http://my.first.domain/things"));
        String responseBody = EntityUtils.toString(response.getEntity());
        assertEquals("Domain 1", responseBody);

        response = httpClient.execute(new HttpGet("http://my.second.domain/things"));
        responseBody = EntityUtils.toString(response.getEntity());
        assertEquals("Domain 2", responseBody);

        wireMockServer.stop();
        JvmProxyConfigurer.restorePrevious();
    }
}
