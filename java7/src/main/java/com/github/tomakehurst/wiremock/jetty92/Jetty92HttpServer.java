package com.github.tomakehurst.wiremock.jetty92;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.jetty9.JettyHttpServer;
import com.github.tomakehurst.wiremock.servlet.MultipartRequestConfigurer;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class Jetty92HttpServer extends JettyHttpServer {

    public Jetty92HttpServer(Options options, AdminRequestHandler adminRequestHandler, StubRequestHandler stubRequestHandler) {
        super(options, adminRequestHandler, stubRequestHandler);
    }

    @Override
    protected SslContextFactory buildSslContextFactory() {
        return new CustomizedSslContextFactory();
    }

    @Override
    protected MultipartRequestConfigurer buildMultipartRequestConfigurer() {
        return new Jetty92MultipartRequestConfigurer();
    }
}
