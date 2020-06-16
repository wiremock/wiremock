package com.github.tomakehurst.wiremock;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import java.net.URI;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ApacheHC4ClientBuilder implements HttpProxiedClientBuilder {

    public static ApacheHC4ClientBuilder INSTANCE = new ApacheHC4ClientBuilder();

    @Override
    public HttpClient buildClient(String proxyScheme, int proxyPort) throws Exception {
        return url -> {
            HttpResponse httpResponse = getHttpResponse(url, proxyScheme, proxyPort);
            return new Response(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
        };
    }

    private static HttpResponse getHttpResponse(
        String url,
        String proxyScheme,
        int proxyPort
    ) throws Exception {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                .setSSLContext(SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build())
                .setSSLHostnameVerifier(new NoopHostnameVerifier());

        HttpHost proxyHost = new HttpHost("localhost", proxyPort, proxyScheme);

        org.apache.http.client.HttpClient httpClientUsingProxy = httpClientBuilder
            .setProxy(proxyHost)
            .build();

        URI targetUri = URI.create(url);
        HttpHost target = new HttpHost(targetUri.getHost(), targetUri.getPort(), targetUri.getScheme());
        HttpGet req = new HttpGet(targetUri.getPath() + (isNullOrEmpty(targetUri.getQuery()) ? "" : "?" + targetUri.getQuery()));

        return httpClientUsingProxy.execute(target, req);
    }

    @Override
    public String toString() {
        return "apache http 4";
    }
}
