/*
 * Copyright (C) 2021-2025 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wiremock.webhooks;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.extension.ServeEventListener;
import com.github.tomakehurst.wiremock.extension.WireMockServices;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

@SuppressWarnings("deprecation") // maintaining PostServeAction for backwards compatibility
public class Webhooks extends PostServeAction implements ServeEventListener {

  private final ScheduledExecutorService scheduler;
  private final HttpClient httpClient;
  private final List<WebhookTransformer> transformers;
  private final TemplateEngine templateEngine;
  private final DataTruncationSettings dataTruncationSettings;

  public Webhooks(
      WireMockServices wireMockServices,
      ScheduledExecutorService scheduler,
      List<WebhookTransformer> transformers) {

    this.scheduler = scheduler;
    this.httpClient = wireMockServices.getDefaultHttpClient();
    this.transformers = transformers;
    this.templateEngine = wireMockServices.getTemplateEngine();
    this.dataTruncationSettings = wireMockServices.getOptions().getDataTruncationSettings();
  }

  @Override
  public String getName() {
    return "webhook";
  }

  @Override
  public void afterComplete(ServeEvent serveEvent, Parameters parameters) {
    triggerWebhook(serveEvent, parameters);
  }

  @Override
  public void doAction(
      final ServeEvent serveEvent, final Admin admin, final Parameters parameters) {
    triggerWebhook(serveEvent, parameters);
  }

  private void triggerWebhook(ServeEvent serveEvent, Parameters parameters) {
    final Notifier notifier = notifier();

    WebhookDefinition definition;
    Request request;
    try {
      definition = WebhookDefinition.from(parameters);
      for (WebhookTransformer transformer : transformers) {
        definition = transformer.transform(serveEvent, definition);
      }
      definition = applyTemplating(definition, serveEvent);
      request = buildRequest(definition);

      serveEvent.appendSubEvent("WEBHOOK_REQUEST", LoggedRequest.createFrom(request));
    } catch (Exception e) {
      final String msg = "Exception thrown while configuring webhook";
      notifier().error(msg, e);
      serveEvent.appendSubEvent(SubEvent.error(msg + ": " + e.getMessage()));
      return;
    }

    final WebhookDefinition finalDefinition = definition;
    scheduler.schedule(
        () -> {
          try {
            Response response = httpClient.execute(request);
            notifier.info(
                String.format(
                    "Webhook %s request to %s returned status %s\n\n%s",
                    finalDefinition.getMethod(),
                    finalDefinition.getUrl(),
                    response.getStatus(),
                    response.getBodyAsString()));
            serveEvent.appendSubEvent(
                "WEBHOOK_RESPONSE",
                LoggedResponse.from(
                    response, this.dataTruncationSettings.getMaxResponseBodySize()));
          } catch (ProhibitedNetworkAddressException e) {
            final String msg =
                String.format(
                    "The target webhook address %s specified by stub %s is denied in WireMock's configuration.",
                    finalDefinition.getUrl(),
                    getFirstNonNull(
                        serveEvent.getStubMapping().getName(),
                        serveEvent.getStubMapping().getId(),
                        "<no name or id>"));
            notifier.error(msg);
            serveEvent.appendSubEvent(SubEvent.error(msg));
          } catch (Exception e) {
            final String msg =
                String.format(
                    "Failed to fire webhook %s %s",
                    finalDefinition.getMethod(), finalDefinition.getUrl());
            notifier.error(msg, e);
            serveEvent.appendSubEvent(SubEvent.error(msg + ": " + e.getMessage()));
          }
        },
        finalDefinition.getDelaySampleMillis(),
        MILLISECONDS);
  }

  private WebhookDefinition applyTemplating(
      WebhookDefinition webhookDefinition, ServeEvent serveEvent) {

    final Map<String, Object> model =
        new HashMap<>(this.templateEngine.buildModelForRequest(serveEvent));
    model.put(
        "parameters",
        webhookDefinition.getExtraParameters() != null
            ? webhookDefinition.getExtraParameters()
            : Collections.<String, Object>emptyMap());
    model.put("originalRequest", model.get("request"));
    model.remove("request");

    WebhookDefinition renderedWebhookDefinition =
        webhookDefinition
            .withUrl(renderTemplate(model, webhookDefinition.getUrl()))
            .withMethod(renderTemplate(model, webhookDefinition.getMethod()))
            .withHeaders(
                webhookDefinition.getHeaders().all().stream()
                    .map(
                        header ->
                            new HttpHeader(
                                header.key(),
                                header.values().stream()
                                    .map(value -> renderTemplate(model, value))
                                    .collect(toList())))
                    .collect(toList()));

    if (webhookDefinition.specifiesBodyFile()) {
      FileSource fileSource = WireMockConfiguration.wireMockConfig().filesRoot();
      try {
        BinaryFile bodyFile = fileSource.getBinaryFileNamed(webhookDefinition.getBodyFileName());
        webhookDefinition.withBody(new String(bodyFile.readContents()));
      } catch (Exception ex) {
        StringWriter writer = new StringWriter();
        writer.append(
            webhookDefinition.getBodyFileName()
                + " not found in fileSource path: "
                + fileSource.getPath());
        ex.printStackTrace(new PrintWriter(writer));
        renderedWebhookDefinition.withBody(writer.toString());
      }
    }

    if (webhookDefinition.getBody() != null) {
      renderedWebhookDefinition.withBody(renderTemplate(model, webhookDefinition.getBody()));
    }

    return renderedWebhookDefinition;
  }

  private String renderTemplate(Object context, String value) {
    return templateEngine.getUncachedTemplate(value).apply(context);
  }

  private static Request buildRequest(WebhookDefinition definition) {
    final ImmutableRequest.Builder requestBuilder =
        ImmutableRequest.create()
            .withMethod(definition.getRequestMethod())
            .withAbsoluteUrl(definition.getUrl())
            .withHeaders(definition.getHeaders());

    if (definition.getRequestMethod().hasEntity() && definition.hasBody()) {
      requestBuilder.withBody(definition.getBinaryBody());
    }

    return requestBuilder.build();
  }

  @Override
  public boolean applyGlobally() {
    return false;
  }

  public static WebhookDefinition webhook() {
    return new WebhookDefinition();
  }
}
