package com.github.tomakehurst.wiremock.jetty12.server;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.jetty12.Jetty12HttpServer;

public class CustomHttpServer extends Jetty12HttpServer {
    public CustomHttpServer(Options options, AdminRequestHandler adminRequestHandler, StubRequestHandler stubRequestHandler) {
        super(options, adminRequestHandler, stubRequestHandler);
    }
}
