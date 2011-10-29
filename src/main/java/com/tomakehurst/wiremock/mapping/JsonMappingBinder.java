package com.tomakehurst.wiremock.mapping;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;

import com.tomakehurst.wiremock.verification.VerificationResult;

public class JsonMappingBinder {

	public static RequestResponseMapping buildMappingFrom(String mappingSpecJson) {
		return read(mappingSpecJson, RequestResponseMapping.class);
	}
	
	public static String buildJsonStringFor(RequestResponseMapping mapping) {
		return write(mapping);
	}
	
	public static VerificationResult buildVerificationResultFrom(String json) {
		return read(json, VerificationResult.class);
	}
	
	public static RequestPattern buildRequestPatternFrom(String json) {
		return read(json, RequestPattern.class);
	}
	
	private static <T> T read(String json, Class<T> clazz) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(json, clazz);
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to bind JSON to object. Reason: " + ioe.getMessage() + "  JSON:" + json, ioe);
		}
	}
	
	public static <T> String write(T object) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(object);
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to generate JSON from object. Reason: " + ioe.getMessage(), ioe);
		}
	}
}
