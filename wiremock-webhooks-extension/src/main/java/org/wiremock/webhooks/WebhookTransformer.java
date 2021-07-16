package org.wiremock.webhooks;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.wiremock.webhooks.WebhookDefinition;

public interface WebhookTransformer {

  WebhookDefinition transform(ServeEvent serveEvent, WebhookDefinition webhookDefinition);
}
