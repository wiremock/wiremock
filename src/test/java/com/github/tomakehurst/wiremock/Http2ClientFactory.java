package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.Exceptions;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class Http2ClientFactory {

    public static HttpClient create() {
        SslContextFactory sslContextFactory = new SslContextFactory.Client(true);
        sslContextFactory.setProvider("Conscrypt");
        HttpClientTransport transport = new HttpClientTransportOverHTTP2(
                new HTTP2Client());
        HttpClient httpClient = new HttpClient(transport, sslContextFactory);

        httpClient.setFollowRedirects(false);
        try {
            httpClient.start();
        } catch (Exception e) {
            return Exceptions.throwUnchecked(e, HttpClient.class);
        }

        return httpClient;
    }
}
