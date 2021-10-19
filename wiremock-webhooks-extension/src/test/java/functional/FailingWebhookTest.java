package functional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.wiremock.webhooks.Webhooks.webhook;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.concurrent.CountDownLatch;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.wiremock.webhooks.Webhooks;
import testsupport.TestNotifier;
import testsupport.ThrowingWebhookTransformer;
import testsupport.WireMockTestClient;

public class FailingWebhookTest {

  @Rule
  public WireMockRule targetServer = new WireMockRule(options().dynamicPort());

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
    targetServer.addMockServiceRequestListener(new RequestListener() {
      @Override
      public void requestReceived(Request request, Response response) {
        if (request.getUrl().startsWith("/callback")) {
          latch.countDown();
        }
      }
    });
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
