package com.github.tomakehurst.wiremock.direct;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;

public class DirectCallHttpServerFactory implements HttpServerFactory {
    private DirectCallHttpServer lastBuilt;

    @Override
    public HttpServer buildHttpServer(Options options, AdminRequestHandler adminRequestHandler, StubRequestHandler stubRequestHandler) {
        lastBuilt = new DirectCallHttpServer(options, adminRequestHandler, stubRequestHandler);
        return lastBuilt;
    }

    /**
     * Returns the most recently created {@link DirectCallHttpServer}.
     *
     * @return the {@link DirectCallHttpServer}
     */
    public DirectCallHttpServer getHttpServer() {
        return lastBuilt;
    }
}
