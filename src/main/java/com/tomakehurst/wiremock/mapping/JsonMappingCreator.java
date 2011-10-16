package com.tomakehurst.wiremock.mapping;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;

public class JsonMappingCreator {

	private Mappings mappings;

	public JsonMappingCreator(Mappings mappings) {
		this.mappings = mappings;
	}
	
	public void addMappingFrom(String mappingSpecJson) {
		RequestResponseMapping mapping = buildMappingFrom(mappingSpecJson);
		mappings.addMapping(mapping);
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
