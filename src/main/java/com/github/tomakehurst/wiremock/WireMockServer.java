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
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.global.RequestDelayControl;
import com.github.tomakehurst.wiremock.global.ThreadSafeRequestDelayControl;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.jetty.DelayableSocketConnector;
import com.github.tomakehurst.wiremock.jetty.DelayableSslSocketConnector;
import com.github.tomakehurst.wiremock.servlet.ContentTypeSettingFilter;
import com.github.tomakehurst.wiremock.servlet.HandlerDispatchingServlet;
import com.github.tomakehurst.wiremock.servlet.TrailingSlashFilter;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.stubbing.StubMappingJsonRecorder;
import com.google.common.io.Resources;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockApp.ADMIN_CONTEXT_ROOT;
import static com.github.tomakehurst.wiremock.servlet.HandlerDispatchingServlet.SHOULD_FORWARD_TO_FILES_CONTEXT;
import static com.google.common.collect.Maps.newHashMap;

public class WireMockServer {

	public static final String FILES_ROOT = "__files";
	private static final String FILES_URL_MATCH = String.format("/%s/*", FILES_ROOT);
	
	private final WireMockApp wireMockApp;
    private final AdminRequestHandler adminRequestHandler;
    private final StubRequestHandler stubRequestHandler;

	
	private Server jettyServer;
    private RequestDelayControl requestDelayControl;
	private final FileSource fileSource;
	private final Notifier notifier;
	private final int port;
    private final Integer httpsPort;

    public WireMockServer(int port, Integer httpsPort, FileSource fileSource, boolean enableBrowserProxying, ProxySettings proxySettings, Notifier notifier) {
        this.fileSource = fileSource;
        this.port = port;
        this.httpsPort = httpsPort;
        this.notifier = notifier;

        requestDelayControl = new ThreadSafeRequestDelayControl();

        wireMockApp = new WireMockApp(requestDelayControl, enableBrowserProxying);

        adminRequestHandler = new AdminRequestHandler(wireMockApp, new BasicResponseRenderer());
        stubRequestHandler = new StubRequestHandler(wireMockApp,
                new StubResponseRenderer(fileSource.child(FILES_ROOT),
                        wireMockApp.getGlobalSettingsHolder(),
                        new ProxyResponseRenderer(proxySettings)));

    }

	public WireMockServer(int port, FileSource fileSource, boolean enableBrowserProxying, ProxySettings proxySettings) {
		this(port, null, fileSource, enableBrowserProxying, proxySettings, new Log4jNotifier());
	}

    public WireMockServer(int port, FileSource fileSource, boolean enableBrowserProxying) {
        this(port, fileSource, enableBrowserProxying, ProxySettings.NO_PROXY);
    }
	
	public WireMockServer(int port) {
		this(port, new SingleRootFileSource("src/test/resources"), false);
	}

    public WireMockServer(int port, Integer httpsPort) {
        this(port, httpsPort, new SingleRootFileSource("src/test/resources"), false, ProxySettings.NO_PROXY, new Log4jNotifier());
    }
	
	public WireMockServer() {
		this(Options.DEFAULT_PORT);
	}
	
	public void loadMappingsUsing(final MappingsLoader mappingsLoader) {
		wireMockApp.loadMappingsUsing(mappingsLoader);
	}
	
	public void addMockServiceRequestListener(RequestListener listener) {
		stubRequestHandler.addRequestListener(listener);
	}
	
	public void enableRecordMappings(FileSource mappingsFileSource, FileSource filesFileSource) {
	    addMockServiceRequestListener(
                new StubMappingJsonRecorder(mappingsFileSource, filesFileSource, wireMockApp));
	    notifier.info("Recording mappings to " + mappingsFileSource.getPath());
	}
	
	public void stop() {
		try {
			jettyServer.stop();
            jettyServer.join();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void start() {
		try {
            jettyServer = new Server();
            jettyServer.addConnector(createHttpConnector());

            if (httpsPort != null) {
                jettyServer.addConnector(createHttpsConnector());
            }

            addAdminContext();
            addMockServiceContext();
			jettyServer.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    private DelayableSocketConnector createHttpConnector() {
        DelayableSocketConnector connector = new DelayableSocketConnector(requestDelayControl);
        connector.setPort(port);
        connector.setHeaderBufferSize(8192);
        return connector;
    }

    private DelayableSslSocketConnector createHttpsConnector() {
        DelayableSslSocketConnector connector = new DelayableSslSocketConnector(requestDelayControl);
        connector.setPort(httpsPort);
        connector.setHeaderBufferSize(8192);
        connector.setKeystore(Resources.getResource("keystore").toString());
        connector.setKeyPassword("password");
        return connector;
    }

    public boolean isRunning() {
        return jettyServer != null && jettyServer.isRunning();
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    private void addMockServiceContext() {
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

    private void addAdminContext() {
        Context adminContext = new Context(jettyServer, ADMIN_CONTEXT_ROOT);
		ServletHolder servletHolder = adminContext.addServlet(HandlerDispatchingServlet.class, "/");
		servletHolder.setInitParameter(RequestHandler.HANDLER_CLASS_KEY, AdminRequestHandler.class.getName());
		adminContext.setAttribute(AdminRequestHandler.class.getName(), adminRequestHandler);
		adminContext.setAttribute(Notifier.KEY, notifier);
		jettyServer.addHandler(adminContext);
    }
    
    
}
