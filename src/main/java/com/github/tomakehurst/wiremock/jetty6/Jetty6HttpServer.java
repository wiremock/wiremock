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
package com.github.tomakehurst.wiremock.jetty6;

import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.global.RequestDelayControl;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.http.RequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.servlet.ContentTypeSettingFilter;
import com.github.tomakehurst.wiremock.servlet.TrailingSlashFilter;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

import static com.github.tomakehurst.wiremock.core.WireMockApp.*;
import static com.github.tomakehurst.wiremock.jetty6.Jetty6HandlerDispatchingServlet.*;
import static com.google.common.collect.Maps.*;

class Jetty6HttpServer implements HttpServer {

    private static final String FILES_URL_MATCH = String.format("/%s/*", WireMockServer.FILES_ROOT);

    private final Server jettyServer;
    private final DelayableSocketConnector httpConnector;
    private final DelayableSslSocketConnector httpsConnector;

    Jetty6HttpServer(
            Options options,
            AdminRequestHandler adminRequestHandler,
            StubRequestHandler stubRequestHandler,
            RequestDelayControl requestDelayControl
    ) {

    	jettyServer = new Server();

        QueuedThreadPool threadPool = new QueuedThreadPool(options.containerThreads());
        jettyServer.setThreadPool(threadPool);

        httpConnector = createHttpConnector(
                requestDelayControl,
                options.bindAddress(),
                options.portNumber()
        );
        jettyServer.addConnector(httpConnector);

        if (options.httpsSettings().enabled()) {
            httpsConnector = createHttpsConnector(
                    requestDelayControl,
                    options.httpsSettings());
            jettyServer.addConnector(httpsConnector);
        } else {
            httpsConnector = null;
        }

        Notifier notifier = options.notifier();
        addAdminContext(
                adminRequestHandler,
                notifier
        );
        addMockServiceContext(
                stubRequestHandler,
                options.filesRoot(),
                notifier
        );
    }

    @Override
    public void start() {
        try {
            jettyServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long timeout=System.currentTimeMillis()+30000;
        while (!jettyServer.isStarted()) {
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException e) {
                // no-op
            }
            if (System.currentTimeMillis()>timeout) {
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
        return jettyServer != null && jettyServer.isRunning();
    }

    @Override
    public int port() {
        return httpConnector.getLocalPort();
    }

    @Override
    public int httpsPort() {
        return httpsConnector.getLocalPort();
    }

    private DelayableSocketConnector createHttpConnector(
            RequestDelayControl requestDelayControl,
            String bindAddress,
            int port
    ) {
        DelayableSocketConnector connector = new DelayableSocketConnector(requestDelayControl);
        connector.setHost(bindAddress);
        connector.setPort(port);
        connector.setHeaderBufferSize(8192);
        return connector;
    }

    private DelayableSslSocketConnector createHttpsConnector(
            RequestDelayControl requestDelayControl,
            HttpsSettings httpsSettings
    ) {
        DelayableSslSocketConnector connector = new DelayableSslSocketConnector(requestDelayControl);
        connector.setPort(httpsSettings.port());
        connector.setHeaderBufferSize(8192);
        connector.setKeystore(httpsSettings.keyStorePath());
        connector.setKeyPassword(httpsSettings.keyStorePassword());

        if (httpsSettings.hasTrustStore()) {
            connector.setTruststore(httpsSettings.trustStorePath());
            connector.setTrustPassword(httpsSettings.trustStorePassword());
        }

        connector.setNeedClientAuth(httpsSettings.needClientAuth());
        return connector;
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    private void addMockServiceContext(
            StubRequestHandler stubRequestHandler,
            FileSource fileSource,
            Notifier notifier
    ) {
        Context mockServiceContext = new Context(jettyServer, "/");

        Map initParams = newHashMap();
        initParams.put("org.mortbay.jetty.servlet.Default.maxCacheSize", "0");
        initParams.put("org.mortbay.jetty.servlet.Default.resourceBase", fileSource.getPath());
        initParams.put("org.mortbay.jetty.servlet.Default.dirAllowed", "false");
        mockServiceContext.setInitParams(initParams);

        mockServiceContext.addServlet(DefaultServlet.class, FILES_URL_MATCH);

        mockServiceContext.setAttribute(StubRequestHandler.class.getName(), stubRequestHandler);
        mockServiceContext.setAttribute(Notifier.KEY, notifier);
        ServletHolder servletHolder = mockServiceContext.addServlet(Jetty6HandlerDispatchingServlet.class, "/");
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

    private void addAdminContext(
            AdminRequestHandler adminRequestHandler,
            Notifier notifier
    ) {
        Context adminContext = new Context(jettyServer, ADMIN_CONTEXT_ROOT);
        ServletHolder servletHolder = adminContext.addServlet(Jetty6HandlerDispatchingServlet.class, "/");
        servletHolder.setInitParameter(RequestHandler.HANDLER_CLASS_KEY, AdminRequestHandler.class.getName());
        adminContext.setAttribute(AdminRequestHandler.class.getName(), adminRequestHandler);
        adminContext.setAttribute(Notifier.KEY, notifier);
        jettyServer.addHandler(adminContext);
    }

}
