package ignored;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.testsupport.TestNotifier;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class NearMissExampleTest {

    @ClassRule
    public static WireMockRule wm = new WireMockRule(options()
        .dynamicPort()
        .withRootDirectory("src/main/resources/empty"),
        false);

    WireMockTestClient client;

    @Before
    public void init() {
        client = new WireMockTestClient(wm.port());
    }

    @Test
    public void showFullUnmatchedRequest() throws Exception {
        client.get("/some-other-thing");
        client.get("/totally-something-else");
        client.get("/whatever");
        client.post("/my-near-miss",
            new StringEntity("{\"data\": { \"one\": 1}}", APPLICATION_JSON),
            withHeader("Content-Type", "application/json"),
            withHeader("X-Expected", "yes"),
            withHeader("X-Matched-1", "yes"),
            withHeader("Cookie", "this=that"),
            withHeader("Authorization", new BasicCredentials("user", "wrong-pass").asAuthorizationHeaderValue())
        );

        wm.verify(postRequestedFor(urlEqualTo("/a-near-miss"))
            .withHeader("Content-Type", equalTo("text/json"))
            .withHeader("X-Expected", equalTo("yes"))
            .withHeader("X-Matched-1", matching("ye.*"))
            .withHeader("X-Matched-2", containing("no"))
            .withCookie("this", equalTo("other"))
            .withBasicAuth(new BasicCredentials("user", "pass"))
            .withRequestBody(equalToJson("{\"data\": { \"two\": 1}}")));
    }

}
