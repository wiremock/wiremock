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

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Log4jNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.mapping.*;
import com.github.tomakehurst.wiremock.servlet.ContentTypeSettingFilter;
import com.github.tomakehurst.wiremock.servlet.HandlerDispatchingServlet;
import com.github.tomakehurst.wiremock.servlet.TrailingSlashFilter;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.Map;

import static com.github.tomakehurst.wiremock.WireMockApp.ADMIN_CONTEXT_ROOT;
import static com.github.tomakehurst.wiremock.servlet.HandlerDispatchingServlet.SHOULD_FORWARD_TO_FILES_CONTEXT;
import static com.google.common.collect.Maps.newHashMap;

public class WireMockServer {

	public static final String FILES_ROOT = "__files";
	public static final int DEFAULT_PORT = 8080;
	private static final String FILES_URL_MATCH = String.format("/%s/*", FILES_ROOT);
	
	private final WireMockApp wireMockApp;
	
	private Server jettyServer;
	private final FileSource fileSource;
	private final Log4jNotifier notifier;
	private final int port;
	
	public WireMockServer(int port, FileSource fileSource, boolean enableBrowserProxying) {
		notifier = new Log4jNotifier();
		this.fileSource = fileSource;
		this.port = port;
		
		wireMockApp = new WireMockApp(fileSource, notifier, enableBrowserProxying);
	}
	
	public WireMockServer(int port) {
		this(port, new SingleRootFileSource("src/test/resources"), false);
	}
	
	public WireMockServer() {
		this(DEFAULT_PORT);
	}
	
	public void loadMappingsUsing(final MappingsLoader mappingsLoader) {
		wireMockApp.loadMappingsUsing(mappingsLoader);
	}
	
	public void addMockServiceRequestListener(RequestListener listener) {
		wireMockApp.addMockServiceRequestListener(listener);
	}
	
	public void setVerboseLogging(boolean verbose) {
		notifier.setVerbose(verbose);
		if (verbose) {
		    notifier.info("Verbose logging enabled");
		}
	}
	
	public void enableRecordMappings(FileSource mappingsFileSource, FileSource filesFileSource) {
	    addMockServiceRequestListener(
                new MappingFileWriterListener(mappingsFileSource, filesFileSource, wireMockApp.getRequestJournal()));
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
		jettyServer = new Server(port);
        jettyServer.getConnectors()[0].setRequestHeaderSize(8192);
        jettyServer.getConnectors()[0].setResponseHeaderSize(8192);
		addAdminContext();
		addMockServiceContext();

		try {
			jettyServer.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    @SuppressWarnings({"rawtypes", "unchecked" })
    private void addMockServiceContext() {
        ContextHandler mockServiceContext = new ContextHandler(jettyServer, "/");
        
        mockServiceContext.setInitParameter("org.mortbay.jetty.servlet.Default.maxCacheSize", "0");
        mockServiceContext.setInitParameter("org.mortbay.jetty.servlet.Default.resourceBase", fileSource.getPath());
        mockServiceContext.setInitParameter("org.mortbay.jetty.servlet.Default.dirAllowed", "false");

        ServletContextHandler handler = new ServletContextHandler();
        handler.addServlet(DefaultServlet.class, FILES_URL_MATCH);
        
		mockServiceContext.setAttribute(MockServiceRequestHandler.class.getName(), wireMockApp.getMockServiceRequestHandler());
		mockServiceContext.setAttribute(Notifier.KEY, notifier);

        ServletHolder servletHolder = handler.addServlet(HandlerDispatchingServlet.class, "/");
		servletHolder.setInitParameter(RequestHandler.HANDLER_CLASS_KEY, MockServiceRequestHandler.class.getName());
		servletHolder.setInitParameter(SHOULD_FORWARD_TO_FILES_CONTEXT, "true");
		
		MimeTypes mimeTypes = new MimeTypes();
		mimeTypes.addMimeMapping("json", "application/json");
		mimeTypes.addMimeMapping("html", "text/html");
		mimeTypes.addMimeMapping("xml", "application/xml");
		mimeTypes.addMimeMapping("txt", "text/plain");
		mockServiceContext.setMimeTypes(mimeTypes);
		
		mockServiceContext.setWelcomeFiles(new String[] { "index.json", "index.html", "index.xml", "index.txt" });

        handler.addFilter(ContentTypeSettingFilter.class, FILES_URL_MATCH, EnumSet.of(DispatcherType.FORWARD));
        handler.addFilter(TrailingSlashFilter.class, FILES_URL_MATCH,
                EnumSet.of(DispatcherType.FORWARD,
                           DispatcherType.INCLUDE,
                           DispatcherType.REQUEST,
                           DispatcherType.ERROR));
		
		jettyServer.addBean(handler);
    }

    private void addAdminContext() {
        ContextHandler adminContext = new ContextHandler(jettyServer, ADMIN_CONTEXT_ROOT);

        ServletContextHandler handler = new ServletContextHandler();
		ServletHolder servletHolder = handler.addServlet(HandlerDispatchingServlet.class, "/");
		servletHolder.setInitParameter(RequestHandler.HANDLER_CLASS_KEY, AdminRequestHandler.class.getName());
		adminContext.setAttribute(AdminRequestHandler.class.getName(), wireMockApp.getAdminRequestHandler());
		adminContext.setAttribute(Notifier.KEY, notifier);
		jettyServer.addBean(handler);
    }
    
    
}
