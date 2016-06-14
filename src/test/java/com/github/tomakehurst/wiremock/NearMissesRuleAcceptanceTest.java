package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.testsupport.TestNotifier;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import ignored.UnmatchedTest;
import org.apache.http.entity.StringEntity;
import org.junit.*;
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

public class NearMissesRuleAcceptanceTest {

    static TestNotifier testNotifier = new TestNotifier();

    @ClassRule
    public static WireMockRule wm = new WireMockRule(options()
        .dynamicPort()
        .notifier(testNotifier)
        .withRootDirectory("src/main/resources/empty"),
        false);

    WireMockTestClient client;

    @Before
    public void init() {
        client = new WireMockTestClient(wm.port());
        testNotifier.reset();
    }

    @Test
    public void logsUnmatchedRequestsAtErrorWithNearMisses() throws Exception {
        wm.stubFor(get(urlEqualTo("/near-miss")).willReturn(aResponse().withStatus(200)));
        wm.stubFor(get(urlEqualTo("/miss")).willReturn(aResponse().withStatus(200)));

        client.post("/a-near-mis", new StringEntity(""));

        assertThat(testNotifier.getErrorMessages(), hasItem(allOf(
                containsString("Request was not matched:"),
                containsString("/a-near-mis"),

                containsString("Closest match:"),
                containsString("/near-miss")
            )
        ));
    }

    @Test
    public void throwsVerificationExceptionIfSomeRequestsWentUnmatched() {
        final AtomicReference<String> message = new AtomicReference<>("");

        JUnitCore junit = new JUnitCore();
        junit.addListener(new RunListener() {

            @Override
            public void testFailure(Failure failure) throws Exception {
                message.set(failure.getMessage());
            }
        });
        junit.run(UnmatchedTest.class);


        assertThat(message.get(), containsString("2 requests were unmatched by any stub mapping"));
    }

}
