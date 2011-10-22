package com.tomakehurst.wiremock.mapping;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;

public class JsonMappingBinder {

	public static RequestResponseMapping buildMappingFrom(String mappingSpecJson) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(mappingSpecJson, RequestResponseMapping.class);
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to bind JSON to object. Reason: " + ioe.getMessage() + "  JSON:" + mappingSpecJson, ioe);
		}
	}
	
	public static String buildJsonStringFor(RequestResponseMapping mapping) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(mapping);
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to generate JSON from object. Reason: " + ioe.getMessage(), ioe);
		}
	}
}
