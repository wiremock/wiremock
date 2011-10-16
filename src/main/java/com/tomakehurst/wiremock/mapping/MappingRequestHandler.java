package com.tomakehurst.wiremock.mapping;

import com.tomakehurst.wiremock.http.RequestMethod;

public class MappingRequestHandler implements RequestHandler {
	
	private Mappings mappings;
	private JsonMappingCreator jsonMappingCreator;
	
	public MappingRequestHandler(Mappings mappings) {
		this.mappings = mappings;
		jsonMappingCreator = new JsonMappingCreator(mappings);
	}

	@Override
	public Response handle(Request request) {
		if (isNewMappingRequest(request)) {
			jsonMappingCreator.addMappingFrom(request.getBodyAsString());
			return Response.created();
		} else if (isResetMappingsRequest(request)) {
			mappings.reset();
			return Response.ok();
		} else {
			return Response.notFound();
		}
	}

	private boolean isResetMappingsRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && request.getUri().equals("/mappings/reset");
	}

	private boolean isNewMappingRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && request.getUri().equals("/mappings/new");
	}
}
