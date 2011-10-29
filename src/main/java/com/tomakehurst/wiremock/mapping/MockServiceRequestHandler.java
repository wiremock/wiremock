package com.tomakehurst.wiremock.mapping;

public class MockServiceRequestHandler extends AbstractRequestHandler {
	
	public static final String CONTEXT_KEY = "MockServiceRequestHandler";

	private Mappings mappings;

	public MockServiceRequestHandler(Mappings mappings) {
		this.mappings = mappings;
	}
	
	@Override
	public Response handleRequest(Request request) {
		Response response = mappings.getFor(request);
		return response;
	}

}
