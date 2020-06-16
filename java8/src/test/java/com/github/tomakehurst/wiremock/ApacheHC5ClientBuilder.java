package com.github.tomakehurst.wiremock;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContexts;

import java.net.URI;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ApacheHC5ClientBuilder implements HttpProxiedClientBuilder {

    public static ApacheHC5ClientBuilder INSTANCE = new ApacheHC5ClientBuilder();

    @Override
    public HttpClient buildClient(String proxyScheme, int proxyPort) throws Exception {
        return url -> {
            ClassicHttpResponse httpResponse = getHttpResponse(url, proxyScheme, proxyPort);
            return new Response(httpResponse.getCode(), EntityUtils.toString(httpResponse.getEntity()));
        };
    }

    private static ClassicHttpResponse getHttpResponse(
        String url,
        String proxyScheme,
        int proxyPort
    ) throws Exception {
        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build())
                .setHostnameVerifier(new NoopHostnameVerifier())
                .build();

        HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                .setConnectionManager(cm);

        HttpHost proxyHost = new HttpHost(proxyScheme, "localhost", proxyPort);

        org.apache.hc.client5.http.classic.HttpClient httpClientUsingProxy = httpClientBuilder
            .setProxy(proxyHost)
            .build();

        URI targetUri = URI.create(url);
        HttpHost target = new HttpHost(targetUri.getScheme(), targetUri.getHost(), targetUri.getPort());
        HttpGet req = new HttpGet(targetUri.getPath() + (isNullOrEmpty(targetUri.getQuery()) ? "" : "?" + targetUri.getQuery()));

        return httpClientUsingProxy.execute(target, req);
    }

    @Override
    public String toString() {
        return "apache http 5";
    }
}
