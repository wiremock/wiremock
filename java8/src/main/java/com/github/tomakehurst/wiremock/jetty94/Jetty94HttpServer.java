package com.github.tomakehurst.wiremock.jetty94;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.servlet.MultipartRequestConfigurer;
import com.github.tomakehurst.wiremock.jetty9.JettyHttpServer;
import com.github.tomakehurst.wiremock.jetty9.DefaultMultipartRequestConfigurer;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class Jetty94HttpServer extends JettyHttpServer {

    public Jetty94HttpServer(Options options, AdminRequestHandler adminRequestHandler, StubRequestHandler stubRequestHandler) {
        super(options, adminRequestHandler, stubRequestHandler);
    }

    @Override
    protected SslContextFactory buildSslContextFactory() {
        return new SslContextFactory();
    }

    @Override
    protected MultipartRequestConfigurer buildMultipartRequestConfigurer() {
        return new DefaultMultipartRequestConfigurer();
    }
}
