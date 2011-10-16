package com.tomakehurst.wiremock.mapping;

import static com.tomakehurst.wiremock.mapping.Response.notFound;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;

import com.tomakehurst.wiremock.http.RequestMethod;

public class MappingRequestHandler implements RequestHandler {
	
	private Mappings mappings;
	
	public MappingRequestHandler(Mappings mappings) {
		this.mappings = mappings;
	}

	@Override
	public Response handle(Request request) {
		if (request.getMethod() == RequestMethod.POST && request.getUri().equals("/mappings/new")) {
			String mappingSpecJson = request.getBodyAsString();
			RequestResponseMapping mapping = buildMappingFrom(mappingSpecJson);
			mappings.addMapping(mapping);
			return Response.created();
		} else {
			return notFound();
		}
	}

	private RequestResponseMapping buildMappingFrom(String mappingSpecJson) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			System.out.println("Binding JSON:\n" + mappingSpecJson);
			return mapper.readValue(mappingSpecJson, RequestResponseMapping.class);
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to bind JSON to object. Reason: " + ioe.getMessage() + "  JSON:" + mappingSpecJson, ioe);
		}
	}

	
}
