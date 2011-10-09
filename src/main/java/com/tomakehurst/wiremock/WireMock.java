package com.tomakehurst.wiremock;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;

public class WireMock {

	private Server jettyServer;
	private ResponseDefinitions responses;
	
	public WireMock() {
		responses = new InMemoryResponseDefinitions();
		RequestServlet.setResponseDefinitions(responses);
	}
	
	public void start() {
		jettyServer = new Server(8080);
		Context context = new Context(jettyServer, "/");
		context.addServlet(RequestServlet.class, "/");
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
