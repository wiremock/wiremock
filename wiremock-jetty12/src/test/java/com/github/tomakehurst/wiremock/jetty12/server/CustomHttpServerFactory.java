package com.github.tomakehurst.wiremock.jetty12.server;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;

public class CustomHttpServerFactory implements HttpServerFactory {

    @Override
    public String getName() {
        return HttpServerFactory.super.getName();
    }

    @Override
    public HttpServer buildHttpServer(Options options, AdminRequestHandler adminRequestHandler, StubRequestHandler stubRequestHandler) {
        return new CustomHttpServer(options, adminRequestHandler, stubRequestHandler);
    }
}
