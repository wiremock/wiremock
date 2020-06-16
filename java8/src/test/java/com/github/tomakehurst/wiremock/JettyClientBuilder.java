package com.github.tomakehurst.wiremock;

import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.Origin;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class JettyClientBuilder implements HttpProxiedClientBuilder {

    public static JettyClientBuilder INSTANCE = new JettyClientBuilder();

    @Override
    public HttpClient buildClient(String proxyScheme, int proxyPort) throws Exception {
        return url -> getHttpResponse(url, proxyScheme, proxyPort);
    }

    private static Response getHttpResponse(
        String url,
        String proxyScheme,
        int proxyPort
    ) throws Exception {
//        Security.addProvider(Conscrypt.newProvider());
        SslContextFactory sslContextFactory = new SslContextFactory.Client(true);
//        sslContextFactory.setProvider("Conscrypt");
        HttpClientTransport transport = new HttpClientTransportOverHTTP2(new HTTP2Client());
        org.eclipse.jetty.client.HttpClient httpClient = new org.eclipse.jetty.client.HttpClient(sslContextFactory);

        httpClient.setFollowRedirects(false);
        httpClient.getContentDecoderFactories().clear();
        httpClient.start();

        ProxyConfiguration proxyConfig = httpClient.getProxyConfiguration();
        HttpProxy httpProxy = new HttpProxy(new Origin.Address("localhost", proxyPort), "https".equalsIgnoreCase(proxyScheme));
        proxyConfig.getProxies().add(httpProxy);

        ContentResponse response = httpClient.GET(url);
        return new Response(response.getStatus(), response.getContentAsString());
    }

    @Override
    public String toString() {
        return "jetty client";
    }
}
