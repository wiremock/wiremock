package com.github.tomakehurst.wiremock;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class OkHttp4ClientBuilder implements HttpProxiedClientBuilder {

    public static OkHttp4ClientBuilder INSTANCE = new OkHttp4ClientBuilder();

    @Override
    public HttpClient buildClient(String proxyScheme, int proxyPort) throws Exception {
        return url -> getHttpResponse(url, proxyScheme, proxyPort);
    }

    private static Response getHttpResponse(
        String url,
        String proxyScheme,
        int proxyPort
    ) throws Exception {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", proxyPort)))
                .build();

        Request request = new Request.Builder()
                .url(url) // This URL is served with a 1 second delay.
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            return new Response(response.code(), response.body().string());
        }
    }

    @Override
    public String toString() {
        return "okhttp 4";
    }
}
