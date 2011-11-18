package com.tomakehurst.wiremock;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;

import com.tomakehurst.wiremock.common.FileSource;
import com.tomakehurst.wiremock.common.SingleRootFileSource;
import com.tomakehurst.wiremock.mapping.AdminRequestHandler;
import com.tomakehurst.wiremock.mapping.InMemoryMappings;
import com.tomakehurst.wiremock.mapping.Mappings;
import com.tomakehurst.wiremock.mapping.MockServiceRequestHandler;
import com.tomakehurst.wiremock.mapping.RequestHandler;
import com.tomakehurst.wiremock.servlet.ContentTypeSettingFilter;
import com.tomakehurst.wiremock.servlet.FileBodyLoadingResponseRenderer;
import com.tomakehurst.wiremock.servlet.HandlerDispatchingServlet;
import com.tomakehurst.wiremock.servlet.ResponseRenderer;
import com.tomakehurst.wiremock.standalone.JsonFileMappingsLoader;
import com.tomakehurst.wiremock.standalone.MappingsLoader;
import com.tomakehurst.wiremock.verification.InMemoryRequestJournal;

public class WireMockServer {

	public static final String FILES_ROOT = "__files";
	private static final String FILES_URL_MATCH = String.format("/%s/*", FILES_ROOT);
    private static final int DEFAULT_PORT = 8080;
	private static final int PORT_NUMBER_ARG = 0;
	
	private Server jettyServer;
	private final Mappings mappings;
	private final InMemoryRequestJournal requestJournal;
	private final RequestHandler mockServiceRequestHandler;
	private final RequestHandler mappingRequestHandler;
	private final FileBodyLoadingResponseRenderer responseRenderer;
	private final int port;
	
	public WireMockServer(int port, FileSource bodyFileSource) {
		mappings = new InMemoryMappings();
		requestJournal = new InMemoryRequestJournal();
		mockServiceRequestHandler = new MockServiceRequestHandler(mappings);
		mockServiceRequestHandler.addRequestListener(requestJournal);
		mappingRequestHandler = new AdminRequestHandler(mappings, requestJournal);
		responseRenderer = new FileBodyLoadingResponseRenderer(bodyFileSource);
		this.port = port;
	}
	
	public WireMockServer(int port) {
		this(port, new SingleRootFileSource("src/test/resources"));
	}
	
	public WireMockServer() {
		this(DEFAULT_PORT);
	}
	
	public void stop() {
		try {
			jettyServer.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void start() {
		jettyServer = new Server(port);
		addAdminContext();
		addSiteContext();
		addMockServiceContext();

		try {
			jettyServer.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    @SuppressWarnings({"rawtypes", "unchecked" })
    private void addSiteContext() {
        Context siteContext = new Context(jettyServer, "/site");
        Map initParams = newHashMap();
        initParams.put("org.mortbay.jetty.servlet.Default.maxCacheSize", "0");
        initParams.put("org.mortbay.jetty.servlet.Default.resourceBase", "site");
        initParams.put("org.mortbay.jetty.servlet.Default.dirAllowed", "true");
        siteContext.setInitParams(initParams);
        siteContext.addServlet(DefaultServlet.class, "/");
		jettyServer.addHandler(siteContext);
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    private void addMockServiceContext() {
        Context mockServiceContext = new Context(jettyServer, "/");
        
        Map initParams = newHashMap();
        initParams.put("org.mortbay.jetty.servlet.Default.maxCacheSize", "0");
        initParams.put("org.mortbay.jetty.servlet.Default.resourceBase", ".");
        initParams.put("org.mortbay.jetty.servlet.Default.dirAllowed", "true");
        mockServiceContext.setInitParams(initParams);
        mockServiceContext.addServlet(DefaultServlet.class, FILES_URL_MATCH);
        
		mockServiceContext.setAttribute(RequestHandler.CONTEXT_KEY, mockServiceRequestHandler);
		mockServiceContext.setAttribute(ResponseRenderer.CONTEXT_KEY, responseRenderer);
		mockServiceContext.addServlet(HandlerDispatchingServlet.class, "/");
		
		MimeTypes mimeTypes = new MimeTypes();
		mimeTypes.addMimeMapping("json", "application/json");
		mimeTypes.addMimeMapping("html", "text/html");
		mimeTypes.addMimeMapping("xml", "application/xml");
		mimeTypes.addMimeMapping("txt", "text/plain");
		mockServiceContext.setMimeTypes(mimeTypes);
		
		mockServiceContext.setWelcomeFiles(new String[] { "index.json", "index.html", "index.xml", "index.txt" });
		
		mockServiceContext.addFilter(ContentTypeSettingFilter.class, "/__files/*", Handler.ALL);
		
		jettyServer.addHandler(mockServiceContext);
    }

    private void addAdminContext() {
        Context adminContext = new Context(jettyServer, "/__admin");
		adminContext.addServlet(HandlerDispatchingServlet.class, "/");
		adminContext.setAttribute(RequestHandler.CONTEXT_KEY, mappingRequestHandler);
		adminContext.setAttribute(ResponseRenderer.CONTEXT_KEY, responseRenderer);
		jettyServer.addHandler(adminContext);
    }
	
	public void loadMappingsUsing(final MappingsLoader mappingsLoader) {
		mappingsLoader.loadMappingsInto(mappings);
	}
	
	public static void main(String... args) {
		FileSource bodyFileSource = new SingleRootFileSource(FILES_ROOT);
		bodyFileSource.createIfNecessary();
		new SingleRootFileSource("site").createIfNecessary();
		
		WireMockServer wireMockServer;
		if (args.length > 0) {
			int port = Integer.parseInt(args[PORT_NUMBER_ARG]);
			wireMockServer = new WireMockServer(port, bodyFileSource);
		} else {
			wireMockServer = new WireMockServer(DEFAULT_PORT, bodyFileSource);
		}
		
		MappingsLoader mappingsLoader = new JsonFileMappingsLoader("mappings");
		wireMockServer.loadMappingsUsing(mappingsLoader);
		wireMockServer.start();
	}
}
