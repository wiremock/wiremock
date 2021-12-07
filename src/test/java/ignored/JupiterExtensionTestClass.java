package ignored;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit5.JUnitJupiterExtensionSubclassingTest;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.junit5.WireMockExtension.extensionOptions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JupiterExtensionTestClass {

    CloseableHttpClient client = HttpClientFactory.createClient();

    @RegisterExtension
    static JUnitJupiterExtensionSubclassingTest.MyWireMockExtension wm =
            new JUnitJupiterExtensionSubclassingTest.MyWireMockExtension(
                    extensionOptions()
                            .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
                            .configureStaticDsl(true));

    @Test
    void respects_config_passed_via_builder() throws Exception {
        assertThat(JUnitJupiterExtensionSubclassingTest.MyWireMockExtension.beforeAllCalled, is(true));
        assertThat(JUnitJupiterExtensionSubclassingTest.MyWireMockExtension.beforeEachCalled, is(true));
        assertThat(JUnitJupiterExtensionSubclassingTest.MyWireMockExtension.afterEachCalled, is(false));
        assertThat(JUnitJupiterExtensionSubclassingTest.MyWireMockExtension.afterAllCalled, is(false));

        stubFor(get("/ping").willReturn(ok()));

        try (CloseableHttpResponse response =
                     client.execute(new HttpGet("https://localhost:" + wm.getHttpsPort() + "/ping"))) {
            assertThat(response.getCode(), is(200));
        }
    }
}
