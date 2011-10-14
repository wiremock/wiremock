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

public class WireMock {

	private Server mockServiceServer;
	private Server adminServer;
	private Mappings mappings;
	private RequestHandler mockServiceRequestHandler;
	private RequestHandler mappingRequestHandler;
	
	public WireMock() {
		mappings = new InMemoryMappings();
		mockServiceRequestHandler = new MockServiceRequestHandler(mappings);
		mappingRequestHandler = new MappingRequestHandler(mappings);
		MockServiceServlet.setMockServiceRequestHandler(mockServiceRequestHandler);
		MappingServlet.setMappingRequestHandler(mappingRequestHandler);
	}
	
	public void start() {
		startMockServiceServer();
		startAdminServer();
	}
	
	public void startAdminServer() {
		adminServer = new Server(8070);
		Context context = new Context(adminServer, "/");
		context.addServlet(MappingServlet.class, "/");
		adminServer.addHandler(context);
		try {
			adminServer.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void startMockServiceServer() {
		mockServiceServer = new Server(8080);
		Context context = new Context(mockServiceServer, "/");
		context.addServlet(MockServiceServlet.class, "/");
		mockServiceServer.addHandler(context);
		try {
			mockServiceServer.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	public void stop() {
		try {
			mockServiceServer.stop();
			adminServer.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
