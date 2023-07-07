/*
 * Copyright (C) 2021-2023 Thomas Akehurst
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
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.extension.responsetemplating.RequestTemplateModel;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

public class Webhooks extends PostServeAction {

  private final ScheduledExecutorService scheduler;
  private final CloseableHttpClient httpClient;
  private final List<WebhookTransformer> transformers;
  private final TemplateEngine templateEngine;

  private Webhooks(
      ScheduledExecutorService scheduler,
      CloseableHttpClient httpClient,
      List<WebhookTransformer> transformers) {
    this.scheduler = scheduler;
    this.httpClient = httpClient;
    this.transformers = transformers;

    this.templateEngine = TemplateEngine.defaultTemplateEngine();
  }

  @JsonCreator
  public Webhooks() {
    this(Executors.newScheduledThreadPool(10), createHttpClient(), new ArrayList<>());
  }

  public Webhooks(WebhookTransformer... transformers) {
    this(Executors.newScheduledThreadPool(10), createHttpClient(), Arrays.asList(transformers));
  }

  private static CloseableHttpClient createHttpClient() {
    return HttpClientBuilder.create()
        .disableAuthCaching()
        .disableAutomaticRetries()
        .disableCookieManagement()
        .disableRedirectHandling()
        .disableContentCompression()
        .setConnectionManager(
            PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultSocketConfig(
                    SocketConfig.custom().setSoTimeout(Timeout.ofMilliseconds(30000)).build())
                .setMaxConnPerRoute(1000)
                .setMaxConnTotal(1000)
                .setValidateAfterInactivity(TimeValue.ofSeconds(5)) // TODO Verify duration
                .build())
        .setConnectionReuseStrategy((request, response, context) -> false)
        .setKeepAliveStrategy((response, context) -> TimeValue.ZERO_MILLISECONDS)
        .build();
  }

  @Override
  public String getName() {
    return "webhook";
  }

  @Override
  public void doAction(
      final ServeEvent serveEvent, final Admin admin, final Parameters parameters) {
    final Notifier notifier = notifier();

    WebhookDefinition definition;
    ClassicHttpRequest request;
    try {
      definition = WebhookDefinition.from(parameters);
      for (WebhookTransformer transformer : transformers) {
        definition = transformer.transform(serveEvent, definition);
      }
      definition = applyTemplating(definition, serveEvent);
      request = buildRequest(definition);
    } catch (Exception e) {
      notifier().error("Exception thrown while configuring webhook", e);
      return;
    }

    final WebhookDefinition finalDefinition = definition;
    scheduler.schedule(
        () -> {
          try (CloseableHttpResponse response = httpClient.execute(request)) {
            notifier.info(
                String.format(
                    "Webhook %s request to %s returned status %s\n\n%s",
                    finalDefinition.getMethod(),
                    finalDefinition.getUrl(),
                    response.getCode(),
                    EntityUtils.toString(response.getEntity())));
          } catch (Exception e) {
            notifier()
                .error(
                    String.format(
                        "Failed to fire webhook %s %s",
                        finalDefinition.getMethod(), finalDefinition.getUrl()),
                    e);
          }
        },
        finalDefinition.getDelaySampleMillis(),
        MILLISECONDS);
  }

  private WebhookDefinition applyTemplating(
      WebhookDefinition webhookDefinition, ServeEvent serveEvent) {

    final Map<String, Object> model = new HashMap<>();
    model.put(
        "parameters",
        webhookDefinition.getExtraParameters() != null
            ? webhookDefinition.getExtraParameters()
            : Collections.<String, Object>emptyMap());
    model.put("originalRequest", RequestTemplateModel.from(serveEvent.getRequest()));

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

    if (webhookDefinition.getBody() != null) {
      renderedWebhookDefinition =
          webhookDefinition.withBody(renderTemplate(model, webhookDefinition.getBody()));
    }

    return renderedWebhookDefinition;
  }

  private String renderTemplate(Object context, String value) {
    return templateEngine.getUncachedTemplate(value).apply(context);
  }

  private static ClassicHttpRequest buildRequest(WebhookDefinition definition) {
    final ClassicRequestBuilder requestBuilder =
        ClassicRequestBuilder.create(definition.getMethod()).setUri(definition.getUrl());

    for (HttpHeader header : definition.getHeaders().all()) {
      for (String value : header.values()) {
        requestBuilder.addHeader(header.key(), value);
      }
    }

    if (definition.getRequestMethod().hasEntity() && definition.hasBody()) {
      requestBuilder.setEntity(
          new ByteArrayEntity(definition.getBinaryBody(), ContentType.DEFAULT_BINARY));
    }

    return requestBuilder.build();
  }

  public static WebhookDefinition webhook() {
    return new WebhookDefinition();
  }
}
