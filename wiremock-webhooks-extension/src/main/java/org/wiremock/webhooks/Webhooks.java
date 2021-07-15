package org.wiremock.webhooks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.wiremock.webhooks.interceptors.WebhookTransformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.http.HttpClientFactory.getHttpRequestFor;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Webhooks extends PostServeAction {

    private final ScheduledExecutorService scheduler;
    private final CloseableHttpClient httpClient;
    private final List<WebhookTransformer> transformers;

    private Webhooks(
            ScheduledExecutorService scheduler,
            CloseableHttpClient httpClient,
            List<WebhookTransformer> transformers) {
      this.scheduler = scheduler;
      this.httpClient = httpClient;
      this.transformers = transformers;
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

        scheduler.schedule(
                () -> {
                    WebhookDefinition definition;
                    HttpUriRequest request;
                    try {
                        definition = WebhookDefinition.from(parameters);
                        for (WebhookTransformer transformer : transformers) {
                            definition = transformer.transform(serveEvent, definition);
                        }
                        request = buildRequest(definition);
                    } catch (Exception e) {
                        notifier().error("Exception thrown while configuring webhook", e);
                        return;
                    }

                    try (CloseableHttpResponse response = httpClient.execute(request)) {
                        notifier.info(
                            String.format("Webhook %s request to %s returned status %s\n\n%s",
                                definition.getMethod(),
                                definition.getUrl(),
                                response.getStatusLine(),
                                EntityUtils.toString(response.getEntity())
                            )
                        );
                    } catch (Exception e) {
                        notifier().error(String.format("Failed to fire webhook %s %s", definition.getMethod(), definition.getUrl()), e);
                    }
                },
            0L,
            SECONDS
        );
    }

    private static HttpUriRequest buildRequest(WebhookDefinition definition) {
        final RequestBuilder requestBuilder = RequestBuilder.create(definition.getMethod().getName())
                .setUri(definition.getUrl());

        for (HttpHeader header: definition.getHeaders().all()) {
            requestBuilder.addHeader(header.key(), header.firstValue());
        }

        if (definition.getMethod().hasEntity()) {
            requestBuilder.setEntity(new ByteArrayEntity(definition.getBinaryBody()));
        }

        return requestBuilder.build();
    }

    public static WebhookDefinition webhook() {
        return new WebhookDefinition();
    }
}
