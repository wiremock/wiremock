package functional;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.wiremock.webhooks.Webhooks;
import testsupport.TestNotifier;
import testsupport.ThrowingWebhookTransformer;
import testsupport.WireMockTestClient;

import java.util.concurrent.CountDownLatch;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.wiremock.webhooks.Webhooks.webhook;

public class FailingWebhookTest {

  @RegisterExtension
  public WireMockExtension targetServer = WireMockExtension.newInstance()
    .options(options().dynamicPort()
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
      }))
    .build();

  CountDownLatch latch;

  Webhooks webhooks = new Webhooks(new ThrowingWebhookTransformer());

  TestNotifier notifier = new TestNotifier();
  WireMockTestClient client;

  @RegisterExtension
  public WireMockExtension extension = WireMockExtension.newInstance()
    .configureStaticDsl(true)
    .options(options()
      .dynamicPort()
      .notifier(notifier)
      .extensions(webhooks))
    .build();

  @BeforeEach
  public void init() {
    notifier.reset();
    targetServer.stubFor(any(anyUrl())
        .willReturn(aResponse().withStatus(200)));
    latch = new CountDownLatch(1);
    client = new WireMockTestClient(extension.getRuntimeInfo().getHttpPort());
    WireMock.configureFor(targetServer.getRuntimeInfo().getHttpPort());
  }

  @Test
  public void failWhenExecutingTheWebhook() throws Exception {
    extension.stubFor(post(urlPathEqualTo("/something-async"))
        .willReturn(aResponse().withStatus(200))
        .withPostServeAction("webhook", webhook()
            .withMethod(POST)
            .withUrl(targetServer.url("/callback"))
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"result\": \"SUCCESS\" }")));

    verify(0, postRequestedFor(anyUrl()));

    client.post("/something-async", new StringEntity("", TEXT_PLAIN));
    latch.await(1, SECONDS);
    assertThat("No webook should have been made", latch.getCount(), is(1L));
  }
}
