package com.tomakehurst.wiremock.mapping;

public class MockServiceRequestHandler implements RequestHandler {

	private Mappings mappings;

	public MockServiceRequestHandler(Mappings mappings) {
		this.mappings = mappings;
	}
	
	@Override
	public Response handle(Request request) {
		Response response = mappings.getFor(request);
		return response;
	}
}
