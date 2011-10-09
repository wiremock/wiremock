package com.tomakehurst.wiremock;

import static com.google.common.collect.Maps.newHashMap;
import static com.tomakehurst.wiremock.ResponseDefinition.notFound;

import java.util.Map;


public class InMemoryResponseDefinitions implements ResponseDefinitions {
	
	private Map<RequestPattern, ResponseDefinition> responseDefinitionMap = newHashMap();
	
	public InMemoryResponseDefinitions() {
		responseDefinitionMap.put(new RequestPattern("/canned/resource"), new ResponseDefinition(200, "{ \"somekey\": \"My value\" }"));
	}
	
	@Override
	public ResponseDefinition get(RequestMethod method, String uri) {
		if (uri.equals("/canned/resource") && method == RequestMethod.GET) {
			return new ResponseDefinition(200, "{ \"somekey\": \"My value\" }");
		} else {
			return notFound();
		}
	}

}
