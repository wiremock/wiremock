package com.github.tomakehurst.wiremock.extension.webhooks;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.http.HttpClientFactory.getHttpRequestFor;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Webhooks extends PostServeAction {

    private final ScheduledExecutorService scheduler;
    private final HttpClient httpClient;

    public Webhooks() {
        scheduler = Executors.newScheduledThreadPool(10);
        httpClient = HttpClientFactory.createClient();
    }

    @Override
    public String getName() {
        return "webhook";
    }

    @Override
    public void doAction(ServeEvent serveEvent, Admin admin, Parameters parameters) {
        final WebhookDefinition definition = parameters.as(WebhookDefinition.class);

        scheduler.schedule(
            new Runnable() {
                @Override
                public void run() {
                    HttpUriRequest request = getHttpRequestFor(
                            definition.getMethod(),
                            definition.getUrl().toString()
                    );

                    for (HttpHeader header: definition.getHeaders().all()) {
                        request.addHeader(header.key(), header.firstValue());
                    }

                    if (definition.getMethod().hasEntity()) {
                        HttpEntityEnclosingRequestBase entityRequest = (HttpEntityEnclosingRequestBase) request;
                        entityRequest.setEntity(new ByteArrayEntity(definition.getBinaryBody()));
                    }

                    try {
                        HttpResponse response = httpClient.execute(request);
                        notifier().info(
                            String.format("Webhook %s request to %s returned status %s\n\n%s",
                                definition.getMethod(),
                                definition.getUrl(),
                                response.getStatusLine(),
                                EntityUtils.toString(response.getEntity())
                            )
                        );
                    } catch (IOException e) {
                        throwUnchecked(e);
                    }
                }
            },
            0L,
            SECONDS
        );
    }

    public static WebhookDefinition webhook() {
        return new WebhookDefinition();
    }
}
