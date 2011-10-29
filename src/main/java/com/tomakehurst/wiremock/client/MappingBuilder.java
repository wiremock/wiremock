package com.tomakehurst.wiremock.client;

import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.RequestPattern;
import com.tomakehurst.wiremock.mapping.RequestResponseMapping;
import com.tomakehurst.wiremock.mapping.Response;

public class MappingBuilder {
	
	private RequestPatternBuilder requestPatternBuilder;
	private ResponseDefinitionBuilder responseDefBuilder;
	
	public MappingBuilder(RequestMethod method, UrlMatchingStrategy urlMatchingStrategy) {
		requestPatternBuilder = new RequestPatternBuilder(method, urlMatchingStrategy);
	}

	public MappingBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder) {
		this.responseDefBuilder = responseDefBuilder;
		return this;
	}
	
	public MappingBuilder withHeader(String key, HeaderMatchingStrategy headerMatchingStrategy) {
		requestPatternBuilder.withHeader(key, headerMatchingStrategy);
		return this;
	}
	
	public RequestResponseMapping build() {
		RequestPattern requestPattern = requestPatternBuilder.build();
		Response response = responseDefBuilder.build();
		RequestResponseMapping mapping = new RequestResponseMapping(requestPattern, response);
		return mapping;
	}
}
