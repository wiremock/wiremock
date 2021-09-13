package org.wiremock.webhooks;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

public interface WebhookTransformer {

  WebhookDefinition transform(ServeEvent serveEvent, WebhookDefinition webhookDefinition);
}
