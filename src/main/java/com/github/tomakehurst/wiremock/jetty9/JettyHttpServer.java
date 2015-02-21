/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.jetty9;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.JettySettings;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.global.RequestDelayControl;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.RequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.servlet.ContentTypeSettingFilter;
import com.github.tomakehurst.wiremock.servlet.TrailingSlashFilter;

import static com.github.tomakehurst.wiremock.core.WireMockApp.ADMIN_CONTEXT_ROOT;
import static com.github.tomakehurst.wiremock.jetty9.JettyHandlerDispatchingServlet.SHOULD_FORWARD_TO_FILES_CONTEXT;

class JettyHttpServer implements HttpServer {

    private static final String FILES_URL_MATCH = String.format("/%s/*", WireMockServer.FILES_ROOT);

    private final Server jettyServer;
    private final ServerConnector httpConnector;
    private final ServerConnector httpsConnector;

    JettyHttpServer(
            Options options,
            AdminRequestHandler adminRequestHandler,
            StubRequestHandler stubRequestHandler,
            RequestDelayControl requestDelayControl
    ) {

        QueuedThreadPool threadPool = new QueuedThreadPool(options.containerThreads());
        jettyServer = new Server(threadPool);

        httpConnector = createHttpConnector(
                requestDelayControl,
                options.bindAddress(),
                options.portNumber(),
                options.jettySettings()
        );
        jettyServer.addConnector(httpConnector);

        if (options.httpsSettings().enabled()) {
            httpsConnector = createHttpsConnector(
                    requestDelayControl,
                    options.httpsSettings(),
                    options.jettySettings());
            jettyServer.addConnector(httpsConnector);
        } else {
            httpsConnector = null;
        }

        Notifier notifier = options.notifier();
        ServletContextHandler adminContext = addAdminContext(
                adminRequestHandler,
                notifier
        );
        ServletContextHandler mockServiceContext = addMockServiceContext(
                stubRequestHandler,
                options.filesRoot(),
                notifier
        );

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[]{adminContext, mockServiceContext});
        jettyServer.setHandler(handlers);
    }

    @Override
    public void start() {
        try {
            jettyServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long timeout = System.currentTimeMillis() + 30000;
        while (!jettyServer.isStarted()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // no-op
            }
            if (System.currentTimeMillis() > timeout) {
                throw new RuntimeException("Server took too long to start up.");
            }
        }
    }

    @Override
    public void stop() {
        try {
            jettyServer.stop();
            jettyServer.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isRunning() {
        return jettyServer.isRunning();
    }

    @Override
    public int port() {
        return httpConnector.getLocalPort();
    }

    @Override
    public int httpsPort() {
        return httpsConnector.getLocalPort();
    }

    private ServerConnector createHttpConnector(
            RequestDelayControl requestDelayControl,
            String bindAddress,
            int port,
            JettySettings jettySettings) {

        ServerConnector connector = createServerConnector(
                jettySettings,
                port,
                new FaultInjectingHttpConnectionFactory(
                        new HttpConfiguration(),
                        requestDelayControl
                )
        );
        connector.setHost(bindAddress);
        return connector;
    }

    private ServerConnector createHttpsConnector(
            RequestDelayControl requestDelayControl,
            HttpsSettings httpsSettings,
            JettySettings jettySettings) {

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(httpsSettings.keyStorePath());
        sslContextFactory.setKeyManagerPassword(httpsSettings.keyStorePassword());
        if (httpsSettings.hasTrustStore()) {
            sslContextFactory.setTrustStorePath(httpsSettings.trustStorePath());
            sslContextFactory.setTrustStorePassword(httpsSettings.trustStorePassword());
        }
        sslContextFactory.setNeedClientAuth(httpsSettings.needClientAuth());

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.addCustomizer(new SecureRequestCustomizer());

        final int port = httpsSettings.port();


        return createServerConnector(
                jettySettings,
                port,
                new SslConnectionFactory(
                        sslContextFactory,
                        "http/1.1"
                ),
                new FaultInjectingHttpConnectionFactory(
                        httpConfig,
                        requestDelayControl
                )
        );
    }

    private ServerConnector createServerConnector(JettySettings jettySettings, int port, ConnectionFactory... connectionFactories) {
        int acceptors = jettySettings.getAcceptors().or(-1);
        ServerConnector connector = new ServerConnector(
                jettyServer,
                null,
                null,
                null,
                acceptors,
                -1,
                connectionFactories
        );
        connector.setPort(port);
        setJettySettings(jettySettings, connector);
        return connector;
    }

    private void setJettySettings(JettySettings jettySettings, ServerConnector connector) {
        if (jettySettings.getAcceptQueueSize().isPresent()) {
            connector.setAcceptQueueSize(jettySettings.getAcceptQueueSize().get());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    private ServletContextHandler addMockServiceContext(
            StubRequestHandler stubRequestHandler,
            FileSource fileSource,
            Notifier notifier
    ) {
        ServletContextHandler mockServiceContext = new ServletContextHandler(jettyServer, "/");

        mockServiceContext.setInitParameter("org.eclipse.jetty.servlet.Default.maxCacheSize", "0");
        mockServiceContext.setInitParameter("org.eclipse.jetty.servlet.Default.resourceBase", fileSource.getPath());
        mockServiceContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

        mockServiceContext.addServlet(DefaultServlet.class, FILES_URL_MATCH);

        mockServiceContext.setAttribute(StubRequestHandler.class.getName(), stubRequestHandler);
        mockServiceContext.setAttribute(Notifier.KEY, notifier);
        ServletHolder servletHolder = mockServiceContext.addServlet(JettyHandlerDispatchingServlet.class, "/");
        servletHolder.setInitParameter(RequestHandler.HANDLER_CLASS_KEY, StubRequestHandler.class.getName());
        servletHolder.setInitParameter(SHOULD_FORWARD_TO_FILES_CONTEXT, "true");

        MimeTypes mimeTypes = new MimeTypes();
        mimeTypes.addMimeMapping("json", "application/json");
        mimeTypes.addMimeMapping("html", "text/html");
        mimeTypes.addMimeMapping("xml", "application/xml");
        mimeTypes.addMimeMapping("txt", "text/plain");
        mockServiceContext.setMimeTypes(mimeTypes);

        mockServiceContext.setWelcomeFiles(new String[] { "index.json", "index.html", "index.xml", "index.txt" });

        mockServiceContext.addFilter(ContentTypeSettingFilter.class, FILES_URL_MATCH, EnumSet.of(DispatcherType.FORWARD));
        mockServiceContext.addFilter(TrailingSlashFilter.class, FILES_URL_MATCH, EnumSet.allOf(DispatcherType.class));

        return mockServiceContext;
    }

    private ServletContextHandler addAdminContext(
            AdminRequestHandler adminRequestHandler,
            Notifier notifier
    ) {
        ServletContextHandler adminContext = new ServletContextHandler(jettyServer, ADMIN_CONTEXT_ROOT);
        ServletHolder servletHolder = adminContext.addServlet(JettyHandlerDispatchingServlet.class, "/");
        servletHolder.setInitParameter(RequestHandler.HANDLER_CLASS_KEY, AdminRequestHandler.class.getName());
        adminContext.setAttribute(AdminRequestHandler.class.getName(), adminRequestHandler);
        adminContext.setAttribute(Notifier.KEY, notifier);
        return adminContext;
    }

}
