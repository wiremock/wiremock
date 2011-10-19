package com.tomakehurst.wiremock;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;

import com.tomakehurst.wiremock.mapping.InMemoryMappings;
import com.tomakehurst.wiremock.mapping.MappingRequestHandler;
import com.tomakehurst.wiremock.mapping.Mappings;
import com.tomakehurst.wiremock.mapping.MockServiceRequestHandler;
import com.tomakehurst.wiremock.mapping.RequestHandler;
import com.tomakehurst.wiremock.servlet.MappingServlet;
import com.tomakehurst.wiremock.servlet.MockServiceServlet;
import com.tomakehurst.wiremock.standalone.JsonFileMappingsLoader;
import com.tomakehurst.wiremock.standalone.MappingsLoader;

public class WireMockServer {

	private Server jettyServer;
	private Mappings mappings;
	private RequestHandler mockServiceRequestHandler;
	private RequestHandler mappingRequestHandler;
	private int port;
	
	public WireMockServer(int port) {
		mappings = new InMemoryMappings();
		mockServiceRequestHandler = new MockServiceRequestHandler(mappings);
		mappingRequestHandler = new MappingRequestHandler(mappings);
		MockServiceServlet.setMockServiceRequestHandler(mockServiceRequestHandler);
		MappingServlet.setMappingRequestHandler(mappingRequestHandler);
		this.port = port;
	}
	
	public WireMockServer() {
		this(8080);
	}
	
	public void start() {
		startMockServiceServer();
	}
	
	public void stop() {
		try {
			jettyServer.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void startMockServiceServer() {
		jettyServer = new Server(port);
		
		Context adminContext = new Context(jettyServer, "/__admin");
		adminContext.addServlet(MappingServlet.class, "/");
		jettyServer.addHandler(adminContext);
		
		Context mockServiceContext = new Context(jettyServer, "/");
		mockServiceContext.addServlet(MockServiceServlet.class, "/");
		jettyServer.addHandler(mockServiceContext);

		try {
			jettyServer.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void loadMappingsUsing(MappingsLoader mappingsLoader) {
		mappingsLoader.loadMappingsInto(mappings);
	}
	
	public static void main(String... args) {
		WireMockServer wireMockServer = new WireMockServer();
		MappingsLoader mappingsLoader = new JsonFileMappingsLoader("mappings");
		wireMockServer.loadMappingsUsing(mappingsLoader);
		wireMockServer.start();
	}
}
