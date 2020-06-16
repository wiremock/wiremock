package com.github.tomakehurst.wiremock;

public interface HttpProxiedClientBuilder {
    HttpClient buildClient(String proxyScheme, int proxyPort) throws Exception;
}
