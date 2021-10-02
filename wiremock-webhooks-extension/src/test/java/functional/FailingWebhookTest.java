package functional;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.wiremock.webhooks.Webhooks;
import testsupport.TestNotifier;
import testsupport.ThrowingWebhookTransformer;
import testsupport.WireMockTestClient;

import java.util.concurrent.CountDownLatch;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.hc.core5.http.ContentType.TEXT_PLAIN;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.wiremock.webhooks.Webhooks.webhook;

public class FailingWebhookTest {

  @Rule
  public WireMockRule targetServer = new WireMockRule(options().dynamicPort()
      .extensions(new PostServeAction() {
        @Override
        public void doGlobalAction(ServeEvent serveEvent, Admin admin) {
          if (serveEvent.getRequest().getUrl().startsWith("/callback")) {
            latch.countDown();
          }
        }

        @Override
        public String getName() {
          return "test-latch";
        }
      }));

  CountDownLatch latch;

  Webhooks webhooks = new Webhooks(
      new ThrowingWebhookTransformer()
  );

  TestNotifier notifier = new TestNotifier();
  WireMockTestClient client;

  @Rule
  public WireMockRule rule = new WireMockRule(
      options()
          .dynamicPort()
          .notifier(notifier)
          .extensions(webhooks));

  @Before
  public void init() {
    reset();
    notifier.reset();
    targetServer.stubFor(any(anyUrl())
        .willReturn(aResponse().withStatus(200)));
    latch = new CountDownLatch(1);
    client = new WireMockTestClient(rule.port());
    WireMock.configureFor(targetServer.port());
  }

  @Test
  public void failWhenExecutingTheWebhook() throws Exception {
    rule.stubFor(post(urlPathEqualTo("/something-async"))
        .willReturn(aResponse().withStatus(200))
        .withPostServeAction("webhook", webhook()
            .withMethod(POST)
            .withUrl("http://localhost:" + targetServer.port() + "/callback")
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"result\": \"SUCCESS\" }"))
    );

    verify(0, postRequestedFor(anyUrl()));

    client.post("/something-async", new StringEntity("", TEXT_PLAIN));
    latch.await(1, SECONDS);
    assertThat("No webook should have been made", latch.getCount(), is(1L));
  }
}
