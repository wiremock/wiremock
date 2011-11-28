package com.tomakehurst.wiremock.mapping;

import com.tomakehurst.wiremock.servlet.ResponseRenderer;

public class MockServiceRequestHandler extends AbstractRequestHandler {
	
	private Mappings mappings;

	public MockServiceRequestHandler(Mappings mappings, ResponseRenderer responseRenderer) {
		super(responseRenderer);
		this.mappings = mappings;
	}
	
	@Override
	public ResponseDefinition handleRequest(Request request) {
		ResponseDefinition response = mappings.getFor(request);
		return response;
	}

}
