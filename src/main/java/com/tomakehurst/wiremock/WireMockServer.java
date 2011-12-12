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
package com.tomakehurst.wiremock;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;

import com.tomakehurst.wiremock.common.FileSource;
import com.tomakehurst.wiremock.common.Log4jNotifier;
import com.tomakehurst.wiremock.common.LoggingExceptionHandler;
import com.tomakehurst.wiremock.common.Notifier;
import com.tomakehurst.wiremock.common.SingleRootFileSource;
import com.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.tomakehurst.wiremock.mapping.AdminRequestHandler;
import com.tomakehurst.wiremock.mapping.InMemoryMappings;
import com.tomakehurst.wiremock.mapping.Mappings;
import com.tomakehurst.wiremock.mapping.MockServiceRequestHandler;
import com.tomakehurst.wiremock.mapping.RequestHandler;
import com.tomakehurst.wiremock.mapping.RequestListener;
import com.tomakehurst.wiremock.servlet.BasicResponseRenderer;
import com.tomakehurst.wiremock.servlet.ContentTypeSettingFilter;
import com.tomakehurst.wiremock.servlet.HandlerDispatchingServlet;
import com.tomakehurst.wiremock.servlet.MockServiceResponseRenderer;
import com.tomakehurst.wiremock.servlet.TrailingSlashFilter;
import com.tomakehurst.wiremock.standalone.MappingsLoader;
import com.tomakehurst.wiremock.verification.InMemoryRequestJournal;

public class WireMockServer {

	public static final String FILES_ROOT = "__files";
	public static final int DEFAULT_PORT = 8080;
	private static final String FILES_URL_MATCH = String.format("/%s/*", FILES_ROOT);
	
	private Server jettyServer;
	private final Mappings mappings;
	private final InMemoryRequestJournal requestJournal;
	private final MockServiceRequestHandler mockServiceRequestHandler;
	private final AdminRequestHandler mappingRequestHandler;
	private final FileSource fileSource;
	private final GlobalSettingsHolder globalSettingsHolder;
	private final Log4jNotifier notifier;
	private final int port;
	
	public WireMockServer(final int port, final FileSource fileSource) {
		globalSettingsHolder = new GlobalSettingsHolder();
		mappings = new InMemoryMappings();
		requestJournal = new InMemoryRequestJournal();
		notifier = new Log4jNotifier();
		mockServiceRequestHandler = new MockServiceRequestHandler(mappings,
				new MockServiceResponseRenderer(fileSource.child(FILES_ROOT), globalSettingsHolder));
		mockServiceRequestHandler.setExceptionHandler(new LoggingExceptionHandler(notifier));
		mockServiceRequestHandler.addRequestListener(requestJournal);
		mappingRequestHandler = new AdminRequestHandler(mappings, requestJournal, globalSettingsHolder,
				new BasicResponseRenderer());
		this.fileSource = fileSource;
		this.port = port;
	}
	
	public WireMockServer(final int port) {
		this(port, new SingleRootFileSource("src/test/resources"));
	}
	
	public WireMockServer() {
		this(DEFAULT_PORT);
	}
	
	public void loadMappingsUsing(final MappingsLoader mappingsLoader) {
		mappingsLoader.loadMappingsInto(mappings);
	}
	
	public void addMockServiceRequestListener(final RequestListener listener) {
		mockServiceRequestHandler.addRequestListener(listener);
	}
	
	public void setVerboseLogging(final boolean verbose) {
		notifier.setVerbose(verbose);
	}
	
	public void stop() {
		try {
			jettyServer.stop();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void start() {
		jettyServer = new Server(port);
		addAdminContext();
		addMockServiceContext();

		try {
			jettyServer.start();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

    @SuppressWarnings({"rawtypes", "unchecked" })
    private void addMockServiceContext() {
        final Context mockServiceContext = new Context(jettyServer, "/");
        
        final Map initParams = newHashMap();
        initParams.put("org.mortbay.jetty.servlet.Default.maxCacheSize", "0");
        initParams.put("org.mortbay.jetty.servlet.Default.resourceBase", fileSource.getPath());
        initParams.put("org.mortbay.jetty.servlet.Default.dirAllowed", "true");
        mockServiceContext.setInitParams(initParams);
        mockServiceContext.addServlet(DefaultServlet.class, FILES_URL_MATCH);
        
		mockServiceContext.setAttribute(RequestHandler.CONTEXT_KEY, mockServiceRequestHandler);
		mockServiceContext.setAttribute(Notifier.KEY, notifier);
		mockServiceContext.addServlet(HandlerDispatchingServlet.class, "/");
		
		final MimeTypes mimeTypes = new MimeTypes();
		mimeTypes.addMimeMapping("json", "application/json");
		mimeTypes.addMimeMapping("html", "text/html");
		mimeTypes.addMimeMapping("xml", "application/xml");
		mimeTypes.addMimeMapping("txt", "text/plain");
		mockServiceContext.setMimeTypes(mimeTypes);
		
		mockServiceContext.setWelcomeFiles(new String[] { "index.json", "index.html", "index.xml", "index.txt" });
		
		mockServiceContext.addFilter(ContentTypeSettingFilter.class, FILES_URL_MATCH, Handler.FORWARD);
		mockServiceContext.addFilter(TrailingSlashFilter.class, FILES_URL_MATCH, Handler.REQUEST);
		
		jettyServer.addHandler(mockServiceContext);
    }

    private void addAdminContext() {
        final Context adminContext = new Context(jettyServer, "/__admin");
		adminContext.addServlet(HandlerDispatchingServlet.class, "/");
		adminContext.setAttribute(RequestHandler.CONTEXT_KEY, mappingRequestHandler);
		adminContext.setAttribute(Notifier.KEY, notifier);
		jettyServer.addHandler(adminContext);
    }
	
	
}
