package org.wiremock.grpc;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.jetty11.Jetty11HttpServer;
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.EnumSet;

public class GrpcHttpServer extends Jetty11HttpServer {

    private final StubRequestHandler stubRequestHandler;

    public GrpcHttpServer(Options options, AdminRequestHandler adminRequestHandler, StubRequestHandler stubRequestHandler) {
        super(options, adminRequestHandler, stubRequestHandler);
        this.stubRequestHandler = stubRequestHandler;
    }

    @Override
    protected void decorateMockServiceContextBeforeConfig(ServletContextHandler mockServiceContext) {
        final GrpcFilter grpcFilter = new GrpcFilter(stubRequestHandler);
        final FilterHolder filterHolder = new FilterHolder(grpcFilter);
        mockServiceContext.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
    }
}
