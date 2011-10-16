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
import com.tomakehurst.wiremock.standalone.JsonFileMappingLoader;

public class WireMock {

	private Server mockServiceServer;
	private Server adminServer;
	private Mappings mappings;
	private RequestHandler mockServiceRequestHandler;
	private RequestHandler mappingRequestHandler;
	private String requestsDirectory = "requests";
	
	public WireMock() {
		mappings = new InMemoryMappings();
		mockServiceRequestHandler = new MockServiceRequestHandler(mappings);
		mappingRequestHandler = new MappingRequestHandler(mappings);
		MockServiceServlet.setMockServiceRequestHandler(mockServiceRequestHandler);
		MappingServlet.setMappingRequestHandler(mappingRequestHandler);
	}
	
	public void start() {
		new JsonFileMappingLoader(mappings, requestsDirectory).loadMappings();
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
	
	public void setRequestsDirectory(String requestsDirectory) {
		this.requestsDirectory = requestsDirectory;
	}
	
	public static void main(String... args) {
		WireMock wireMock = new WireMock();
		wireMock.start();
	}
}
