package com.github.tomakehurst.wiremock;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.ssl.SSLContexts;

import java.net.URI;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ApacheHC5AsyncClientBuilder implements HttpProxiedClientBuilder {

    public static ApacheHC5AsyncClientBuilder INSTANCE = new ApacheHC5AsyncClientBuilder();

    @Override
    public HttpClient buildClient(String proxyScheme, int proxyPort) throws Exception {
        return url -> {
            SimpleHttpResponse httpResponse = getHttpResponse(url, proxyScheme, proxyPort);
            return new Response(httpResponse.getCode(), httpResponse.getBodyText());
        };
    }

    private static SimpleHttpResponse getHttpResponse(
        String url,
        String proxyScheme,
        int proxyPort
    ) throws Exception {
        TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                .setSslContext(SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build())
                .setHostnameVerifier(new NoopHostnameVerifier())
                .setTlsDetailsFactory(sslEngine -> new TlsDetails(sslEngine.getSession(), sslEngine.getApplicationProtocol()))
                .build();

        PoolingAsyncClientConnectionManager cm = PoolingAsyncClientConnectionManagerBuilder.create()
                .setTlsStrategy(tlsStrategy)
                .build();

        HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom()
                .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
                .setConnectionManager(cm);

        HttpHost proxyHost = new HttpHost(proxyScheme, "localhost", proxyPort);

        try (CloseableHttpAsyncClient httpClientUsingProxy = clientBuilder
            .setProxy(proxyHost)
            .build()) {

            httpClientUsingProxy.start();

            URI targetUri = URI.create(url);
            HttpHost target = new HttpHost(targetUri.getScheme(), targetUri.getHost(), targetUri.getPort());
            SimpleHttpRequest request = SimpleHttpRequests.get(target, targetUri.getPath() + (isNullOrEmpty(targetUri.getQuery()) ? "" : "?" + targetUri.getQuery()));

            return httpClientUsingProxy.execute(request, new NoOpFutureCallback<>()).get();
        }
    }

    private static class NoOpFutureCallback<T> implements FutureCallback<T> {
        @Override
        public void completed(final T response) {
        }

        @Override
        public void failed(final Exception ex) {
        }

        @Override
        public void cancelled() {
        }
    }

    @Override
    public String toString() {
        return "apache http 5 async";
    }
}
