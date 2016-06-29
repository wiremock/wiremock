package ignored;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class ManyUnmatchedRequestsTest {

    @Rule
    public WireMockRule wm = new WireMockRule(options()
        .dynamicPort()
        .withRootDirectory("src/main/resources/empty"));

    WireMockTestClient client;

    @Before
    public void init() {
        client = new WireMockTestClient(wm.port());
    }

    @Test
    public void unmatched() {
        wm.stubFor(get(urlEqualTo("/hit")).willReturn(aResponse().withStatus(200)));

        client.get("/a-near-mis");
        client.get("/near-misssss");
        client.get("/hit");
    }
}
