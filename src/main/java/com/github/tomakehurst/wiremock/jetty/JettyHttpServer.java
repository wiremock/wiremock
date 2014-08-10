package com.github.tomakehurst.wiremock.jetty;

import com.github.tomakehurst.wiremock.HttpServer;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.global.RequestDelayControl;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.RequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.servlet.ContentTypeSettingFilter;
import com.github.tomakehurst.wiremock.servlet.HandlerDispatchingServlet;
import com.github.tomakehurst.wiremock.servlet.TrailingSlashFilter;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockApp.ADMIN_CONTEXT_ROOT;
import static com.github.tomakehurst.wiremock.servlet.HandlerDispatchingServlet.SHOULD_FORWARD_TO_FILES_CONTEXT;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;

class JettyHttpServer implements HttpServer {

    private static final String FILES_URL_MATCH = String.format("/%s/*", WireMockServer.FILES_ROOT);

    private final Notifier notifier;
    private final String bindAddress;
    private final int port;
    private final FileSource fileSource;
    private final int httpsPort;
    private final String keystorePath;
    private final boolean httpsEnabled;

    private final AdminRequestHandler adminRequestHandler;
    private final StubRequestHandler stubRequestHandler;
    private final RequestDelayControl requestDelayControl;

    private Server jettyServer;
    private DelayableSocketConnector httpConnector;
    private DelayableSslSocketConnector httpsConnector;

    JettyHttpServer(
            Options options,
            AdminRequestHandler adminRequestHandler,
            StubRequestHandler stubRequestHandler,
            RequestDelayControl requestDelayControl) {
        this.adminRequestHandler = adminRequestHandler;
        this.stubRequestHandler = stubRequestHandler;
        this.requestDelayControl = requestDelayControl;
        this.fileSource = options.filesRoot();
        this.port = options.portNumber();
        this.bindAddress = options.bindAddress();
        this.notifier = options.notifier();
        this.httpsPort = options.httpsSettings().port();
        this.keystorePath = options.httpsSettings().keyStorePath();
        this.httpsEnabled = options.httpsSettings().enabled();
    }

    @Override
    public void start() {
        try {
            jettyServer = new Server();
            httpConnector = createHttpConnector(requestDelayControl);
            jettyServer.addConnector(httpConnector);

            if (httpsEnabled) {
                httpsConnector = createHttpsConnector(requestDelayControl);
                jettyServer.addConnector(httpsConnector);
            }

            addAdminContext(adminRequestHandler);
            addMockServiceContext(stubRequestHandler);
            jettyServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            httpConnector = null;
            httpsConnector = null;
            jettyServer.stop();
            jettyServer.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isRunning() {
        return jettyServer != null && jettyServer.isRunning();
    }

    @Override
    public int port() {
        checkState(httpConnector != null, "Not listening on HTTP port. The WireMock server is most likely stopped");
        return httpConnector.getLocalPort();
    }

    @Override
    public int httpsPort() {
        checkState(httpsConnector != null, "Not listening on HTTPS port. Either HTTPS is not enabled or the WireMock server is stopped.");
        return httpsConnector.getLocalPort();
    }

    private DelayableSocketConnector createHttpConnector(RequestDelayControl requestDelayControl) {
        DelayableSocketConnector connector = new DelayableSocketConnector(requestDelayControl);
        connector.setHost(bindAddress);
        connector.setPort(port);
        connector.setHeaderBufferSize(8192);
        return connector;
    }

    private DelayableSslSocketConnector createHttpsConnector(RequestDelayControl requestDelayControl) {
        DelayableSslSocketConnector connector = new DelayableSslSocketConnector(requestDelayControl);
        connector.setPort(httpsPort);
        connector.setHeaderBufferSize(8192);
        connector.setKeystore(keystorePath);
        connector.setKeyPassword("password");
        return connector;
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    private void addMockServiceContext(StubRequestHandler stubRequestHandler) {
        Context mockServiceContext = new Context(jettyServer, "/");

        Map initParams = newHashMap();
        initParams.put("org.mortbay.jetty.servlet.Default.maxCacheSize", "0");
        initParams.put("org.mortbay.jetty.servlet.Default.resourceBase", fileSource.getPath());
        initParams.put("org.mortbay.jetty.servlet.Default.dirAllowed", "false");
        mockServiceContext.setInitParams(initParams);

        mockServiceContext.addServlet(DefaultServlet.class, FILES_URL_MATCH);

        mockServiceContext.setAttribute(StubRequestHandler.class.getName(), stubRequestHandler);
        mockServiceContext.setAttribute(Notifier.KEY, notifier);
        ServletHolder servletHolder = mockServiceContext.addServlet(HandlerDispatchingServlet.class, "/");
        servletHolder.setInitParameter(RequestHandler.HANDLER_CLASS_KEY, StubRequestHandler.class.getName());
        servletHolder.setInitParameter(SHOULD_FORWARD_TO_FILES_CONTEXT, "true");

        MimeTypes mimeTypes = new MimeTypes();
        mimeTypes.addMimeMapping("json", "application/json");
        mimeTypes.addMimeMapping("html", "text/html");
        mimeTypes.addMimeMapping("xml", "application/xml");
        mimeTypes.addMimeMapping("txt", "text/plain");
        mockServiceContext.setMimeTypes(mimeTypes);

        mockServiceContext.setWelcomeFiles(new String[] { "index.json", "index.html", "index.xml", "index.txt" });

        mockServiceContext.addFilter(ContentTypeSettingFilter.class, FILES_URL_MATCH, Handler.FORWARD);
        mockServiceContext.addFilter(TrailingSlashFilter.class, FILES_URL_MATCH, Handler.ALL);

        jettyServer.addHandler(mockServiceContext);
    }

    private void addAdminContext(AdminRequestHandler adminRequestHandler) {
        Context adminContext = new Context(jettyServer, ADMIN_CONTEXT_ROOT);
        ServletHolder servletHolder = adminContext.addServlet(HandlerDispatchingServlet.class, "/");
        servletHolder.setInitParameter(RequestHandler.HANDLER_CLASS_KEY, AdminRequestHandler.class.getName());
        adminContext.setAttribute(AdminRequestHandler.class.getName(), adminRequestHandler);
        adminContext.setAttribute(Notifier.KEY, notifier);
        jettyServer.addHandler(adminContext);
    }

}
