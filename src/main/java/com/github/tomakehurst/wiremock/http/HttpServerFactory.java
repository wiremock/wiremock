package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.global.RequestDelayControl;

public interface HttpServerFactory {

    HttpServer buildHttpServer(
            Options options,
            AdminRequestHandler adminRequestHandler,
            StubRequestHandler stubRequestHandler,
            RequestDelayControl requestDelayControl
    );
}
