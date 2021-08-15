package org.wiremock.webhooks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.extension.responsetemplating.RequestTemplateModel;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static java.util.concurrent.TimeUnit.*;
import static java.util.stream.Collectors.toList;

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

        this.templateEngine = new TemplateEngine(
                Collections.emptyMap(),
                null,
                Collections.emptySet()
        );
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
                .setMaxConnTotal(1000)
                .setMaxConnPerRoute(1000)
                .setDefaultRequestConfig(RequestConfig.custom().setStaleConnectionCheckEnabled(true).build())
                .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(30000).build())
                .setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE)
                .setKeepAliveStrategy((response, context) -> 0)
                .build();
    }

    @Override
    public String getName() {
        return "webhook";
    }

    @Override
    public void doAction(final ServeEvent serveEvent, final Admin admin, final Parameters parameters) {
        final Notifier notifier = notifier();

        WebhookDefinition definition;
        HttpUriRequest request;
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
                                String.format("Webhook %s request to %s returned status %s\n\n%s",
                                        finalDefinition.getMethod(),
                                        finalDefinition.getUrl(),
                                        response.getStatusLine(),
                                        EntityUtils.toString(response.getEntity())
                                )
                        );
                    } catch (Exception e) {
                        notifier().error(String.format("Failed to fire webhook %s %s", finalDefinition.getMethod(), finalDefinition.getUrl()), e);
                    }
                },
                finalDefinition.getDelaySampleMillis(),
                MILLISECONDS
        );
    }

    private WebhookDefinition applyTemplating(WebhookDefinition webhookDefinition, ServeEvent serveEvent) {

        final Map<String, Object> model = new HashMap<>();
        model.put("parameters", webhookDefinition.getExtraParameters() != null ?
                webhookDefinition.getExtraParameters() :
                Collections.<String, Object>emptyMap());
        model.put("originalRequest", RequestTemplateModel.from(serveEvent.getRequest()));

        WebhookDefinition renderedWebhookDefinition = webhookDefinition
                .withUrl(renderTemplate(model, webhookDefinition.getUrl()))
                .withMethod(renderTemplate(model, webhookDefinition.getMethod()))
                .withHeaders(
                        webhookDefinition.getHeaders().all().stream()
                                .map(header -> new HttpHeader(header.key(), header.values().stream()
                                        .map(value -> renderTemplate(model, value))
                                        .collect(toList()))
                                ).collect(toList())
                );

        if (webhookDefinition.getBody() != null) {
            renderedWebhookDefinition = webhookDefinition.withBody(renderTemplate(model, webhookDefinition.getBody()));
        }

        return renderedWebhookDefinition;
    }

    private String renderTemplate(Object context, String value) {
        return templateEngine.getUncachedTemplate(value).apply(context);
    }

    private static HttpUriRequest buildRequest(WebhookDefinition definition) {
        final RequestBuilder requestBuilder = RequestBuilder.create(definition.getMethod())
                .setUri(definition.getUrl());

        for (HttpHeader header : definition.getHeaders().all()) {
            for (String value : header.values()) {
                requestBuilder.addHeader(header.key(), value);
            }
        }

        if (definition.getRequestMethod().hasEntity() && definition.hasBody()) {
            requestBuilder.setEntity(new ByteArrayEntity(definition.getBinaryBody()));
        }

        return requestBuilder.build();
    }

    public static WebhookDefinition webhook() {
        return new WebhookDefinition();
    }
}
