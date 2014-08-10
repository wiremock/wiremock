package com.github.tomakehurst.wiremock.jetty;

import com.github.tomakehurst.wiremock.HttpServer;
import com.github.tomakehurst.wiremock.HttpServerFactory;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.global.RequestDelayControl;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;

public class JettyHttpServerFactory implements HttpServerFactory {
    @Override
    public HttpServer buildHttpServer(
            Options options,
            AdminRequestHandler adminRequestHandler,
            StubRequestHandler stubRequestHandler,
            RequestDelayControl requestDelayControl
    ) {
        return new JettyHttpServer(
                options,
                adminRequestHandler,
                stubRequestHandler,
                requestDelayControl
        );
    }
}
