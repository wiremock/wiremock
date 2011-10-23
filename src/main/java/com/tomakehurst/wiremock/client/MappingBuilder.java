package com.tomakehurst.wiremock.client;

import static com.google.common.collect.Maps.newLinkedHashMap;

import java.util.Map;

import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.RequestPattern;
import com.tomakehurst.wiremock.mapping.RequestResponseMapping;
import com.tomakehurst.wiremock.mapping.Response;

public class MappingBuilder {
	
	private RequestMethod method;
	private UrlMatchingStrategy urlMatchingStrategy;
	private ResponseDefinitionBuilder responseDefBuilder;
	private Map<String, HeaderMatchingStrategy> headers = newLinkedHashMap();
	
	
	public MappingBuilder(RequestMethod method, UrlMatchingStrategy urlMatchingStrategy) {
		this.method = method;
		this.urlMatchingStrategy = urlMatchingStrategy;
	}

	public MappingBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder) {
		this.responseDefBuilder = responseDefBuilder;
		return this;
	}
	
	public MappingBuilder withHeader(String key, HeaderMatchingStrategy headerMatchingStrategy) {
		headers.put(key, headerMatchingStrategy);
		return this;
	}
	
	public RequestResponseMapping build() {
		RequestPattern requestPattern = new RequestPattern();
		requestPattern.setMethod(method);
		urlMatchingStrategy.contributeTo(requestPattern);
		for (Map.Entry<String, HeaderMatchingStrategy> header: headers.entrySet()) {
			header.getValue().contributeTo(requestPattern, header.getKey());
		}
		
		Response response = responseDefBuilder.build();
		RequestResponseMapping mapping = new RequestResponseMapping(requestPattern, response);
		return mapping;
	}
}
