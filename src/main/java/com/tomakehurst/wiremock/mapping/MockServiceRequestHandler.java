package com.tomakehurst.wiremock.mapping;

public class MockServiceRequestHandler extends AbstractRequestHandler {
	
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
