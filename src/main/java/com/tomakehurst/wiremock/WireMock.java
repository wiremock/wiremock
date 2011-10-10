package com.tomakehurst.wiremock;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;

import com.tomakehurst.wiremock.mapping.InMemoryMappings;
import com.tomakehurst.wiremock.mapping.Mappings;
import com.tomakehurst.wiremock.mapping.MockServiceRequestHandler;
import com.tomakehurst.wiremock.mapping.RequestHandler;
import com.tomakehurst.wiremock.servlet.MockServiceServlet;

public class WireMock {

	private Server jettyServer;
	private Mappings mappings;
	private RequestHandler mockServiceRequestHandler;
	
	public WireMock() {
		mappings = new InMemoryMappings();
		mockServiceRequestHandler = new MockServiceRequestHandler(mappings);
		MockServiceServlet.setMockServiceRequestHandler(mockServiceRequestHandler);
	}
	
	public void start() {
		jettyServer = new Server(8080);
		Context context = new Context(jettyServer, "/");
		context.addServlet(MockServiceServlet.class, "/");
		jettyServer.addHandler(context);
		try {
			jettyServer.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void stop() {
		try {
			jettyServer.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
