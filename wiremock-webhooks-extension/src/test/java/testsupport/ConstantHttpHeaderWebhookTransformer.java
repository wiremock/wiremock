package testsupport;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.wiremock.webhooks.WebhookDefinition;
import org.wiremock.webhooks.WebhookTransformer;

public class ConstantHttpHeaderWebhookTransformer implements WebhookTransformer {

  public static final String key = "X-customer-header";
  public static final String value = "foo";

  @Override
  public WebhookDefinition transform(ServeEvent serveEvent, WebhookDefinition webhookDefinition) {
    return webhookDefinition.withHeader(key, value);
  }
}
