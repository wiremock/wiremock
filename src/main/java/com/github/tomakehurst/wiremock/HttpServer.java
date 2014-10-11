package com.github.tomakehurst.wiremock;

public interface HttpServer {
    void start();
    void stop();
    boolean isRunning();
    int port();
    int httpsPort();
}
